package com.gor.trino.connectors.influx;

import java.util.List;

import org.eclipse.jetty.http.HttpFields.Immutable;

import com.google.common.collect.ImmutableList;

import io.trino.spi.connector.ColumnHandle;
import io.trino.spi.connector.ConnectorRecordSetProvider;
import io.trino.spi.connector.ConnectorSession;
import io.trino.spi.connector.ConnectorSplit;
import io.trino.spi.connector.ConnectorTableHandle;
import io.trino.spi.connector.ConnectorTransactionHandle;
import io.trino.spi.connector.RecordSet;

public class InfluxRecordSetProvider implements ConnectorRecordSetProvider {

    @Override
    public RecordSet getRecordSet(
            ConnectorTransactionHandle transaction,
            ConnectorSession session,
            ConnectorSplit split,
            ConnectorTableHandle table,
            List<? extends ColumnHandle> columns) {

        InfluxSplit influxSplit = (InfluxSplit) split;

        ImmutableList.Builder<InfluxColumnHandle> handles = ImmutableList.builder();
        for (ColumnHandle column : columns) {
            handles.add((InfluxColumnHandle) column);
        }

        return new InfluxRecordSet(influxSplit, handles.build());

    }

}
