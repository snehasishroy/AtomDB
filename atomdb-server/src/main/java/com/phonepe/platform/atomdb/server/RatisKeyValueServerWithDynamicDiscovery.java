package com.phonepe.platform.atomdb.server;

import com.phonepe.platform.atomdb.server.state.KeyValueStateMachine;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.ratis.RaftConfigKeys;
import org.apache.ratis.client.RaftClient;
import org.apache.ratis.conf.Parameters;
import org.apache.ratis.conf.RaftProperties;
import org.apache.ratis.netty.NettyConfigKeys;
import org.apache.ratis.netty.NettyFactory;
import org.apache.ratis.protocol.ClientId;
import org.apache.ratis.protocol.RaftClientReply;
import org.apache.ratis.protocol.RaftGroup;
import org.apache.ratis.protocol.RaftGroupId;
import org.apache.ratis.protocol.RaftPeer;
import org.apache.ratis.protocol.RaftPeerId;
import org.apache.ratis.rpc.SupportedRpcType;
import org.apache.ratis.server.RaftServer;
import org.apache.ratis.server.RaftServerConfigKeys;
import org.apache.ratis.server.storage.RaftStorage.StartupOption;
import org.apache.ratis.thirdparty.com.google.common.base.MoreObjects;

@Slf4j
public class RatisKeyValueServerWithDynamicDiscovery {

    private static final String LOCAL_ADDR = "0.0.0.0";
    @Getter
    private static RaftServer server;

    @Getter
    private static int port;

    public static final UUID GROUP_ID = UUID.fromString("02511d47-d67c-49a3-9011-abb3109a44c2");
    public static RaftGroupId RAFT_GROUP_ID = RaftGroupId.valueOf(GROUP_ID);

    private static Map<Integer, RatisKeyValueServerWithDynamicDiscovery> members = new HashMap<>();
    private static final Scanner sc = new Scanner(System.in, "UTF-8");

    public RatisKeyValueServerWithDynamicDiscovery(RaftGroup group,
                                                   RaftPeerId serverId,
                                                   int port) throws IOException {
        File storageDir = new File("./storage-" + serverId);
        storageDir.mkdirs();
        RatisKeyValueServerWithDynamicDiscovery.port = port;

        final RaftProperties properties = new RaftProperties();
        RaftServerConfigKeys.setStorageDir(properties, Collections.singletonList(storageDir));
        RaftConfigKeys.Rpc.setType(properties, SupportedRpcType.NETTY);
        NettyConfigKeys.Server.setPort(properties, port);

        RaftGroup modified = group;
//        if (port == 8090) {
//            modified = RaftGroup.emptyGroup();
//        }
        RatisKeyValueServerWithDynamicDiscovery.server = RaftServer.newBuilder()
                .setGroup(modified)
                .setProperties(properties)
                .setServerId(serverId)
                .setStateMachine(new KeyValueStateMachine())
                .setOption(StartupOption.RECOVER)
                .build();

    }


    public void start() throws IOException {
        server.start();
        log.info("Raft server started");
    }

    public void stop() throws IOException {
        server.close();
    }

    public static void main(String[] args) throws IOException {

        execute();
    }

    public static void init(List<Integer> initPorts) throws IOException {
        RaftGroup group = initGroup(initPorts);
        int port = initPorts.get(0);
        // only start the server on the first port
        RatisKeyValueServerWithDynamicDiscovery server = new RatisKeyValueServerWithDynamicDiscovery(group,
                peerId(port), port);
        server.start();
        members.put(port, server);
    }

    private static RaftGroup initGroup(List<Integer> ports) {
        List<RaftPeer> peers = new ArrayList<>();
        for (int port : ports) {
            peers.add(RaftPeer.newBuilder()
                    .setId(peerId(port))
                    .setAddress(LOCAL_ADDR + ":" + port)
                    .build());
        }
        members.values()
                .stream()
                .map(RatisKeyValueServerWithDynamicDiscovery::getPeer)
                .forEach(peers::add);
        return RaftGroup.valueOf(RAFT_GROUP_ID, peers);
    }

    private static RaftPeerId peerId(int port) {
        return RaftPeerId.valueOf("p" + port);
    }

    public RaftPeer getPeer() {
        return server.getPeer();
    }

    private static void execute() throws IOException {
        while (true) {
            String[] args = commandLineInput();
            String command = args[0];
            if (command.equalsIgnoreCase("show")) {
                show();
            } else if (command.equalsIgnoreCase("add")) {
                add(args, 1);
            } else if (command.equalsIgnoreCase("init")) {
                String[] portArguments = args[1].split(",");
                List<Integer> ports = new ArrayList<>();
                Arrays.stream(portArguments)
                        .map(Integer::parseInt)
                        .forEach(ports::add);
                init(ports);
            }
        }

//                else if (command.equalsIgnoreCase("remove")) {
//                    remove(args, 1);
//                } else if (command.equalsIgnoreCase("update")) {
//                    update(args, 1);
//                } else if (command.equalsIgnoreCase("incr")) {
//                    cluster.counterIncrement();
//                } else if (command.equalsIgnoreCase("query")) {
//                    cluster.queryCounter();
//                } else if (command.equalsIgnoreCase("quit")) {
//                    break;
//                } else {
//                    System.out.println(USAGE_MSG);
//                }
//            } catch (Exception e) {
//                System.out.println("Get error " + e.getMessage());
//            }
//        }
//        try {
//            System.out.println("Closing cluster...");
//            server.close();
//            System.out.println("Cluster closed successfully.");
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
    }

    private static String[] commandLineInput() {
        System.out.print(">>> ");
        return sc.nextLine()
                .split(" ");
    }

    public static void show() {
        Collection<RatisKeyValueServerWithDynamicDiscovery> peers = members.values();
        System.out.println(peersInfo(peers, "Cluster members"));
    }

    private static String peersInfo(Collection<RatisKeyValueServerWithDynamicDiscovery> peers,
                                    String prefix) {
        StringBuilder msgBuilder = new StringBuilder(prefix).append("={");
        if (peers.isEmpty()) {
            msgBuilder.append("}");
        } else {
            peers.forEach(p -> msgBuilder.append("\n\t")
                    .append(p));
            msgBuilder.append("\n}");
        }
        return msgBuilder.toString();
    }

    private static void add(String[] args,
                            int index) throws IOException {
        int port = 8080;//Integer.parseInt(args[index]);
        List<Integer> ports = new ArrayList();
        ports.add(port);
//        ports.addAll(ports());
        update(List.of());
    }

    public static Collection<Integer> ports() {
        return members.keySet();
    }

    public static void update(Collection<Integer> newPorts) throws IOException {
//        Preconditions.assertTrue(!members.isEmpty(), "Cluster is empty.");
//
//        Collection<RatisKeyValueServerWithDynamicDiscovery> oldPeers = members.values();
//        List<RatisKeyValueServerWithDynamicDiscovery> newPeers = new ArrayList<>();
//        List<RatisKeyValueServerWithDynamicDiscovery> peerToStart = new ArrayList<>();
//        List<RatisKeyValueServerWithDynamicDiscovery> peerToStop = new ArrayList<>();
//
//        for (Integer port : newPorts) {
//            RatisKeyValueServerWithDynamicDiscovery server = members.get(port);
//            if (server == null) {
//                // New peer always start with an empty group.
//                RaftGroup group = RaftGroup.emptyGroup();
//                server = new RatisKeyValueServerWithDynamicDiscovery(group, peerId(port), port);
//                peerToStart.add(server);
//            }
//            newPeers.add(server);
//        }
//
//        for (RatisKeyValueServerWithDynamicDiscovery peer : oldPeers) {
//            if (!newPeers.contains(peer)) {
//                peerToStop.add(peer);
//            }
//        }
//
//        // Step 1: start new peers.
//        System.out.println("Update membership ...... Step 1: start new peers.");
//        System.out.println(peersInfo(peerToStart, "Peers_to_start"));
        // I don't need to start the new peer, as it should be already running somewhere
//        for (RatisKeyValueServerWithDynamicDiscovery server : peerToStart) {
//            server.start();
//        }

        // Step 2: update membership.
//        System.out.println("Update membership ...... Step 2: update membership from C_old to C_new.");
//        System.out.println(peersInfo(oldPeers, "C_old"));
//        System.out.println(peersInfo(newPeers, "C_new"));
//        if (!members.isEmpty()) {
        try (RaftClient client = createClient()) {
//                RaftClientReply reply = client.admin()
//                        .setConfiguration(newPeers.stream()
//                                .map(RatisKeyValueServerWithDynamicDiscovery::getPeer)
//                                .collect(Collectors.toList()));
            RaftClientReply reply = client.admin()
                    .setConfiguration(List.of(RaftPeer.newBuilder()
                            .setId("p8080")
                            .setAddress("0.0.0.0:8080")
                            .build(), RaftPeer.newBuilder()
                            .setId("p8081")
                            .setAddress("0.0.0.0:8081")
                            .build(), RaftPeer.newBuilder()
                            .setId("p8083")
                            .setAddress("0.0.0.0:8083")
                            .build()));
            System.out.println("Received response");
            if (!reply.isSuccess()) {
                throw reply.getException();
            }
        }
    }

    // Step 3: stop outdated peers.
//        System.out.println("Update membership ...... Step 3: stop outdated peers.");
//        System.out.println(peersInfo(peerToStop, "Peers_to_stop"));
//        for (RatisKeyValueServerWithDynamicDiscovery server : peerToStop) {
//            server.close();
//            members.remove(server.getPort());
//        }

    // Add new peers to members.
//        for (RatisKeyValueServerWithDynamicDiscovery server : peerToStart) {
//            members.put(server.getPort(), server);
//        }

    private static RaftClient createClient() {
        RaftProperties properties = new RaftProperties();
        RaftClient.Builder builder = RaftClient.newBuilder()
                .setProperties(properties);

        builder.setRaftGroup(RaftGroup.valueOf(RatisKeyValueServerWithDynamicDiscovery.RAFT_GROUP_ID,
                List.of(RaftPeer.newBuilder()
                        .setAddress("0.0.0.0:8080")
                        .setId("p8080")
                        .build(), RaftPeer.newBuilder()
                        .setAddress("0.0.0.0:8081")
                        .setId("p8081")
                        .build(), RaftPeer.newBuilder()
                        .setAddress("0.0.0.0:8082")
                        .setId("p8082")
                        .build())));
        builder.setClientRpc(new NettyFactory(new Parameters()).newRaftClientRpc(ClientId.randomId(), properties));

        return builder.build();
    }

    @Override
    public String toString() {
        try {
            return MoreObjects.toStringHelper(this)
                    .add("server", server.getPeer())
                    .add("role", server.getDivision(RAFT_GROUP_ID)
                            .getInfo()
                            .getCurrentRole())
                    .toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}