package com.phonepe.platform.atomdb.server.examples;// RatisClient.java
import org.apache.ratis.client.RaftClient;
import org.apache.ratis.conf.RaftProperties;
import org.apache.ratis.grpc.GrpcFactory;
import org.apache.ratis.protocol.*;
import org.apache.ratis.thirdparty.com.google.protobuf.ByteString;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class RatisClient {
    private static final String DISCOVERY_FILE = "peers.txt";
    private static final String GROUP_ID = "x";
    
    private final RaftClient client;
    
    public RatisClient() throws IOException {
        // Discover peers from the same file used by servers
        List<RaftPeer> peers = discoverPeers();
        
        if (peers.isEmpty()) {
            throw new RuntimeException("No peers found! Make sure at least one server is running.");
        }
        
        // Create Raft group
        RaftGroupId groupId = RaftGroupId.valueOf(ByteString.copyFromUtf8(GROUP_ID));
        RaftGroup group = RaftGroup.valueOf(groupId, peers);
        
        // Create client
        RaftProperties properties = new RaftProperties();
        this.client = RaftClient.newBuilder()
            .setProperties(properties)
            .setRaftGroup(group)
            .setClientRpc(new GrpcFactory(new org.apache.ratis.conf.Parameters()).newRaftClientRpc(
                ClientId.randomId(), properties))
            .build();
        
        System.out.println("Client connected to group: " + GROUP_ID);
        System.out.println("Available peers: " + peers.size());
    }
    
    private List<RaftPeer> discoverPeers() throws IOException {
        List<RaftPeer> peers = new ArrayList<>();
        Path discoveryPath = Paths.get(DISCOVERY_FILE);
        
        if (Files.exists(discoveryPath)) {
            List<String> lines = Files.readAllLines(discoveryPath);
            for (String line : lines) {
                if (!line.trim().isEmpty()) {
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
        
        return peers;
    }
    
    public void sendCommand(String command) {
        try {
            RaftClientReply reply = client.io().send(Message.valueOf(command));
            if (reply.isSuccess()) {
                System.out.println("SUCCESS: " + reply.getMessage().getContent().toStringUtf8());
            } else {
                System.out.println("FAILED: " + reply.getException());
            }
        } catch (IOException e) {
            System.err.println("Error sending command: " + e.getMessage());
        }
    }
    
    public void sendQuery(String query) {
        try {
            RaftClientReply reply = client.io().sendReadOnly(Message.valueOf(query));
            if (reply.isSuccess()) {
                System.out.println("RESULT: " + reply.getMessage().getContent().toStringUtf8());
            } else {
                System.out.println("FAILED: " + reply.getException());
            }
        } catch (IOException e) {
            System.err.println("Error sending query: " + e.getMessage());
        }
    }
    
    public void close() throws IOException {
        client.close();
    }
    
    public static void main(String[] args) throws Exception {
        RatisClient client = new RatisClient();
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("\n=== Ratis Client ===");
        System.out.println("Commands:");
        System.out.println("  PUT:<key>=<value>  - Store a key-value pair");
        System.out.println("  GET:<key>         - Retrieve a value");
        System.out.println("  quit              - Exit client");
        System.out.println();
        
        while (true) {
            System.out.print("ratis> ");
            String input = scanner.nextLine().trim();
            
            if ("quit".equalsIgnoreCase(input)) {
                break;
            }
            
            if (input.startsWith("PUT:")) {
                client.sendCommand(input);
            } else if (input.startsWith("GET:")) {
                client.sendQuery(input);
            } else if (!input.isEmpty()) {
                System.out.println("Unknown command. Use PUT:<key>=<value> or GET:<key>");
            }
        }
        
        client.close();
        System.out.println("Client closed.");
    }
}