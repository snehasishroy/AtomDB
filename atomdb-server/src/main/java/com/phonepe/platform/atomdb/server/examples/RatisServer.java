package com.phonepe.platform.atomdb.server.examples;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.ratis.client.RaftClient;
import org.apache.ratis.conf.RaftProperties;
import org.apache.ratis.grpc.GrpcConfigKeys;
import org.apache.ratis.proto.RaftProtos;
import org.apache.ratis.protocol.Message;
import org.apache.ratis.protocol.RaftGroup;
import org.apache.ratis.protocol.RaftGroupId;
import org.apache.ratis.protocol.RaftPeer;
import org.apache.ratis.protocol.RaftPeerId;
import org.apache.ratis.server.RaftServer;
import org.apache.ratis.server.RaftServerConfigKeys;
import org.apache.ratis.statemachine.TransactionContext;
import org.apache.ratis.statemachine.impl.BaseStateMachine;
import org.apache.ratis.util.TimeDuration;

public class RatisServer {

    private static final String DISCOVERY_FILE = "peers.txt";
    private static final String GROUP_ID = "11fb71c0-9a71-4625-86a5-27a1a3363d33";
    private static final int MAX_DISCOVERY_ATTEMPTS = 30;
    private static final int DISCOVERY_WAIT_MS = 1000;
    private static final int PEER_REFRESH_INTERVAL_SECONDS = 10;

    private final String peerId;
    private final int port;
    private final RaftServer server;
    private final RaftGroupId groupId;
    private final RaftProperties properties;
    private final ScheduledExecutorService scheduler;
    private volatile boolean isRunning = false;
    private volatile Set<String> knownPeers = new HashSet<>();

    public RatisServer(String peerId,
                       int port) throws IOException, InterruptedException {
        this.peerId = peerId;
        this.port = port;
        this.groupId = RaftGroupId.valueOf(UUID.fromString(GROUP_ID));
        this.scheduler = Executors.newScheduledThreadPool(1);

        // Register this peer in discovery file
        registerPeer(peerId, port);

        // Wait for initial peer discovery
        List<RaftPeer> initialPeers = waitForInitialPeerDiscovery();

        // Track known peers
        knownPeers.addAll(initialPeers.stream()
                .map(p -> p.getId()
                        .toString())
                .collect(Collectors.toSet()));

        // Create Raft group
        RaftGroup group = RaftGroup.valueOf(groupId, initialPeers);

        // Configure server with better settings
        this.properties = configureRaftProperties(port);

        // Create and build server
        this.server = RaftServer.newBuilder()
                .setGroup(group)
                .setProperties(properties)
                .setServerId(RaftPeerId.valueOf(peerId))
                .setStateMachine(new SimpleStateMachine())
                .build();
    }

    private RaftProperties configureRaftProperties(int port) {
        RaftProperties properties = new RaftProperties();

        // Basic configuration
        GrpcConfigKeys.Server.setPort(properties, port);
        RaftServerConfigKeys.setStorageDir(properties, Collections.singletonList(new File("target/" + peerId)));

        // Election timeouts - increase to reduce election conflicts
        RaftServerConfigKeys.Rpc.setTimeoutMin(properties, TimeDuration.valueOf(300, TimeUnit.MILLISECONDS));
        RaftServerConfigKeys.Rpc.setTimeoutMax(properties, TimeDuration.valueOf(600, TimeUnit.MILLISECONDS));

        // Leader election configuration
        RaftServerConfigKeys.LeaderElection.setPreVote(properties, true);

        // Increase RPC timeout for better stability
        RaftServerConfigKeys.Rpc.setRequestTimeout(properties, TimeDuration.valueOf(5000, TimeUnit.MILLISECONDS));

        // Log configuration
        RaftServerConfigKeys.Log.setSegmentSizeMax(properties, org.apache.ratis.util.SizeInBytes.valueOf("16MB"));

        return properties;
    }

    private void registerPeer(String peerId,
                              int port) throws IOException {
        // Use file locking to prevent race conditions
        Path discoveryPath = Paths.get(DISCOVERY_FILE);
        Path lockPath = Paths.get(DISCOVERY_FILE + ".lock");

        // Simple file-based locking
        int attempts = 0;
        while (attempts < 10) {
            try {
                if (!Files.exists(lockPath)) {
                    Files.createFile(lockPath);
                    break;
                }
                Thread.sleep(100);
                attempts++;
            } catch (Exception e) {
                attempts++;
            }
        }

        try {
            // Read existing peers
            Set<String> existingPeers = new HashSet<>();
            if (Files.exists(discoveryPath)) {
                existingPeers.addAll(Files.readAllLines(discoveryPath));
            }

            // Add this peer
            String peerEntry = peerId + ":" + port;
            existingPeers.add(peerEntry);

            // Write back to file
            try (PrintWriter writer = new PrintWriter(new FileWriter(DISCOVERY_FILE))) {
                for (String peer : existingPeers) {
                    writer.println(peer);
                }
            }

            System.out.println("Registered peer: " + peerEntry);
        } finally {
            // Release lock
            try {
                Files.deleteIfExists(lockPath);
            } catch (IOException e) {
                // Ignore
            }
        }
    }

    private List<RaftPeer> waitForInitialPeerDiscovery() throws InterruptedException {
        List<RaftPeer> peers = new ArrayList<>();

        for (int attempt = 0; attempt < MAX_DISCOVERY_ATTEMPTS; attempt++) {
            peers = discoverPeers();

            // If we have more than just ourselves, we can proceed
            if (peers.size() > 1) {
                System.out.println("Discovered " + peers.size() + " peers, proceeding with cluster formation");
                break;
            }

            // For single-node clusters, proceed after a few attempts
            if (attempt > 5) {
                System.out.println("Starting as single-node cluster");
                break;
            }

            System.out.println(
                    "Waiting for peer discovery... (attempt " + (attempt + 1) + "/" + MAX_DISCOVERY_ATTEMPTS + ")");
            Thread.sleep(DISCOVERY_WAIT_MS);
        }

        return peers;
    }

    private List<RaftPeer> discoverPeers() {
        List<RaftPeer> peers = new ArrayList<>();
        Path discoveryPath = Paths.get(DISCOVERY_FILE);

        try {
            if (Files.exists(discoveryPath)) {
                List<String> lines = Files.readAllLines(discoveryPath);
                for (String line : lines) {
                    if (!line.trim()
                            .isEmpty()) {
                        String[] parts = line.split(":");
                        if (parts.length == 2) {
                            String id = parts[0];
                            int port = Integer.parseInt(parts[1]);

                            RaftPeer peer = RaftPeer.newBuilder()
                                    .setId(id)
                                    .setAddress(new InetSocketAddress("localhost", port))
                                    .build();
                            peers.add(peer);

                            System.out.println("Discovered peer: " + id + " on port " + port);
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading discovery file: " + e.getMessage());
        }

        if (peers.isEmpty()) {
            // If no peers discovered, add self
            RaftPeer selfPeer = RaftPeer.newBuilder()
                    .setId(peerId)
                    .setAddress(new InetSocketAddress("localhost", port))
                    .build();
            peers.add(selfPeer);
        }

        return peers;
    }

    private RaftClient createClient() throws IOException {
        // Get current cluster configuration
        RaftGroup currentGroup = server.getDivision(groupId)
                .getGroup();

        return RaftClient.newBuilder()
                .setRaftGroup(currentGroup)
                .build();
    }

    private void startPeerDiscoveryService() {
        scheduler.scheduleWithFixedDelay(() -> {
            if (!isRunning) {
                return;
            }

            try {
                checkForNewPeers();
            } catch (Exception e) {
                System.err.println("Error during peer discovery: " + e.getMessage());
            }
        }, PEER_REFRESH_INTERVAL_SECONDS, PEER_REFRESH_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    private void checkForNewPeers() {
        try {
            List<RaftPeer> currentPeers = discoverPeers();
            Set<String> discoveredPeerIds = currentPeers.stream()
                    .map(p -> p.getId()
                            .toString())
                    .collect(Collectors.toSet());

            // Check if there are new peers
            Set<String> newPeerIds = new HashSet<>(discoveredPeerIds);
            newPeerIds.removeAll(knownPeers);

            if (!newPeerIds.isEmpty()) {
                System.out.println("New peers detected: " + newPeerIds);

                // Only update configuration if this node is the leader or if cluster allows it
                try {
                    updateClusterConfiguration(currentPeers);
                    knownPeers.addAll(newPeerIds);
                    System.out.println("Successfully added new peers to cluster: " + newPeerIds);
                } catch (Exception e) {
                    System.err.println("Failed to update cluster configuration: " + e.getMessage());
                    // If we're not the leader, the leader will eventually do the update
                }
            }
        } catch (Exception e) {
            System.err.println("Error checking for new peers: " + e.getMessage());
        }
    }

    private void updateClusterConfiguration(List<RaftPeer> newPeers) throws Exception {
        try (RaftClient client = createClient()) {
            // Use the admin API to set the new configuration
            var reply = client.admin()
                    .setConfiguration(newPeers);

            if (!reply.isSuccess()) {
                if (reply.getException() != null) {
                    throw reply.getException();
                } else {
                    throw new RuntimeException("Failed to update cluster configuration: " + reply);
                }
            }

            System.out.println("Cluster configuration updated successfully");
        } catch (Exception e) {
            // This is expected if we're not the leader
            System.out.println("Cannot update configuration - not the leader: " + e.getMessage());
        }
    }

    public void start() throws IOException {
        server.start();
        isRunning = true;

        System.out.println("Ratis server " + peerId + " started on port " + port);
        System.out.println("Group ID: " + GROUP_ID);

        // Give some time for cluster formation
        try {
            Thread.sleep(2000);
            var division = server.getDivision(groupId);
            if (division != null) {
                System.out.println("Server role: " + division.getInfo());
            }
        } catch (Exception e) {
            System.out.println("Could not determine server role: " + e.getMessage());
        }

        // Start the peer discovery service
        startPeerDiscoveryService();
        System.out.println("Dynamic peer discovery service started");
    }

    public void shutdown() throws IOException {
        isRunning = false;

        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread()
                        .interrupt();
            }
        }

        if (server != null) {
            server.close();
        }
    }

    public RaftServer getServer() {
        return server;
    }

    // Enhanced state machine for demonstration
    private static class SimpleStateMachine extends BaseStateMachine {

        private final Map<String, String> keyValueStore = new HashMap<>();

        @Override
        public CompletableFuture<Message> applyTransaction(TransactionContext trx) {
            final RaftProtos.LogEntryProto entry = trx.getLogEntry();
            final String logData = entry.getStateMachineLogEntry()
                    .getLogData()
                    .toStringUtf8();

            System.out.println("Applying transaction: " + logData);

            // Simple key-value operations
            if (logData.startsWith("PUT:")) {
                String[] parts = logData.substring(4)
                        .split("=", 2);
                if (parts.length == 2) {
                    keyValueStore.put(parts[0], parts[1]);
                    System.out.println("Stored: " + parts[0] + " = " + parts[1]);
                    return CompletableFuture.completedFuture(Message.valueOf("OK: " + parts[0] + "=" + parts[1]));
                }
            } else if (logData.startsWith("DELETE:")) {
                String key = logData.substring(7);
                String removed = keyValueStore.remove(key);
                if (removed != null) {
                    System.out.println("Deleted: " + key);
                    return CompletableFuture.completedFuture(Message.valueOf("DELETED: " + key));
                } else {
                    return CompletableFuture.completedFuture(Message.valueOf("NOT_FOUND: " + key));
                }
            }

            return CompletableFuture.completedFuture(Message.valueOf("UNKNOWN COMMAND: " + logData));
        }

        @Override
        public CompletableFuture<Message> query(Message request) {
            final String queryData = request.getContent()
                    .toStringUtf8();

            if (queryData.startsWith("GET:")) {
                String key = queryData.substring(4);
                String value = keyValueStore.getOrDefault(key, "NOT_FOUND");
                return CompletableFuture.completedFuture(Message.valueOf(value));
            } else if (queryData.equals("LIST")) {
                StringBuilder sb = new StringBuilder();
                for (Map.Entry<String, String> entry : keyValueStore.entrySet()) {
                    sb.append(entry.getKey())
                            .append("=")
                            .append(entry.getValue())
                            .append("\n");
                }
                return CompletableFuture.completedFuture(Message.valueOf(sb.toString()));
            }

            return CompletableFuture.completedFuture(Message.valueOf("UNKNOWN QUERY: " + queryData));
        }

        @Override
        public void initialize(RaftServer server,
                               RaftGroupId groupId,
                               org.apache.ratis.server.storage.RaftStorage storage) throws IOException {
            super.initialize(server, groupId, storage);
            System.out.println("State machine initialized");
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Usage: java RatisServer <peer-id> <port>");
            System.err.println("Example: java RatisServer peer1 8090");
            System.exit(1);
        }

        String peerId = args[0];
        int port = Integer.parseInt(args[1]);

        // Clean up discovery file on first run
        if (System.getProperty("clean") != null) {
            Files.deleteIfExists(Paths.get(DISCOVERY_FILE));
            Files.deleteIfExists(Paths.get(DISCOVERY_FILE + ".lock"));
            System.out.println("Cleaned discovery file");
        }

        try {
            RatisServer ratisServer = new RatisServer(peerId, port);

            // Add shutdown hook
            Runtime.getRuntime()
                    .addShutdownHook(new Thread(() -> {
                        try {
                            ratisServer.shutdown();
                            System.out.println("Server shutdown complete");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }));

            ratisServer.start();

            // Keep server running
            System.out.println("Server is running. Press Ctrl+C to stop.");
            Thread.currentThread()
                    .join();

        } catch (Exception e) {
            System.err.println("Failed to start server: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}