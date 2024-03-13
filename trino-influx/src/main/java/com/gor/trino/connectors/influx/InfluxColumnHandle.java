package com.gor.trino.connectors.influx;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.trino.spi.connector.ColumnHandle;
import io.trino.spi.type.Type;
import lombok.ToString;

@ToString
public class InfluxColumnHandle implements ColumnHandle {

    private final String columnName;
    private final Type columnType;

    @JsonCreator
    public InfluxColumnHandle(
            @JsonProperty(value = "columnName", required = true) String columnName,
            @JsonProperty(value = "columnType", required = true) Type columnType) {

        this.columnName = columnName;
        this.columnType = columnType;
    }

    @JsonProperty
    public String getColumnName() {
        return columnName;
    }

    @JsonProperty
    public Type getColumnType() {
        return columnType;
    }

}
