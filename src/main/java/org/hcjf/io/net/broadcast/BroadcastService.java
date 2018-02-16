package org.hcjf.io.net.broadcast;

import org.hcjf.bson.BsonDecoder;
import org.hcjf.bson.BsonDocument;
import org.hcjf.bson.BsonEncoder;
import org.hcjf.io.net.InetPortProvider;
import org.hcjf.log.Log;
import org.hcjf.properties.SystemProperties;
import org.hcjf.service.Service;
import org.hcjf.utils.Introspection;

import java.io.Closeable;
import java.io.IOException;
import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalTime;
import java.time.temporal.ChronoField;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * This service provides capabilities to register broadcast task in order to notify
 * for all the net this task.
 * @author javaito
 */
public class BroadcastService extends Service<BroadcastConsumer> {

    private static final String IMPLEMENTATION_FIELD_NAME = "implementation";
    private static final String IP_PROTOCOL_VERSION_4 = "4";
    private static final String IP_PROTOCOL_VERSION_6 = "6";

    private static final BroadcastService instance;
    private static final UUID instanceId;

    private Map<String, BroadcastInterface> interfaces;
    private BroadcastSender sender;
    private Map<String, BroadcastReceiver> receivers;
    private Map<String, BroadcastConsumer> consumers;
    private boolean shuttingDown;
    private MessageDigest messageDigest;

    static {
        instance = new BroadcastService(SystemProperties.get(SystemProperties.Net.Broadcast.SERVICE_NAME));
        instanceId = UUID.randomUUID();
    }

    private BroadcastService(String serviceName) {
        super(serviceName, 2);
    }

    /**
     * Returns the singleton instance of the service.
     * @return Service instance.
     */
    public static BroadcastService getInstance() {
        return instance;
    }

    /**
     * This method initialize all the necessary components of the service.
     */
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

    /**
     * Verify if the service is in the shutting down process.
     * @return True if the service in the shutting down process or not in the otherwise.
     */
    public boolean isShuttingDown() {
        return shuttingDown;
    }

    @Override
    protected void shutdown(ShutdownStage stage) {
        this.shuttingDown = true;
        synchronized (this.sender) {
            this.sender.notify();
        }
    }

    /**
     * Register a consumer into the the service.
     * @param consumer Object with the logic to consume the service.
     */
    @Override
    public void registerConsumer(BroadcastConsumer consumer) {
        this.consumers.put(consumer.getTaskName(), consumer);
        synchronized (this.sender) {
            this.sender.notify();
        }
    }

    /**
     * Unregister a consumer of the service.
     * @param consumer Consumer to unregister.
     */
    @Override
    public void unregisterConsumer(BroadcastConsumer consumer) {
        this.consumers.remove(consumer);
    }

    /**
     * Returns the broadcast interface, this interface contains the datagram socket to send a receive the
     * task between all the host of the net.
     * @param consumer Consumer with the information to create the interface.
     * @return Broadcast interface instance.
     * @throws SocketException
     */
    private synchronized BroadcastInterface getBroadcastInterface(BroadcastConsumer consumer) throws SocketException {
        Integer port = consumer.getPort();
        String interfaceId = BroadcastInterface.createInterfaceId(consumer.getNetInterfaceName(), port);
        BroadcastInterface result = interfaces.get(interfaceId);
        if(result == null) {

            //Obtain all the net interfaces of the operating system.
            Enumeration interfacesEnumeration = NetworkInterface.getNetworkInterfaces();
            boolean done = false;

            //Check for each interfaces if the interface is the same that the interface into the consumer.
            while(interfacesEnumeration.hasMoreElements()){
                NetworkInterface networkInterface = (NetworkInterface) interfacesEnumeration.nextElement();

                if(networkInterface.getName().equals(consumer.getNetInterfaceName())){

                    //Find the first interface address with the same protocol version that the consumer.
                    for(InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()){

                        switch(consumer.getIpVersion()) {
                            case IP_PROTOCOL_VERSION_4: {
                                done = interfaceAddress.getAddress() instanceof Inet4Address;
                                break;
                            }
                            case IP_PROTOCOL_VERSION_6: {
                                done = interfaceAddress.getAddress() instanceof Inet6Address;
                                break;
                            }
                            default: {
                                throw new IllegalArgumentException("Invalid ip version: " + consumer.getIpVersion());
                            }
                        }

                        if(done) {
                            //Creates a broadcast interface using the interface address
                            result = new BroadcastInterface(
                                    consumer.getNetInterfaceName(), port,
                                    interfaceAddress.getAddress(), interfaceAddress.getBroadcast());
                            interfaces.put(result.getId(), result);

                            //Creates a new instance of the receiver.
                            BroadcastReceiver broadcastReceiver = new BroadcastReceiver(result);
                            receivers.put(result.getId(), broadcastReceiver);
                            //Create a new thread to the interface receiver.
                            fork(broadcastReceiver);
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

    /**
     * This method creates a signature using the name of the task, private key
     * of the consumer and the minute and seconds of the instant.
     * @param name Name of the task.
     * @param privateKey Private key of the consumer.
     * @param time Minute and second of the instant.
     * @return Signature created.
     */
    private String sign(String name, String privateKey, String time) {
        String signature = name + privateKey + time;
        return new String(messageDigest.digest(signature.getBytes()));
    }

    /**
     * Serialize the broadcast message and transform it in a byte array with bson format.
     * @param message Message to serialize.
     * @return Serialized message.
     */
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

    /**
     * Creates a broadcast message instance from a bson stored into the byte array.
     * @param body Byte array with bson format.
     * @return Decoded message instance.
     */
    private BroadcastMessage decode(byte[] body) {
        BroadcastMessage message;
        try {
            BsonDocument document = BsonDecoder.decode(body);
            message = (BroadcastMessage)
                    Class.forName(document.get(IMPLEMENTATION_FIELD_NAME).getAsString()).getConstructor().newInstance();
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

    /**
     * This runnable contains the logic to send a ping message periodically,
     * and send the shutdown message when the kill signal start the shutdown process
     * into the instance of system.
     */
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
                        pingMessage.setPort(broadcastInterface.getPort());
                        pingMessage.setInstanceId(instanceId);
                        pingMessage.setTimestamp(instant.toEpochMilli());
                        pingMessage.setSignature(instance.sign(consumer.getTaskName(),consumer.getPrivateKey(), hour + minute));
                        pingMessage.setCustomParameters(consumer.getPingParameters());
                        byte[] body =  instance.encode(pingMessage);
                        DatagramPacket packet;
                        for (int i = broadcastInterface.getPort(); i < broadcastInterface.getPort() + 10; i++) {
                            packet = new DatagramPacket(body, body.length,
                                    broadcastInterface.getBroadcastAddress(), i);
                            broadcastInterface.getBroadcastSocket().send(packet);
                        }
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

            ShutdownMessage shutdownMessage;
            for(BroadcastConsumer consumer : consumers.values()) {
                try {
                    instant = Instant.now();
                    localTime = LocalTime.now();

                    hour = Integer.toString(localTime.get(ChronoField.HOUR_OF_DAY));
                    minute = Integer.toString(localTime.get(ChronoField.MINUTE_OF_HOUR));
                    broadcastInterface = getBroadcastInterface(consumer);
                    shutdownMessage = new ShutdownMessage();
                    shutdownMessage.setTaskName(consumer.getTaskName());
                    shutdownMessage.setHost(broadcastInterface.getLocalAddress().getHostName());
                    shutdownMessage.setPort(broadcastInterface.getPort());
                    shutdownMessage.setInstanceId(instanceId);
                    shutdownMessage.setTimestamp(instant.toEpochMilli());
                    shutdownMessage.setSignature(instance.sign(consumer.getTaskName(),consumer.getPrivateKey(), hour + minute));
                    shutdownMessage.setCustomParameters(consumer.getPingParameters());
                    byte[] body =  instance.encode(shutdownMessage);
                    DatagramPacket packet = new DatagramPacket(body, body.length,
                            broadcastInterface.getBroadcastAddress(), broadcastInterface.getPort());
                    broadcastInterface.getBroadcastSocket().send(packet);
                    broadcastInterface.close();
                } catch (Exception ex) {
                    Log.w(SystemProperties.get(SystemProperties.Net.Broadcast.LOG_TAG),
                            "", ex);
                }
            }
        }

    }

    /**
     * This runnable is all the time listening the broadcast interface in order to
     * receive the broadcast messages sending for other host into the net.
     */
    private class BroadcastReceiver implements Runnable {

        private final BroadcastInterface broadcastInterface;

        public BroadcastReceiver(BroadcastInterface broadcastInterface) {
            this.broadcastInterface = broadcastInterface;
        }

        @Override
        public void run() {

            Instant instant;
            LocalTime localTime;
            String hour;
            String minute;

            byte[] buffer;
            DatagramPacket inputPacket;
            BroadcastMessage broadcastMessage;

            //Main loop
            while(!instance.isShuttingDown()) {
                try {
                    buffer = new byte[SystemProperties.getInteger(SystemProperties.Net.Broadcast.RECEIVER_BUFFER_SIZE)];
                    inputPacket = new DatagramPacket(buffer, buffer.length);
                    try {
                        broadcastInterface.getBroadcastSocket().receive(inputPacket);
                    } catch (IOException ex) {
                        continue;
                    }
                    broadcastMessage = instance.decode(inputPacket.getData());

                    BroadcastConsumer consumer = consumers.get(broadcastMessage.getTaskName());

                    if(broadcastMessage instanceof PingMessage) {
                        if(!broadcastMessage.getInstanceId().equals(instanceId)) {
                            consumer.onPing((PingMessage) broadcastMessage);

                            instant = Instant.now();
                            localTime = LocalTime.now();

                            hour = Integer.toString(localTime.get(ChronoField.HOUR_OF_DAY));
                            minute = Integer.toString(localTime.get(ChronoField.MINUTE_OF_HOUR));

                            PongMessage pongMessage = new PongMessage();
                            pongMessage.setTaskName(consumer.getTaskName());
                            pongMessage.setHost(broadcastInterface.getLocalAddress().getHostName());
                            pongMessage.setPort(broadcastInterface.getPort());
                            pongMessage.setInstanceId(instanceId);
                            pongMessage.setTimestamp(instant.toEpochMilli());
                            pongMessage.setSignature(instance.sign(consumer.getTaskName(),consumer.getPrivateKey(), hour + minute));
                            pongMessage.setCustomParameters(consumer.getPingParameters());
                            byte[] body =  instance.encode(pongMessage);
                            DatagramPacket packet = new DatagramPacket(body, body.length,
                                    InetAddress.getByName(broadcastMessage.getHost()), broadcastInterface.getPort());
                            broadcastInterface.getBroadcastSocket().send(packet);
                        }
                    } else if(broadcastMessage instanceof PongMessage) {
                        consumer.onPong((PongMessage) broadcastMessage);
                    } else if(broadcastMessage instanceof ShutdownMessage) {
                        if(!broadcastMessage.getHost().equals(broadcastInterface.getLocalAddress().getHostName())) {
                            consumer.onShutdown((ShutdownMessage) broadcastMessage);
                        }
                    }
                } catch (Exception ex) {
                    Log.w(SystemProperties.get(SystemProperties.Net.Broadcast.LOG_TAG),
                            "Broadcast receiver error", broadcastInterface.getName(), ex);
                }
            }
        }

    }

    /**
     * This broadcast interface is created for each combination of net interface name and port,
     * and contains all the information to create the socket to send a receive the broadcast messages.
     */
    private static class BroadcastInterface implements Closeable {

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

        /**
         * Closes the broadcast socket.
         * @throws IOException
         */
        @Override
        public void close() throws IOException {
            broadcastSocket.close();
        }

        /**
         * Returns the id of the broadcast interface.
         * @return Interface id.
         */
        public String getId() {
            return id;
        }

        /**
         * Returns the name of the interface.
         * @return Name of the interface.
         */
        public String getName() {
            return name;
        }

        /**
         * Returns the port of the interface.
         * @return Port of the interface.
         */
        public Integer getPort() {
            return port;
        }

        /**
         * Returns the local address of the interface.
         * @return Local address.
         */
        public InetAddress getLocalAddress() {
            return localAddress;
        }

        /**
         * Returns the broadcast address of the interface.
         * @return Broadcast address.
         */
        public InetAddress getBroadcastAddress() {
            return broadcastAddress;
        }

        /**
         * Return the datagram socket of the broadcast interface.
         * @return Datagram socket.
         */
        public DatagramSocket getBroadcastSocket() {
            return broadcastSocket;
        }

        /**
         * Utility method to create the interface id based on the name of the interface and the port.
         * @param name Name of the interface.
         * @param port Port of the interface.
         * @return Generated id.
         */
        public static String createInterfaceId(String name, Integer port) {
            return name + port.toString();
        }

    }

    /**
     * Base class for all the broadcast messages.
     */
    private static abstract class BroadcastMessage {

        private String taskName;
        private String host;
        private Integer port;
        private Long timestamp;
        private String signature;
        private UUID instanceId;
        private Map<String,Object> customParameters;

        /**
         * Returns the task name.
         * @return Task name.
         */
        public String getTaskName() {
            return taskName;
        }

        /**
         * Sets the task name.
         * @param taskName Task name.
         */
        public void setTaskName(String taskName) {
            this.taskName = taskName;
        }

        /**
         * Returns the host name.
         * @return Host name.
         */
        public String getHost() {
            return host;
        }

        /**
         * Sets the host name.
         * @param host Host name.
         */
        public void setHost(String host) {
            this.host = host;
        }

        /**
         * Returns the port number.
         * @return Port number.
         */
        public Integer getPort() {
            return port;
        }

        /**
         * Set the port number.
         * @param port Port number.
         */
        public void setPort(Integer port) {
            this.port = port;
        }

        /**
         * Returns the creation timestamp.
         * @return Creation timestamp.
         */
        public Long getTimestamp() {
            return timestamp;
        }

        /**
         * Sets create timestamp.
         * @param timestamp Creation timestamp.
         */
        public void setTimestamp(Long timestamp) {
            this.timestamp = timestamp;
        }

        /**
         * Returns the package signature.
         * @return Package signature.
         */
        public String getSignature() {
            return signature;
        }

        /**
         * Sets the package signature.
         * @param signature Package signature.
         */
        public void setSignature(String signature) {
            this.signature = signature;
        }

        public UUID getInstanceId() {
            return instanceId;
        }

        public void setInstanceId(UUID instanceId) {
            this.instanceId = instanceId;
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

    /**
     * Message that is sending in order to publish the instance for all the net
     */
    public static class PingMessage extends BroadcastMessage {}

    /**
     * Message that is sending as response for the Ping Message.
     */
    public static class PongMessage extends BroadcastMessage {}

    /**
     * Message that is sending to notify for all the net that the instance is shutting down
     */
    public static class ShutdownMessage extends BroadcastMessage {}
}
