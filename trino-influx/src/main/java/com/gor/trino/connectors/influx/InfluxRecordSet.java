package com.gor.trino.connectors.influx;

import java.util.List;

import org.eclipse.jetty.http.HttpFields.Immutable;

import com.google.common.collect.ImmutableList;

import io.trino.spi.connector.ColumnHandle;
import io.trino.spi.connector.RecordCursor;
import io.trino.spi.connector.RecordSet;
import io.trino.spi.predicate.TupleDomain;
import io.trino.spi.type.Type;
import lombok.NonNull;

public class InfluxRecordSet implements RecordSet {

    private final String tableName;
    private final List<InfluxColumnHandle> columnHandles;
    private final List<Type> columnTypes;

    private TupleDomain<ColumnHandle> constraint;

    public InfluxRecordSet(@NonNull InfluxSplit split, @NonNull List<InfluxColumnHandle> columnHandles) {
        this.columnHandles = columnHandles;
        this.tableName = split.getTableName();

        ImmutableList.Builder<Type> types = ImmutableList.builder();
        for (InfluxColumnHandle column : columnHandles) {
            types.add(column.getColumnType());
        }
        this.columnTypes = types.build();
        this.constraint = split.getConstraint();
    }

    @Override
    public List<Type> getColumnTypes() {
        return columnTypes;
    }

    @Override
    public RecordCursor cursor() {
        return new InfluxRecordCursor(tableName, columnHandles, constraint);
    }

}
