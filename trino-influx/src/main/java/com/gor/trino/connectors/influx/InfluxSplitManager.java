package com.gor.trino.connectors.influx;

import com.google.inject.Inject;

import io.trino.spi.connector.ConnectorSession;
import io.trino.spi.connector.ConnectorSplitManager;
import io.trino.spi.connector.ConnectorSplitSource;
import io.trino.spi.connector.ConnectorTableHandle;
import io.trino.spi.connector.ConnectorTransactionHandle;
import io.trino.spi.connector.Constraint;
import io.trino.spi.connector.DynamicFilter;
import io.trino.spi.connector.FixedSplitSource;
import lombok.NonNull;

import static com.google.common.base.Preconditions.checkState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InfluxSplitManager implements ConnectorSplitManager {

    private final InfluxClient client;

    @Inject
    public InfluxSplitManager(@NonNull InfluxClient client) {
        this.client = client;
    }

    @Override
    public ConnectorSplitSource getSplits(ConnectorTransactionHandle transaction,
            ConnectorSession session,
            ConnectorTableHandle handle,
            DynamicFilter dynamicFilter,
            Constraint constraint) {

        InfluxTableHandle tableHandle = (InfluxTableHandle) handle;
        InfluxTable table = client.getTable(tableHandle.getTableName());

        checkState(table != null, "Table %s.%s no longer exists", tableHandle.getSchemaName(),
                tableHandle.getTableName());

        List<InfluxSplit> splits = new ArrayList<>();
        splits.add(new InfluxSplit(tableHandle.getTableName(), tableHandle.getConstraint()));
        Collections.shuffle(splits);

        return new FixedSplitSource(splits);
    }

}
