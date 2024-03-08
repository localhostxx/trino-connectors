package com.gor.trino.connectors.influx;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.trino.spi.connector.ColumnHandle;
import io.trino.spi.connector.ConnectorSplit;
import io.trino.spi.predicate.TupleDomain;

public class InfluxSplit implements ConnectorSplit {

    private final String tableName;
    private TupleDomain<ColumnHandle> tupleDomain;

    @JsonCreator
    public InfluxSplit(@JsonProperty("tableName") String tableName) {
        this.tableName = tableName;
        this.tupleDomain = null;
        // TODO: Implement TupleDomain
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
    public TupleDomain<ColumnHandle> getTupleDomain() {
        return tupleDomain;
    }

}
