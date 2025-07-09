package com.phonepe.platform.atomdb.server.discovery;

import com.phonepe.platform.atomdb.server.RatisDynamicServer;
import com.phonepe.platform.atomdb.server.utils.ConfigUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.ratis.protocol.RaftGroup;
import org.apache.ratis.protocol.RaftGroupId;
import org.apache.ratis.protocol.RaftPeer;
import org.apache.ratis.protocol.RaftPeerId;

@Slf4j
public class ClusterManager {

    public static final UUID GROUP_ID = UUID.fromString("02511d47-d67c-49a3-9011-abb3109a44c2");
    public static final RaftGroupId RAFT_GROUP_ID = RaftGroupId.valueOf(GROUP_ID);
    public static final String INSTANCE_ID = "DROVE_INSTANCE_ID";
    ClusterService clusterService;
    String currentInstanceId;

    public ClusterManager(ClusterService clusterService) {
        // TODO: avoid dependency on drove
        currentInstanceId = ConfigUtils.readEnvOrProperty(INSTANCE_ID, INSTANCE_ID);
        this.clusterService = clusterService;
    }

    public void manage() throws IOException {
        List<DiscoveryNode> candidateNodes = clusterService.awaitDiscovery();

        if (isCandidateNode(candidateNodes)) {
            log.info("{} is a candidate node, Starting server", currentInstanceId);
            init(candidateNodes);
        } else {
            log.info("Not a candidate node {}", currentInstanceId);
        }
    }

    public void init(List<DiscoveryNode> candidates) throws IOException {
        // create the Raft group and start the server on the local node iff it's in the candidate list
        RaftGroup group = initGroup(candidates);

        for (DiscoveryNode candidate : candidates) {
            if (candidate.getNodeId()
                    .equals(currentInstanceId)) {
                RatisDynamicServer ratisServer = new RatisDynamicServer(group, peerId(candidate),
                        candidate.getPublicAddress()
                                .getPort());
                ratisServer.start();
            }
        }
    }

    private RaftGroup initGroup(List<DiscoveryNode> nodes) {
        List<RaftPeer> peers = new ArrayList<>();
        for (DiscoveryNode node : nodes) {
            peers.add(RaftPeer.newBuilder()
                    .setId(peerId(node))
                    .setAddress(node.getPublicAddress()
                            .getHost() + ":" + node.getPublicAddress()
                            .getPort())
                    .build());
        }
//        members.values()
//                .stream()
//                .map(RatisDynamicServer::getPeer)
//                .forEach(peers::add);
        return RaftGroup.valueOf(RAFT_GROUP_ID, peers);
    }

    private static RaftPeerId peerId(DiscoveryNode node) {
        return RaftPeerId.valueOf(node.getNodeId());
    }

    public boolean isCandidateNode(List<DiscoveryNode> candidateNodes) {
        return candidateNodes.stream()
                .anyMatch(node -> node.getNodeId()
                        .equals(currentInstanceId));
    }
}
