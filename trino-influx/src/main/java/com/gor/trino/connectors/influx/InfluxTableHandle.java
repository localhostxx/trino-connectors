package com.gor.trino.connectors.influx;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.trino.spi.connector.ColumnHandle;
import io.trino.spi.connector.ConnectorTableHandle;
import io.trino.spi.predicate.TupleDomain;

public class InfluxTableHandle implements ConnectorTableHandle {

    private final String tableName;
    private final String schemaName;

    private final TupleDomain<ColumnHandle> constraint;

    @JsonCreator
    public InfluxTableHandle(
            @JsonProperty("schemaName") String schemaName,
            @JsonProperty("tableName") String tableName) {

        this.schemaName = schemaName;
        this.tableName = tableName;

        this.constraint = TupleDomain.all();
    }

    public InfluxTableHandle(String schemaName, String tableName, TupleDomain<ColumnHandle> constraint) {
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.constraint = constraint;
    }

    @JsonProperty
    public String getTableName() {
        return this.tableName;
    }

    @JsonProperty
    public String getSchemaName() {
        return this.schemaName;
    }

    @JsonProperty
    public TupleDomain<ColumnHandle> getConstraint() {
        return this.constraint;
    }

}
