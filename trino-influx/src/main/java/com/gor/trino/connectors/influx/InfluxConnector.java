package com.gor.trino.connectors.influx;

import com.google.inject.Inject;

import io.airlift.bootstrap.LifeCycleManager;
import io.trino.spi.connector.Connector;
import io.trino.spi.connector.ConnectorRecordSetProvider;
import io.trino.spi.connector.ConnectorSession;
import io.trino.spi.connector.ConnectorSplitManager;
import io.trino.spi.connector.ConnectorTransactionHandle;
import io.trino.spi.transaction.IsolationLevel;

import static java.util.Objects.requireNonNull;

public class InfluxConnector implements Connector {

    private final LifeCycleManager lifeCycleManager;
    private final InfluxMetadata metadata;
    private final InfluxSplitManager splitManager;
    private final InfluxRecordSetProvider recordSetProvider;

    @Inject
    public InfluxConnector(LifeCycleManager lifeCycleManager, InfluxMetadata metadata, InfluxSplitManager splitManager,
            InfluxRecordSetProvider recordSetProvider) {
        this.lifeCycleManager = requireNonNull(lifeCycleManager, "lifeCycleManager is null");
        this.metadata = requireNonNull(metadata, "metadata is null");
        this.splitManager = requireNonNull(splitManager, "splitManager is null");
        this.recordSetProvider = requireNonNull(recordSetProvider, "recordSetProvider is null");
    }

    @Override
    public ConnectorTransactionHandle beginTransaction(IsolationLevel isolationLevel, boolean readOnly,
            boolean autoCommit) {
        return InfluxTransactionHandle.INSTANCE;
    }

    @Override
    public final InfluxMetadata getMetadata(ConnectorSession session, ConnectorTransactionHandle transactionHandle) {
        return metadata;
    }

    @Override
    public ConnectorSplitManager getSplitManager() {
        return splitManager;
    }

    @Override
    public ConnectorRecordSetProvider getRecordSetProvider() {
        return recordSetProvider;
    }

    @Override
    public final void shutdown() {
        try {
            lifeCycleManager.stop();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
