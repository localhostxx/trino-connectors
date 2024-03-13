package com.gor.trino.connectors.influx;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.trino.spi.connector.ColumnHandle;
import io.trino.spi.connector.ConnectorSplit;
import io.trino.spi.predicate.TupleDomain;

public class InfluxSplit implements ConnectorSplit {

    private final String tableName;
    private TupleDomain<ColumnHandle> constraint;

    @JsonCreator
    public InfluxSplit(
            @JsonProperty("tableName") String tableName,
            @JsonProperty("constraint") TupleDomain<ColumnHandle> constraint) {
        this.tableName = tableName;
        this.constraint = constraint;
    }

    @Override
    public Object getInfo() {
        return this;
    }

    @JsonProperty
    public String getTableName() {
        return tableName;
    }

    @JsonProperty
    public TupleDomain<ColumnHandle> getConstraint() {
        return constraint;
    }

}
