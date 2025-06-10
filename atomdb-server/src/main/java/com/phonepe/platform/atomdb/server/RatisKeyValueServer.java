package com.phonepe.platform.atomdb.server;

import com.phonepe.platform.atomdb.server.state.KeyValueStateMachine;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.UUID;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.ratis.conf.RaftProperties;
import org.apache.ratis.grpc.GrpcConfigKeys;
import org.apache.ratis.protocol.RaftGroup;
import org.apache.ratis.protocol.RaftGroupId;
import org.apache.ratis.protocol.RaftPeer;
import org.apache.ratis.protocol.RaftPeerId;
import org.apache.ratis.server.RaftServer;
import org.apache.ratis.server.RaftServerConfigKeys;

@Slf4j
public class RatisKeyValueServer {

    @Getter
    private final RaftServer server;
    private final RaftPeer peer;

    public RatisKeyValueServer(String peerId,
                               String address,
                               int port,
                               RaftGroup group) throws IOException {
        this.peer = RaftPeer.newBuilder()
                .setId(peerId)
                .setAddress(address + ":" + port)
                .build();

        // Configure Raft properties
        final RaftProperties properties = new RaftProperties();

        // Set storage directory
        final File storageDir = new File("./raft-storage-" + peerId);
        storageDir.mkdirs();
        RaftServerConfigKeys.setStorageDir(properties, Collections.singletonList(storageDir));

        // Configure gRPC settings
        GrpcConfigKeys.Server.setPort(properties, port);

        // Build server with the provided group
        this.server = RaftServer.newBuilder()
                .setGroup(group)
                .setProperties(properties)
                .setServerId(RaftPeerId.valueOf(peerId))
                .setStateMachine(new KeyValueStateMachine())
                .build();
    }

    public void start() throws IOException {
        server.start();
        log.info("Raft server started: {}", peer.getAddress());
    }

    public void stop() throws IOException {
        server.close();
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            log.error("Usage: java RatisKeyValueServer <peerId> <address> <port>");
            System.exit(1);
        }

        final String peerId = args[0];
        final String address = args[1];
        final int port = Integer.parseInt(args[2]);

        // Create all peers for the cluster
        final RaftPeer peer1 = RaftPeer.newBuilder()
                .setId("server1")
                .setAddress("localhost:9999")
                .build();
//        final RaftPeer peer2 = RaftPeer.newBuilder()
//                .setId("server2")
//                .setAddress("localhost:6001")
//                .build();
//        final RaftPeer peer3 = RaftPeer.newBuilder()
//                .setId("server3")
//                .setAddress("localhost:6002")
//                .build();
//
        final RaftGroupId groupId = RaftGroupId.valueOf(UUID.fromString("02511d47-d67c-49a3-9011-abb3109a44c1"));
        final RaftGroup group = RaftGroup.valueOf(groupId, peer1);

        final RatisKeyValueServer server = new RatisKeyValueServer(peerId, address, port, group);
        server.start();

        // Keep server running
        Runtime.getRuntime()
                .addShutdownHook(new Thread(() -> {
                    try {
                        server.stop();
                    } catch (IOException e) {
                        log.error("Failed to stop server", e);
                    }
                }));

        try {
            Thread.currentThread()
                    .join();
        } catch (InterruptedException e) {
            Thread.currentThread()
                    .interrupt();
        }
    }
}