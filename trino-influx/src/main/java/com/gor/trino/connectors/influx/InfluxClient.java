package com.gor.trino.connectors.influx;

import java.security.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;

import com.google.inject.Inject;

import io.trino.spi.type.BigintType;
import io.trino.spi.type.BooleanType;
import io.trino.spi.type.DoubleType;
import io.trino.spi.type.TimestampType;
import io.trino.spi.type.Type;
import io.trino.spi.type.VarcharType;
import lombok.Getter;
import lombok.NonNull;

public class InfluxClient {

    private final InfluxConfig config;

    @Getter
    private final InfluxDB influxDBClient;

    @Inject
    public InfluxClient(@NonNull InfluxConfig config) {
        this.config = config;

        String url = config.getInfluxUrl();

        String username = config.getInfluxUsername();
        String password = config.getInfluxPassword();

        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            this.influxDBClient = InfluxDBFactory.connect(url);
        } else {
            this.influxDBClient = InfluxDBFactory.connect(url, username, password);
        }

        this.influxDBClient.setDatabase(config.getInfluxBucket());

        InfluxDBClient.setClient(influxDBClient);
    }

    public List<String> getMeasurementNames() {
        QueryResult queryResult = influxDBClient.query(new Query("SHOW MEASUREMENTS", config.getInfluxBucket()));
        List<QueryResult.Result> results = queryResult.getResults();
        List<String> measurementNames = new ArrayList<>();

        for (QueryResult.Result result : results) {
            List<QueryResult.Series> series = result.getSeries();
            if (series != null) {
                for (QueryResult.Series serie : series) {
                    List<List<Object>> values = serie.getValues();
                    if (values != null) {
                        for (List<Object> value : values) {
                            String measurementName = (String) value.get(0);
                            measurementNames.add(measurementName);
                        }
                    }
                }
            }
        }

        System.out.println("measurementNames: " + measurementNames);
        return measurementNames;
    }

    public boolean isMeasurementExists(String measurementName) {
        return getMeasurementNames().contains(measurementName);
    }

    public InfluxTable getTable(String measurementName) {
        String query = String.format("SELECT * FROM %s LIMIT 1", measurementName);
        QueryResult queryResult = influxDBClient.query(new Query(query));
        List<QueryResult.Series> seriesList = queryResult.getResults().get(0).getSeries();

        if (seriesList != null && !seriesList.isEmpty()) {
            QueryResult.Series series = seriesList.get(0);
            List<String> columns = series.getColumns();
            Map<String, String> fields = getFields(measurementName);

            List<InfluxColumnHandle> influxColumns = new ArrayList<>();

            influxColumns.add(new InfluxColumnHandle("time",
                    TimestampType.TIMESTAMP_MICROS, 0));

            for (int i = 1; i < columns.size(); i++) {
                String columnName = columns.get(i);

                Type type = VarcharType.VARCHAR;

                if (fields.containsKey(columnName)) {
                    type = getType(fields.get(columnName));
                }

                InfluxColumnHandle column = new InfluxColumnHandle(columnName, type, i);

                System.out.println("column: " + column);
                influxColumns.add(column);
            }

            return new InfluxTable(measurementName, influxColumns);
        }

        return null;
    }

    public Map<String, String> getFields(String measurementName) {
        String query = "SHOW FIELD KEYS FROM " + measurementName;
        QueryResult queryResult = influxDBClient.query(new Query(query, config.getInfluxBucket()));
        List<QueryResult.Series> seriesList = queryResult.getResults().get(0).getSeries();

        if (seriesList != null && !seriesList.isEmpty()) {
            QueryResult.Series series = seriesList.get(0);
            List<List<Object>> values = series.getValues();
            Map<String, String> fields = new HashMap<>();

            for (List<Object> value : values) {
                String fieldName = (String) value.get(0);
                String fieldType = (String) value.get(1);
                System.out.println("fieldName: " + fieldName + " fieldType: " + fieldType);
                fields.put(fieldName, fieldType);
            }
            return fields;
        }
        return null;
    }

    public Type getType(String fieldType) {
        switch (fieldType) {
            case "string":
                return VarcharType.VARCHAR;
            case "integer":
                return BigintType.BIGINT;
            case "float":
                return DoubleType.DOUBLE;
            case "boolean":
                return BooleanType.BOOLEAN;
            default:
                return VarcharType.VARCHAR;

        }
    }

}
