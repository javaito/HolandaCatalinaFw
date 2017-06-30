package org.hcjf.io.net.broadcast;

import org.hcjf.io.net.InetPortProvider;
import org.hcjf.properties.SystemProperties;
import org.hcjf.service.Service;

import java.net.*;
import java.util.*;

/**
 * @author javaito
 */
public class BroadcastService extends Service<BroadcastConsumer> {

    private static final String IP_PROTOCOL_VERSION_4 = "4";
    private static final String IP_PROTOCOL_VERSION_6 = "6";

    private static final BroadcastService instance;

    private final Map<String, BroadcastInterface> interfaces;
    private final BroadcastSender sender;
    private final BroadcastReceiver receiver;
    private final Set<BroadcastConsumer> consumers;
    private boolean shuttingDown;


    static {
        instance = new BroadcastService(SystemProperties.get(SystemProperties.Net.Broadcast.SERVICE_NAME));
    }

    private BroadcastService(String serviceName) {
        super(serviceName, 2);
        this.interfaces = new HashMap<>();
        this.consumers = new HashSet<>();
        this.sender = new BroadcastSender();
        this.receiver = new BroadcastReceiver();
    }

    @Override
    protected void init() {
        fork(sender);
        fork(receiver);
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
        this.consumers.add(consumer);
    }

    @Override
    public void unregisterConsumer(BroadcastConsumer consumer) {
        this.consumers.remove(consumer);
    }

    private synchronized BroadcastInterface getBroadcastInterface(BroadcastConsumer consumer) throws SocketException {
        Integer port = InetPortProvider.getPort(consumer.getBasePort());
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

//    /**
//     * Este metodo genera un nuevo hash aleatorio que sera usado como clave
//     * publica en el siguiente mensaje PING y tambien sera usado para calcular
//     * la firma de los mensaje PONG que sean recibidos por el servidor.
//     */
//    private void generatePublicKey() throws SignatureException {
//        this.currentPublicKey = StringUtilities.calculateHmacSha1(UUID.randomUUID().toString(), getClusterName());
//    }
//
//    /**
//     * Devuelve la clave publica vigente.
//     * @return Clave publica vigente.
//     */
//    private String getCurrentPublicKey() {
//        return currentPublicKey;
//    }
//
//    /**
//     * Este metodo es el encargado de crear la firma con los parametro indicados
//     * @param serverId Id del servidor que genero la difucion.
//     * @param publicKey Clave publica generado por el servidor.
//     * @return Devuelve la firma adecuada dependiendo de la clave privada
//     * configurada en el servidor.
//     * @throws SignatureException
//     */
//    private String calculateSignature(String serverId, String publicKey) throws SignatureException {
//        return StringUtilities.calculateHmacSha1(publicKey + serverId, privateKey);
//    }
//
//    /**
//     * Este metodo es el encargado de crear la mensajes de contestacion a los
//     * servidores que esten haciendo difucion en la misma red.
//     * El mensaje PONG esta formado de la siguiente manera:
//     * PONG;$SERVER_ID;$SIGNATURE
//     * $SERVER_ID: Es el id del servidor que contesto.
//     * $SIGNATURE: Es el resultado de aplicar el algoritmo HmacSha1 al string
//     * formado por la concatenacion de la clave publica obtenida de la trama
//     * PING y el id del servidor remoto que tambien se obtiene de la trama PING
//     * @return Devuelve el mensaje de tipo PONG para chequear si son parte de
//     * un cluster o no.
//     */
//    private String createPongMessage(String signature, String publicKey) {
//        StringBuilder pongMessageBuilder = new StringBuilder();
//        pongMessageBuilder.append(BROADCAST_MESSAGE_PONG).append(BROADCAST_MESSAGE_FIELD_SEPARATOR);
//        pongMessageBuilder.append(getServerid()).append(BROADCAST_MESSAGE_FIELD_SEPARATOR);
//        pongMessageBuilder.append(publicKey).append(BROADCAST_MESSAGE_FIELD_SEPARATOR);
//        pongMessageBuilder.append(signature);
//        return pongMessageBuilder.toString();
//    }
//
//    /**
//     * Este metodo es el encargado de crear los mensajes del tipo PING que son
//     * los mensajes que seran enviados a toda la red con el fin de difundir la
//     * existencia del servidor.
//     * El mensaje PING esta formado de la siguiente forma:
//     * PING;$SERVER_ID;$PUBLIC_KEY
//     * $SERVER_ID: Id del servidor que genera el mensaje de difucion PING.
//     * $PUBLIC_KEY: Hash aleatorio que genera el servidor cada vez que genera
//     * un mensaje PING.
//     * @return Mensaje de difucion usado por el servidor.
//     */
//    private String createPingMessage(String publicKey) {
//        StringBuilder pingMessageBuilder = new StringBuilder();
//        pingMessageBuilder.append(BROADCAST_MESSAGE_PING).append(BROADCAST_MESSAGE_FIELD_SEPARATOR);
//        pingMessageBuilder.append(getServerid()).append(BROADCAST_MESSAGE_FIELD_SEPARATOR);
//        pingMessageBuilder.append(publicKey);
//        return pingMessageBuilder.toString();
//    }

    private class BroadcastSender implements Runnable {

        @Override
        public void run() {
//            while(!instance.isShuttingDown()) {
//                try {
//                    generatePublicKey();
//                    byte[] pingMessage = createPingMessage(getCurrentPublicKey()).getBytes();
//                    DatagramPacket packet = new DatagramPacket(pingMessage, pingMessage.length, broadcastAddress, broadcastPort);
//                    broadcastSocket.send(packet);
//                } catch (Exception ex) {
//                    LoggerManager.getLogger().log(Level.WARNING, "Error sending PING broadcasting message", ex);
//                }
//
//                try {
//                    Thread.sleep(broadcastDelay);
//                } catch (InterruptedException ex) {
//                }
//            }
        }

    }

    private class BroadcastReceiver implements Runnable {

        @Override
        public void run() {
            while(!instance.isShuttingDown()){
//                try {
//                    byte[] buffer = new byte[1024];
//                    DatagramPacket inputPacket = new DatagramPacket(buffer, buffer.length);
//                    broadcastSocket.receive(inputPacket);
//
//                    String[] message = new String(inputPacket.getData()).trim().split(BROADCAST_MESSAGE_FIELD_SEPARATOR);
//                    switch(message[0]){
//                        case BROADCAST_MESSAGE_PING: {
//                            String serverId = message[1];
//                            String publicKey = message[2];
//
//                            //En caso de que el server id de la intancia sea el
//                            //mismo que el que llego en el mensaje entonces significa
//                            //que es un mensaje de difucion enviado por la misma intancia.
//                            if(!serverId.equals(Integer.toString(getServerid()))){
//                                byte[] pongMessage = createPongMessage(calculateSignature(serverId, publicKey), publicKey).getBytes();
//                                DatagramPacket packet = new DatagramPacket(pongMessage, pongMessage.length, broadcastAddress, broadcastPort);
//                                broadcastSocket.send(packet);
//                            }
//                            break;
//                        }
//                        case BROADCAST_MESSAGE_PONG: {
//                            String serverId = message[1];
//                            String publicKey = message[2];
//                            String remoteSignature = message[3];
//                            String localSignature = calculateSignature(Integer.toString(getServerid()), publicKey);
//
//                            //En este punto se verifica que el nodo que contesto al
//                            //comando ping esta configurado con las mismas credenciales
//                            //que las que tiene configurada esta instancia.
//                            if(remoteSignature.equals(localSignature)){
//                                LoggerManager.getLogger().log(Level.INFO, "Broadcast node found, server: {0}, host: {1}",
//                                        getCloud().getLoggerFile(), serverId, inputPacket.getAddress().getHostAddress());
//                                addNode(Integer.parseInt(serverId), inputPacket.getAddress().getHostAddress());
//                            }
//                            break;
//                        }
//                    }
//                } catch (Exception ex){
//                    LoggerManager.getLogger().log(Level.WARNING, "Broadcast error", ex);
//                }
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

    private enum BroadcastMessageType {

        PING,

        PONG,

        SHUTDOWN

    }

    private static abstract class BroadcastMessage {

        private final BroadcastMessageType type;
        private final String taskName;
        private final String host;
        private final Long timestamp;
        private final String signature;

        public BroadcastMessage(BroadcastMessageType type, String taskName,
                                String host, Long timestamp, String signature) {
            this.type = type;
            this.taskName = taskName;
            this.host = host;
            this.timestamp = timestamp;
            this.signature = signature;
        }

        public BroadcastMessageType getType() {
            return type;
        }

        public String getTaskName() {
            return taskName;
        }

        public String getHost() {
            return host;
        }

        public Long getTimestamp() {
            return timestamp;
        }

        public String getSignature() {
            return signature;
        }
    }

    private static class PingMessage extends BroadcastMessage {

        private Map<String,Object> customParams;

        public PingMessage(BroadcastMessageType type, String taskName, String host,
                           Long timestamp, String signature, Map<String, Object> customParams) {
            super(type, taskName, host, timestamp, signature);
            this.customParams = customParams;
        }

    }

    private static class PongMessage extends BroadcastMessage {

        public PongMessage(BroadcastMessageType type, String taskName, String host,
                           Long timestamp, String signature) {
            super(type, taskName, host, timestamp, signature);
        }

    }

    private static class ShutdownMessage extends BroadcastMessage {

        public ShutdownMessage(BroadcastMessageType type, String taskName, String host,
                               Long timestamp, String signature) {
            super(type, taskName, host, timestamp, signature);
        }

    }
}
