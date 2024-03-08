package com.gor.trino.connectors.influx;

import java.util.Iterator;
import java.util.List;

import org.influxdb.InfluxDB;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;

import io.airlift.slice.Slice;
import io.trino.spi.connector.RecordCursor;
import io.trino.spi.type.Type;
import lombok.NonNull;

public class InfluxRecordCursor implements RecordCursor {

    private final String tableName;
    private final List<InfluxColumnHandle> columnHandles;

    private List<String> fields;
    private final Long totalBytes;

    private final Iterator<List<Object>> tableDataIterator;

    public InfluxRecordCursor(@NonNull String tableName, @NonNull List<InfluxColumnHandle> columnHandles) {
        this.tableName = tableName;
        this.columnHandles = columnHandles;

        this.tableDataIterator = getTableDataIterator();
        this.fields = columnHandles.stream().map(InfluxColumnHandle::getColumnName).toList();
        this.totalBytes = 0L;
    }

    @Override
    public long getCompletedBytes() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getCompletedBytes'");
    }

    @Override
    public long getReadTimeNanos() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getReadTimeNanos'");
    }

    @Override
    public Type getType(int field) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getType'");
    }

    @Override
    public boolean advanceNextPosition() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'advanceNextPosition'");
    }

    @Override
    public boolean getBoolean(int field) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getBoolean'");
    }

    @Override
    public long getLong(int field) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getLong'");
    }

    @Override
    public double getDouble(int field) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getDouble'");
    }

    @Override
    public Slice getSlice(int field) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getSlice'");
    }

    @Override
    public Object getObject(int field) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getObject'");
    }

    @Override
    public boolean isNull(int field) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isNull'");
    }

    @Override
    public void close() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'close'");
    }

    public Iterator<List<Object>> getTableDataIterator() {
        InfluxDB client = InfluxDBClient.getClient();

        QueryResult queryResult = client.query(new Query(String.format("SELECT * FROM %s", tableName)));
        QueryResult.Result result = queryResult.getResults().get(0);

        List<QueryResult.Series> series = result.getSeries();
        if (series != null) {
            for (QueryResult.Series serie : series) {
                List<List<Object>> values = serie.getValues();
                if (values != null) {
                    return values.iterator();
                }
            }
        }

        return null;
    }
}
