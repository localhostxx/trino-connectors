package com.gor.trino.connectors.influx;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;

import io.trino.spi.connector.ColumnHandle;
import io.trino.spi.connector.ColumnMetadata;
import io.trino.spi.connector.ConnectorMetadata;
import io.trino.spi.connector.ConnectorSession;
import io.trino.spi.connector.ConnectorTableHandle;
import io.trino.spi.connector.ConnectorTableMetadata;
import io.trino.spi.connector.Constraint;
import io.trino.spi.connector.ConstraintApplicationResult;
import io.trino.spi.connector.SchemaTableName;
import io.trino.spi.connector.SchemaTablePrefix;
import io.trino.spi.connector.TableColumnsMetadata;
import io.trino.spi.connector.TableNotFoundException;
import io.trino.spi.predicate.TupleDomain;

public class InfluxMetadata implements ConnectorMetadata {

    private final InfluxClient client;

    @Inject
    public InfluxMetadata(InfluxClient client) {
        this.client = client;
    }

    @Override
    public List<String> listSchemaNames(ConnectorSession session) {
        return List.of("default");
    }

    @Override
    public List<SchemaTableName> listTables(ConnectorSession session, Optional<String> schemaNameOrNull) {
        Set<String> schemaNames = schemaNameOrNull.map(Set::of).orElse(Set.of("default"));
        ImmutableList.Builder<SchemaTableName> builder = ImmutableList.builder();
        for (String schemaName : schemaNames) {
            for (String tableName : client.getMeasurementNames()) {
                builder.add(new SchemaTableName(schemaName, tableName));
            }
        }
        return builder.build();
    }

    @Override
    public Iterator<TableColumnsMetadata> streamTableColumns(ConnectorSession session, SchemaTablePrefix prefix) {

        if (prefix.getSchema().isPresent() && prefix.getTable().isPresent()) {
            String schemaName = prefix.getSchema().get();
            String tableName = prefix.getTable().get();

            InfluxTable table = client.getTable(tableName);

            SchemaTableName schemaTableName = new SchemaTableName(schemaName, tableName);
            TableColumnsMetadata tableColumnsMetadata = new TableColumnsMetadata(schemaTableName,
                    Optional.of(table.getColumnsMetadata()));

            return ImmutableList.of(tableColumnsMetadata).iterator();
        }

        ImmutableList.Builder<TableColumnsMetadata> builder = ImmutableList.builder();

        client.getMeasurementNames().forEach(tableName -> {
            InfluxTable table = client.getTable(tableName);
            SchemaTableName schemaTableName = new SchemaTableName("default", tableName);
            TableColumnsMetadata tableColumnsMetadata = new TableColumnsMetadata(schemaTableName,
                    Optional.of(table.getColumnsMetadata()));

            builder.add(tableColumnsMetadata);
        });

        return builder.build().iterator();

    }

    @Override
    public ConnectorTableHandle getTableHandle(ConnectorSession session, SchemaTableName tableName) {
        return new InfluxTableHandle(tableName.getSchemaName(), tableName.getTableName());
    }

    @Override
    public ConnectorTableMetadata getTableMetadata(ConnectorSession session, ConnectorTableHandle tableHandle) {
        InfluxTableHandle handle = (InfluxTableHandle) tableHandle;
        SchemaTableName tableName = new SchemaTableName(handle.getSchemaName(), handle.getTableName());

        InfluxTable table = client.getTable(handle.getTableName());

        return new ConnectorTableMetadata(tableName, table.getColumnsMetadata());
    }

    @Override
    public Map<String, ColumnHandle> getColumnHandles(ConnectorSession session, ConnectorTableHandle tableHandle) {
        InfluxTableHandle handle = (InfluxTableHandle) tableHandle;
        InfluxTable table = client.getTable(handle.getTableName());

        if (table == null)
            throw new TableNotFoundException(
                    new SchemaTableName(handle.getSchemaName(), handle.getTableName()));

        ImmutableMap.Builder<String, ColumnHandle> columnHandles = ImmutableMap.builder();
        for (ColumnMetadata column : table.getColumnsMetadata()) {
            ColumnHandle columnHandle = new InfluxColumnHandle(column.getName(), column.getType());
            columnHandles.put(column.getName(), columnHandle);
        }
        return columnHandles.build();
    }

    @Override
    public ColumnMetadata getColumnMetadata(ConnectorSession session, ConnectorTableHandle tableHandle,
            ColumnHandle columnHandle) {
        InfluxColumnHandle influxColumnHandle = (InfluxColumnHandle) columnHandle;
        return new ColumnMetadata(influxColumnHandle.getColumnName(), influxColumnHandle.getColumnType());
    }

    @Override
    public Optional<ConstraintApplicationResult<ConnectorTableHandle>> applyFilter(ConnectorSession session,
            ConnectorTableHandle handle, Constraint constraint) {
        InfluxTableHandle tableHandle = (InfluxTableHandle) handle;

        TupleDomain<ColumnHandle> oldDomain = tableHandle.getConstraint();
        TupleDomain<ColumnHandle> newDomain = oldDomain.intersect(constraint.getSummary());

        if (newDomain.equals(oldDomain)) {
            return Optional.empty();
        }

        InfluxTableHandle newTableHandle = new InfluxTableHandle(tableHandle.getSchemaName(),
                tableHandle.getTableName(),
                newDomain);

        return Optional.of(new ConstraintApplicationResult<>(newTableHandle, constraint.getSummary(), false));
    }

}
