package com.gor.trino.connectors.influx;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.airlift.slice.Slice;
import io.airlift.slice.Slices;
import io.trino.spi.connector.ColumnHandle;
import io.trino.spi.connector.RecordCursor;
import io.trino.spi.predicate.TupleDomain;
import io.trino.spi.type.BooleanType;
import io.trino.spi.type.DoubleType;
import io.trino.spi.type.Type;
import io.trino.spi.type.VarcharType;
import lombok.NonNull;

public class InfluxRecordCursor implements RecordCursor {

    private final List<InfluxColumnHandle> columnHandles;

    private Long totalBytes;
    private List<Object> fields;

    private final boolean isTimeColumnPresent;
    private final Iterator<List<Object>> tableDataIterator;

    public InfluxRecordCursor(@NonNull String tableName, @NonNull List<InfluxColumnHandle> columnHandles,
            @NonNull TupleDomain<ColumnHandle> constraint) {
        this.totalBytes = 0L;
        this.columnHandles = columnHandles;

        this.isTimeColumnPresent = columnHandles.stream()
                .anyMatch(columnHandle -> columnHandle.getColumnName().equals("time"));

        this.tableDataIterator = InfluxDataBuilder.getTableDataIterator(tableName, columnHandles, constraint);
    }

    @Override
    public long getCompletedBytes() {
        return this.totalBytes;
    }

    @Override
    public long getReadTimeNanos() {
        return 0l;
    }

    @Override
    public Type getType(int field) {
        return columnHandles.get(field).getColumnType();
    }

    @Override
    public boolean advanceNextPosition() {
        if (tableDataIterator == null)
            return false;

        if (!tableDataIterator.hasNext())
            return false;

        fields = new ArrayList<>(tableDataIterator.next());

        if (!isTimeColumnPresent) {
            fields.remove(0);
        }

        if (fields != null) {
            for (Object field : fields) {
                if (field != null) {
                    totalBytes += field.toString().getBytes().length;
                }
            }
        }

        return true;
    }

    @Override
    public boolean getBoolean(int field) {
        checkFieldType(field, BooleanType.BOOLEAN);
        return Boolean.parseBoolean(fields.get(field).toString());
    }

    @Override
    public long getLong(int field) {

        String value = fields.get(field).toString();
        Double doubleValue = Double.parseDouble(value);
        return doubleValue.longValue();
    }

    @Override
    public double getDouble(int field) {
        checkFieldType(field, DoubleType.DOUBLE);
        return Double.parseDouble(fields.get(field).toString());
    }

    @Override
    public Slice getSlice(int field) {
        checkFieldType(field, VarcharType.createUnboundedVarcharType());
        return Slices.utf8Slice(fields.get(field).toString());
    }

    @Override
    public Object getObject(int field) {
        throw new UnsupportedOperationException("getObject is not supported");
    }

    @Override
    public boolean isNull(int field) {
        return fields.get(field) == null;
    }

    @Override
    public void close() {
    }

    private void checkFieldType(int field, Type expectedType) {
        if (getType(field) != expectedType) {
            throw new IllegalArgumentException("Expected field " + field + " to be of type " + expectedType);
        }
    }
}
