package com.phonepe.platform.atomdb.client;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.apache.ratis.client.RaftClient;
import org.apache.ratis.conf.RaftProperties;
import org.apache.ratis.protocol.Message;
import org.apache.ratis.protocol.RaftClientReply;
import org.apache.ratis.protocol.RaftGroup;
import org.apache.ratis.protocol.RaftGroupId;
import org.apache.ratis.protocol.RaftPeer;

@Slf4j
public class KeyValueClient implements AutoCloseable {

    private final RaftClient client;

    public KeyValueClient(RaftPeer... peers) {
        final RaftProperties properties = new RaftProperties();
//        GrpcConfigKeys.Client.setRetryInterval(properties, 1000);

        final RaftGroupId groupId = RaftGroupId.valueOf(UUID.fromString("02511d47-d67c-49a3-9011-abb3109a44c1"));
        final RaftGroup group = RaftGroup.valueOf(groupId, peers);

        this.client = RaftClient.newBuilder()
                .setProperties(properties)
                .setRaftGroup(group)
                .build();
    }

    public String put(String key,
                      String value) throws IOException {
        final String command = "PUT:" + key + ":" + value;
        final RaftClientReply reply = client.io()
                .send(Message.valueOf(command));
        return reply.getMessage()
                .getContent()
                .toStringUtf8();
    }

    public String get(String key) throws IOException {
        final String command = "GET:" + key;
        final RaftClientReply reply = client.io()
                .sendReadOnly(Message.valueOf(command));
        return reply.getMessage()
                .getContent()
                .toStringUtf8();
    }

    @Override
    public void close() throws IOException {
        client.close();
    }

    public static void main(String[] args) throws IOException {
        // Connect to a cluster of servers
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

        KeyValueClient client = new KeyValueClient(peer1);
        log.info("Putting key1 -> value1: {}", client.put("key1", "value1"));
        log.info("Getting key1: {}", client.get("key1"));
        log.info("Getting non-existent key: {}", client.get("nonexistent"));
    }
}