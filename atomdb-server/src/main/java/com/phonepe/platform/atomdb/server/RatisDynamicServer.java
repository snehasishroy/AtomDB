package com.phonepe.platform.atomdb.server;

import static com.phonepe.platform.atomdb.server.discovery.ClusterManager.RAFT_GROUP_ID;

import com.phonepe.platform.atomdb.server.state.KeyValueStateMachine;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public class RatisDynamicServer {

    @Getter
    private RaftServer server;

    @Getter
    private int port;

    private Map<Integer, RatisDynamicServer> members = new HashMap<>();

    public RatisDynamicServer(RaftGroup group,
                              RaftPeerId serverId,
                              int port) throws IOException {
        File storageDir = new File("./storage-" + serverId);
        storageDir.mkdirs();
        this.port = port;

        final RaftProperties properties = new RaftProperties();
        RaftServerConfigKeys.setStorageDir(properties, Collections.singletonList(storageDir));
        RaftConfigKeys.Rpc.setType(properties, SupportedRpcType.NETTY);
        NettyConfigKeys.Server.setPort(properties, port);

        server = RaftServer.newBuilder()
                .setGroup(group)
                .setProperties(properties)
                .setServerId(serverId)
                .setStateMachine(new KeyValueStateMachine())
                .setOption(StartupOption.RECOVER)
                .build();
    }

    public void start() throws IOException {
        members.put(port, this);
        server.start();
        log.info("Raft server started");
    }

    public void stop() throws IOException {
        server.close();
    }


    public RaftPeer getPeer() {
        return server.getPeer();
    }

    public void show() {
        Collection<RatisDynamicServer> peers = members.values();
        log.info(peersInfo(peers, "Cluster members"));
    }

    private String peersInfo(Collection<RatisDynamicServer> peers,
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

    public Collection<Integer> ports() {
        return members.keySet();
    }

    public void update(Collection<Integer> newPorts) throws IOException {
        try (RaftClient client = createClient()) {
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
            log.info("Received response {}", reply);
            if (!reply.isSuccess()) {
                throw reply.getException();
            }
        }
    }

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
            log.error("Exception encountered while getting division", e);
            throw new RuntimeException(e);
        }
    }
}
