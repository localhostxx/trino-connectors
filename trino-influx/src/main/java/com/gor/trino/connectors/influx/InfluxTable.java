package com.gor.trino.connectors.influx;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;

import io.trino.spi.connector.ColumnMetadata;
import lombok.NonNull;

public class InfluxTable {

    private final String name;
    private final List<InfluxColumnHandle> columns;
    private final List<ColumnMetadata> columnsMetadata;

    @JsonCreator
    public InfluxTable(
            @JsonProperty("name") @NonNull String name,
            @JsonProperty("columns") @NonNull List<InfluxColumnHandle> columns) {
        this.name = name;
        this.columns = columns;

        ImmutableList.Builder<ColumnMetadata> columnsMetadata = ImmutableList.builder();
        for (InfluxColumnHandle column : columns) {
            columnsMetadata.add(new ColumnMetadata(column.getColumnName(), column.getColumnType()));
        }
        this.columnsMetadata = columnsMetadata.build();
    }

    @JsonProperty
    public String getName() {
        return name;
    }

    @JsonProperty
    public List<InfluxColumnHandle> getColumns() {
        return columns;
    }

    @JsonProperty
    public List<ColumnMetadata> getColumnsMetadata() {
        return columnsMetadata;
    }

}
