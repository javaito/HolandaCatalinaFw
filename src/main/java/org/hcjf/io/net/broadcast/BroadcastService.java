package org.hcjf.io.net.broadcast;

import org.hcjf.bson.BsonDecoder;
import org.hcjf.bson.BsonDocument;
import org.hcjf.bson.BsonEncoder;
import org.hcjf.io.net.InetPortProvider;
import org.hcjf.log.Log;
import org.hcjf.properties.SystemProperties;
import org.hcjf.service.Service;
import org.hcjf.utils.Introspection;

import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalTime;
import java.time.temporal.ChronoField;
import java.util.*;

/**
 * @author javaito
 */
public class BroadcastService extends Service<BroadcastConsumer> {

    private static final String IMPLEMENTATION_FIELD_NAME = "implementation";
    private static final String IP_PROTOCOL_VERSION_4 = "4";
    private static final String IP_PROTOCOL_VERSION_6 = "6";

    private static final BroadcastService instance;

    private Map<String, BroadcastInterface> interfaces;
    private BroadcastSender sender;
    private Map<String, BroadcastReceiver> receivers;
    private Map<String, BroadcastConsumer> consumers;
    private boolean shuttingDown;
    private MessageDigest messageDigest;


    static {
        instance = new BroadcastService(SystemProperties.get(SystemProperties.Net.Broadcast.SERVICE_NAME));
    }

    private BroadcastService(String serviceName) {
        super(serviceName, 2);
    }

    public static BroadcastService getInstance() {
        return instance;
    }

    @Override
    protected void init() {
        this.interfaces = new HashMap<>();
        this.consumers = new HashMap<>();
        this.sender = new BroadcastSender();
        this.receivers = new HashMap<>();
        try {
            this.messageDigest = MessageDigest.getInstance(SystemProperties.get(SystemProperties.Net.Broadcast.SIGNATURE_ALGORITHM));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("Broadcast signature algorithm not found", e);
        }
        fork(sender);
    }

    public boolean isShuttingDown() {
        return shuttingDown;
    }

    @Override
    protected void shutdown(ShutdownStage stage) {
        this.shuttingDown = true;
    }

    @Override
    public void registerConsumer(BroadcastConsumer consumer) {
        this.consumers.put(consumer.getTaskName(), consumer);
    }

    @Override
    public void unregisterConsumer(BroadcastConsumer consumer) {
        this.consumers.remove(consumer);
    }

    private synchronized BroadcastInterface getBroadcastInterface(BroadcastConsumer consumer) throws SocketException {
        Integer port = consumer.getBasePort();
        String interfaceId = BroadcastInterface.createInterfaceId(consumer.getNetInterfaceName(), port);
        BroadcastInterface result = interfaces.get(interfaceId);
        if(result == null) {

            Enumeration interfacesEnumeration = NetworkInterface.getNetworkInterfaces();
            boolean done = false;
            while(interfacesEnumeration.hasMoreElements()){
                NetworkInterface networkInterface = (NetworkInterface) interfacesEnumeration.nextElement();

                if(networkInterface.getName().equals(consumer.getNetInterfaceName())){

                    for(InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()){

                        switch(consumer.getIpVersion()) {
                            case IP_PROTOCOL_VERSION_4: {
                                if(interfaceAddress.getAddress() instanceof Inet4Address){
                                    result = new BroadcastInterface(
                                            consumer.getNetInterfaceName(), port,
                                            interfaceAddress.getAddress(), interfaceAddress.getBroadcast());
                                    interfaces.put(result.getId(), result);
                                    fork(receivers.put(result.getId(), new BroadcastReceiver(result)));
                                    done = true;
                                }
                                break;
                            }
                            case IP_PROTOCOL_VERSION_6: {
                                if(interfaceAddress.getAddress() instanceof Inet6Address){
                                    result = new BroadcastInterface(
                                            consumer.getNetInterfaceName(), port,
                                            interfaceAddress.getAddress(), interfaceAddress.getBroadcast());
                                    interfaces.put(result.getId(), result);
                                    fork(receivers.put(result.getId(), new BroadcastReceiver(result)));
                                    done = true;
                                }
                                break;
                            }
                            default: {
                                throw new IllegalArgumentException("Invalid ip version: " + consumer.getIpVersion());
                            }
                        }

                        if(done) {
                            break;
                        }
                    }
                }

                if(done) {
                    break;
                }
            }
        }

        return result;
    }

    private String sign(String name, String privateKey, String time) {
        String signature = name + privateKey + time;
        return new String(messageDigest.digest(signature.getBytes()));
    }

    private byte[] encode(BroadcastMessage message) {
        BsonDocument document = new BsonDocument();
        Map<String, Introspection.Getter> getters = Introspection.getGetters(message.getClass());
        getters.forEach((K, V) -> {
            try {
                document.put(K, V.get(message));
            } catch (Exception e) {}
        });
        return BsonEncoder.encode(document);
    }

    private BroadcastMessage decode(byte[] body) {
        BroadcastMessage message;
        try {
            BsonDocument document = BsonDecoder.decode(body);
            message = (BroadcastMessage)
                    Class.forName(document.get(IMPLEMENTATION_FIELD_NAME).getAsString()).newInstance();
            Map<String, Introspection.Setter> setters = Introspection.getSetters(message.getClass());
            setters.forEach((K, V) -> {
                try {
                    V.set(message, document.get(K).getValue());
                } catch (Exception e) {}
            });
        } catch (Exception e) {
            throw new RuntimeException();
        }
        return message;
    }

    private class BroadcastSender implements Runnable {

        @Override
        public void run() {

            BsonDocument document;

            Instant instant;
            LocalTime localTime;
            String hour;
            String minute;

            PingMessage pingMessage;
            BroadcastInterface broadcastInterface;

            //Main loop
            while(!instance.isShuttingDown()) {
                //Consumers loop
                for(BroadcastConsumer consumer : consumers.values()) {
                    try {
                        instant = Instant.now();
                        localTime = LocalTime.now();

                        hour = Integer.toString(localTime.get(ChronoField.HOUR_OF_DAY));
                        minute = Integer.toString(localTime.get(ChronoField.MINUTE_OF_HOUR));
                        broadcastInterface = getBroadcastInterface(consumer);
                        pingMessage = new PingMessage();
                        pingMessage.setTaskName(consumer.getTaskName());
                        pingMessage.setHost(broadcastInterface.getLocalAddress().getHostName());
                        pingMessage.setTimestamp(instant.toEpochMilli());
                        pingMessage.setSignature(instance.sign(consumer.getTaskName(),consumer.getPrivateKey(), hour + minute));
                        pingMessage.setCustomParameters(consumer.getPingParameters());
                        byte[] body =  instance.encode(pingMessage);
                        DatagramPacket packet = new DatagramPacket(body, body.length,
                                broadcastInterface.getBroadcastAddress(), broadcastInterface.getPort());
                        broadcastInterface.getBroadcastSocket().send(packet);
                    } catch (Exception ex) {
                        Log.w(SystemProperties.get(SystemProperties.Net.Broadcast.LOG_TAG),
                                "", ex);
                    }
                }

                synchronized (this) {
                    try {
                        this.wait(SystemProperties.getLong(SystemProperties.Net.Broadcast.SENDER_DELAY));
                    } catch (InterruptedException ex) {
                    }
                }
            }
        }

    }

    private class BroadcastReceiver implements Runnable {

        private final BroadcastInterface broadcastInterface;

        public BroadcastReceiver(BroadcastInterface broadcastInterface) {
            this.broadcastInterface = broadcastInterface;
        }

        @Override
        public void run() {

            byte[] buffer;
            DatagramPacket inputPacket;
            BroadcastMessage broadcastMessage;

            //Main loop
            while(!instance.isShuttingDown()) {
                try {
                    buffer = new byte[SystemProperties.getInteger(SystemProperties.Net.Broadcast.RECEIVER_BUFFER_SIZE)];
                    inputPacket = new DatagramPacket(buffer, buffer.length);
                    broadcastInterface.getBroadcastSocket().receive(inputPacket);
                    broadcastMessage = instance.decode(inputPacket.getData());

                    BroadcastConsumer consumer = consumers.get(broadcastMessage.getTaskName());

                    if(broadcastMessage instanceof PingMessage) {
                        consumer.onPing((PingMessage) broadcastMessage);
                    } else if(broadcastMessage instanceof PongMessage) {
                        consumer.onPong((PongMessage) broadcastMessage);
                    } else if(broadcastMessage instanceof ShutdownMessage) {
                        consumer.onShutdown((ShutdownMessage) broadcastMessage);
                    }
                } catch (Exception ex) {
                    Log.w(SystemProperties.get(SystemProperties.Net.Broadcast.LOG_TAG),
                            "Broadcast receiver error", broadcastInterface.getName(), ex);
                }
            }
        }

    }

    private static class BroadcastInterface {

        private final String id;
        private final String name;
        private final Integer port;
        private final InetAddress localAddress;
        private final InetAddress broadcastAddress;
        private final DatagramSocket broadcastSocket;

        public BroadcastInterface(String name, Integer port,
                                  InetAddress localAddress, InetAddress broadcastAddress) throws SocketException {
            this.id = createInterfaceId(name, port);
            this.name = name;
            this.port = port;
            this.localAddress = localAddress;
            this.broadcastAddress = broadcastAddress;
            this.broadcastSocket = new DatagramSocket(port);
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public Integer getPort() {
            return port;
        }

        public InetAddress getLocalAddress() {
            return localAddress;
        }

        public InetAddress getBroadcastAddress() {
            return broadcastAddress;
        }

        public DatagramSocket getBroadcastSocket() {
            return broadcastSocket;
        }

        public static String createInterfaceId(String name, Integer port) {
            return name + port.toString();
        }
    }

    private static abstract class BroadcastMessage {

        private String taskName;
        private String host;
        private Long timestamp;
        private String signature;
        private Map<String,Object> customParameters;

        public String getTaskName() {
            return taskName;
        }

        public void setTaskName(String taskName) {
            this.taskName = taskName;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public Long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Long timestamp) {
            this.timestamp = timestamp;
        }

        public String getSignature() {
            return signature;
        }

        public void setSignature(String signature) {
            this.signature = signature;
        }

        public Map<String, Object> getCustomParameters() {
            return customParameters;
        }

        public void setCustomParameters(Map<String, Object> customParameters) {
            this.customParameters = customParameters;
        }

        public String getImplementation() {
            return getClass().getName();
        }
    }

    public static class PingMessage extends BroadcastMessage {

    }

    public static class PongMessage extends BroadcastMessage {

    }

    public static class ShutdownMessage extends BroadcastMessage {

    }
}
