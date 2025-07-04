//package com.phonepe.platform.atomdb.server;
//
//import static com.phonepe.platform.atomdb.server.RatisKeyValueServerWithDynamicDiscovery.LOCAL_ADDR;
//
//import com.phonepe.platform.atomdb.server.state.KeyValueStateMachine;
//import java.io.File;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Collection;
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.UUID;
//import lombok.Getter;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.ratis.conf.RaftProperties;
//import org.apache.ratis.protocol.RaftGroup;
//import org.apache.ratis.protocol.RaftGroupId;
//import org.apache.ratis.protocol.RaftPeer;
//import org.apache.ratis.protocol.RaftPeerId;
//import org.apache.ratis.server.RaftServer;
//import org.apache.ratis.server.RaftServerConfigKeys;
//import org.apache.ratis.server.storage.RaftStorage.StartupOption;
//
//@Slf4j
//public class RatisKeyValueServerDummy {
//
//    private static final String LOCAL_ADDR = "0.0.0.0";
//    @Getter
//    private final RaftServer server;
//    private final RaftPeer current;
//
//    static final UUID GROUP_ID = UUID.fromString("02511d47-d67c-49a3-9011-abb3109a44c1");
//    static final RaftGroupId RAFT_GROUP_ID = RaftGroupId.valueOf(GROUP_ID);
//
//    private static Map<Integer, RatisKeyValueServerDummy> members = new HashMap<>();
//
//    //    public RatisKeyValueServerDummy(String curId,
////                                    String address,
////                                    int port,
////                                    RaftGroup group) throws IOException {
////        this.current = RaftPeer.newBuilder()
////                .setId(RaftPeerId.valueOf("p" + port))
////                .setAddress(address + ":" + port)
////                .build();
////
////        // Configure Raft properties
////        final RaftProperties properties = new RaftProperties();
////
////        // Set storage directory
////        final File storageDir = new File("./raft-storage-" + curId);
////        storageDir.mkdirs();
////        RaftServerConfigKeys.setStorageDir(properties, Collections.singletonList(storageDir));
////
////        // Configure gRPC settings
////        GrpcConfigKeys.Server.setPort(properties, port);
////
////        // Build server with the provided group
////        this.server = RaftServer.newBuilder()
////                .setGroup(group)
////                .setProperties(properties)
////                .setServerId(RaftPeerId.valueOf(curId))
////                .setStateMachine(new KeyValueStateMachine())
////                .build();
////    }
////
//    public RatisKeyValueServerDummy(RaftGroup group,
//                                    RaftPeerId serverId,
//                                    int port) throws IOException {
//        File storageDir = new File("./storage-" + serverId);
//        storageDir.mkdirs();
//
//        final RaftProperties properties = new RaftProperties();
//        RaftServerConfigKeys.setStorageDir(properties, Collections.singletonList(storageDir));
//
//        // build the Raft server.
//        this.server = RaftServer.newBuilder()
//                .setGroup(group)
//                .setProperties(properties)
//                .setServerId(serverId)
//                .setStateMachine(new KeyValueStateMachine())
//                .setOption(StartupOption.RECOVER)
//                .build();
//    }
//
//    public void start() throws IOException {
//        server.start();
//        log.info("Raft server started: {}", current.getAddress());
//    }
//
//    public void stop() throws IOException {
//        server.close();
//    }
//
//    public static void main(String[] args) throws IOException {
//        if (args.length != 3) {
//            log.error("Usage: java RatisKeyValueServer <peerId> <address> <port>");
//            System.exit(1);
//        }
//
//        String[] portArguments = args[0].split(",");
//        List<Integer> ports = new ArrayList<>();
//        Arrays.stream(portArguments)
//                .map(Integer::parseInt)
//                .forEach(ports::add);
//        if (args.length == 2) {
//            peer = RaftPeer.newBuilder()
//                    .setId("server1")
//        }
//
////        // Keep server running
////        Runtime.getRuntime()
////                .addShutdownHook(new Thread(() -> {
////                    try {
////                        server.stop();
////                    } catch (IOException e) {
////                        log.error("Failed to stop server", e);
////                    }
////                }));
//
//        try {
//            Thread.currentThread()
//                    .join();
//        } catch (InterruptedException e) {
//            Thread.currentThread()
//                    .interrupt();
//        }
//    }
//
//    public void init(List<Integer> initPorts) throws IOException {
//        RaftGroup group = initGroup(initPorts);
//        // we need to run the server on only the first port
//        RatisKeyValueServerDummy server = new RatisKeyValueServerDummy(group, peerId(initPorts.get(0)),
//                initPorts.get(0));
//        server.start();
//        members.put(initPorts.get(0), server);
//    }
//}
//
//private static RaftGroup initGroup(List<Integer> ports) {
//    List<RaftPeer> peers = new ArrayList<>();
//    for (int port : ports) {
//        peers.add(RaftPeer.newBuilder()
//                .setId(peerId(port))
//                .setAddress(LOCAL_ADDR + ":" + port)
//                .build());
//    }
//    members.values()
//            .stream()
//            .map(RatisKeyValueServerDummy::getPeer)
//            .forEach(peers::add);
//    return RaftGroup.valueOf(RAFT_GROUP_ID, peers);
//}
//
//private static RaftPeerId peerId(int port) {
//    return RaftPeerId.valueOf("p" + port);
//}
//
//public RaftPeer getPeer() {
//    return server.getPeer();
//}
//
//}