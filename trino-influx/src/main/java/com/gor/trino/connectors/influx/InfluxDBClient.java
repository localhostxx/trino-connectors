package com.gor.trino.connectors.influx;

import org.influxdb.InfluxDB;

public class InfluxDBClient {
    private static InfluxDB influxDB;

    public static InfluxDB getClient() {
        return influxDB;
    }

    public static void setClient(InfluxDB influxDB) {
        InfluxDBClient.influxDB = influxDB;
    }
}
