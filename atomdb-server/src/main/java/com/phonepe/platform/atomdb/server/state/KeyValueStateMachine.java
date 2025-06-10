package com.phonepe.platform.atomdb.server.state;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.ratis.proto.RaftProtos;
import org.apache.ratis.protocol.Message;
import org.apache.ratis.protocol.RaftGroupId;
import org.apache.ratis.server.RaftServer;
import org.apache.ratis.server.protocol.TermIndex;
import org.apache.ratis.server.storage.RaftStorage;
import org.apache.ratis.statemachine.TransactionContext;
import org.apache.ratis.statemachine.impl.BaseStateMachine;
import org.apache.ratis.statemachine.impl.SimpleStateMachineStorage;
import org.apache.ratis.statemachine.impl.SingleFileSnapshotInfo;

/**
 * TODO:
 * Caused by: java.io.IOException: Failed to FORMAT: One or more existing directories found [/Users/snehasish.roy/Documents/incremental-interesting-projects/atomdb/./raft-storage-server1/02511d47-d67c-49a3-9011-abb3109a44c1] for 02511d47-d67c-49a3-9011-abb3109a44c1
 */
public class KeyValueStateMachine extends BaseStateMachine {

    private final ConcurrentHashMap<String, String> keyValueStore = new ConcurrentHashMap<>();
    private final SimpleStateMachineStorage storage = new SimpleStateMachineStorage();

    @Override
    public void initialize(RaftServer server,
                           RaftGroupId groupId,
                           RaftStorage raftStorage) throws IOException {
        super.initialize(server, groupId, raftStorage);
        this.storage.init(raftStorage);
        loadSnapshot(storage.getLatestSnapshot());
    }

    @Override
    public CompletableFuture<Message> applyTransaction(TransactionContext trx) {
        final RaftProtos.LogEntryProto entry = trx.getLogEntry();
        final String logData = entry.getStateMachineLogEntry()
                .getLogData()
                .toStringUtf8();

        // Parse command: PUT:key:value or GET:key
        final String[] parts = logData.split(":", 3);
        final String operation = parts[0];

        Message reply;
        if ("PUT".equals(operation) && parts.length == 3) {
            final String key = parts[1];
            final String value = parts[2];
            keyValueStore.put(key, value);
            reply = Message.valueOf("OK");
        } else {
            reply = Message.valueOf("INVALID_COMMAND");
        }

        // Update the last applied term index
        updateLastAppliedTermIndex(entry.getTerm(), entry.getIndex());

        return CompletableFuture.completedFuture(reply);
    }

    @Override
    public CompletableFuture<Message> query(Message request) {
        final String logData = request.getContent()
                .toStringUtf8();
        final String[] parts = logData.split(":", 3);

        if ("GET".equals(parts[0]) && parts.length >= 2) {
            final String key = parts[1];
            final String value = keyValueStore.getOrDefault(key, "NOT_FOUND");
            return CompletableFuture.completedFuture(Message.valueOf(value));
        }

        return CompletableFuture.completedFuture(Message.valueOf("INVALID_QUERY"));
    }

    @Override
    public long takeSnapshot() {
        final TermIndex last = getLastAppliedTermIndex();
        final long lastAppliedIndex = last.getIndex();

        try {
            final File snapshotFile = storage.getSnapshotFile(last.getTerm(), lastAppliedIndex);
            try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(snapshotFile))) {
                out.writeObject(new ConcurrentHashMap<>(keyValueStore));
            }
            return lastAppliedIndex;
        } catch (IOException e) {
            throw new RuntimeException("Failed to take snapshot", e);
        }
    }

    private void loadSnapshot(SingleFileSnapshotInfo snapshot) throws IOException {
        if (snapshot == null) {
            return;
        }

        final File snapshotFile = snapshot.getFile()
                .getPath()
                .toFile();
        if (!snapshotFile.exists()) {
            return;
        }

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(snapshotFile))) {
            @SuppressWarnings("unchecked") ConcurrentHashMap<String, String> loaded = (ConcurrentHashMap<String, String>) in.readObject();
            keyValueStore.putAll(loaded);
        } catch (ClassNotFoundException e) {
            throw new IOException("Failed to load snapshot", e);
        }
    }
}