package com.gor.trino.connectors.influx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;

import io.trino.spi.type.BigintType;
import io.trino.spi.type.BooleanType;
import io.trino.spi.type.DoubleType;
import io.trino.spi.type.Type;
import io.trino.spi.type.VarcharType;

public class InfluxCache {
    private static InfluxDB influxDBClient;
    private static Map<String, InfluxTable> tablesCache = new HashMap<>();
    private static Map<String, Map<String, String>> coloumnsCaseSensitiveNames = new HashMap<>();

    public static InfluxDB getClient() {
        return influxDBClient;
    }

    public static void setClient(InfluxDB influxDBClient) {
        InfluxCache.influxDBClient = influxDBClient;
    }

    public static Map<String, InfluxTable> getTablesCache() {
        return tablesCache;
    }

    public static Map<String, Map<String, String>> getColoumnsCaseSensitiveNames() {
        return coloumnsCaseSensitiveNames;
    }

    public static List<String> getMeasurementNames() {
        QueryResult queryResult = influxDBClient.query(new Query("SHOW MEASUREMENTS"));
        QueryResult.Result result = queryResult.getResults().get(0);
        List<String> measurementNames = new ArrayList<>();

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

        return measurementNames;
    }

    public static void populateAllTables(List<String> measurementNames) {
        for (String measurementName : measurementNames) {

            Map<String, Type> schema;
            try {
                schema = getTableSchema(measurementName);
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
            List<InfluxColumnHandle> columns = new ArrayList<>();

            coloumnsCaseSensitiveNames.putIfAbsent(measurementName, new HashMap<>());

            for (Map.Entry<String, Type> entry : schema.entrySet()) {
                String columnName = entry.getKey();
                Type columnType = entry.getValue();

                coloumnsCaseSensitiveNames.get(measurementName).put(columnName.toLowerCase(), columnName);
                columns.add(new InfluxColumnHandle(columnName, columnType));
            }

            InfluxTable table = new InfluxTable(measurementName, columns);
            tablesCache.put(measurementName, table);
        }
    }

    public static Map<String, Type> getTableSchema(String measurementName)
            throws ExecutionException, InterruptedException {
        Map<String, Type> schema = new LinkedHashMap<>();
        schema.put("time", VarcharType.VARCHAR);

        CompletableFuture<Map<String, Type>> fieldKeysFuture = CompletableFuture.supplyAsync(() -> {
            Map<String, Type> fieldKeys = new HashMap<>();
            String query = String.format("SHOW FIELD KEYS FROM \"%s\"", measurementName);
            QueryResult queryResult = influxDBClient.query(new Query(query));
            List<QueryResult.Series> seriesList = queryResult.getResults().get(0).getSeries();

            if (seriesList != null && !seriesList.isEmpty()) {
                QueryResult.Series series = seriesList.get(0);
                List<List<Object>> values = series.getValues();

                for (List<Object> value : values) {
                    String fieldName = (String) value.get(0);
                    String fieldType = (String) value.get(1);
                    fieldKeys.put(fieldName, getType(fieldType));
                }
            }
            return fieldKeys;
        });

        CompletableFuture<Map<String, Type>> tagKeysFuture = CompletableFuture.supplyAsync(() -> {
            Map<String, Type> tagKeys = new HashMap<>();
            String query = String.format("SHOW TAG KEYS FROM \"%s\"", measurementName);
            QueryResult queryResult = influxDBClient.query(new Query(query));
            List<QueryResult.Series> seriesList = queryResult.getResults().get(0).getSeries();

            if (seriesList != null && !seriesList.isEmpty()) {
                QueryResult.Series series = seriesList.get(0);
                List<List<Object>> values = series.getValues();

                for (List<Object> value : values) {
                    String fieldName = (String) value.get(0);
                    tagKeys.put(fieldName, getType(query));
                }
            }
            return tagKeys;
        });

        schema.putAll(fieldKeysFuture.get());
        schema.putAll(tagKeysFuture.get());

        return schema;
    }

    public static Type getType(String fieldType) {
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

    public static void refreshMetadata() {
        tablesCache = new HashMap<>();

        List<String> measurementNames = getMeasurementNames();
        populateAllTables(measurementNames);
    }

    public static void connectToInflux(String url, String username, String password, String database) {
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            influxDBClient = InfluxDBFactory.connect(url);
        } else {
            influxDBClient = InfluxDBFactory.connect(url, username, password);
        }

        influxDBClient.setDatabase(database);

        InfluxCache.setClient(influxDBClient);
    }

}
