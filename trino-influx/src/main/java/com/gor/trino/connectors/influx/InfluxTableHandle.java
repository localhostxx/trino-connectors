package com.gor.trino.connectors.influx;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.trino.spi.connector.ConnectorTableHandle;
import io.trino.spi.predicate.TupleDomain;

public class InfluxTableHandle implements ConnectorTableHandle {

    private final String tableName;
    private final String schemaName;

    private final TupleDomain<InfluxColumnHandle> constraint;

    @JsonCreator
    public InfluxTableHandle(
            @JsonProperty String schemaName,
            @JsonProperty String tableName) {
        this.schemaName = schemaName;
        this.tableName = tableName;

        this.constraint = TupleDomain.all();
    }

    public InfluxTableHandle(String schemaName, String tableName, TupleDomain<InfluxColumnHandle> constraint) {
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.constraint = constraint;
    }

    @JsonProperty
    public String getTableName() {
        return tableName;
    }

    @JsonProperty
    public String getSchemaName() {
        return schemaName;
    }

    public TupleDomain<InfluxColumnHandle> getConstraint() {
        return constraint;
    }

}
