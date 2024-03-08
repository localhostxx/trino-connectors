package com.gor.trino.connectors.influx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.influxdb.impl.TimeUtil;

public class App {
    public static void main(String[] args) {
        String bucket = "cpu";

        InfluxDB influxDBClient = InfluxDBFactory.connect("http://localhost:8086");
        influxDBClient.setDatabase(bucket);

        // Add points to the database in string format
        String point1 = "metric1,memory=ssd speed=10";
        String point2 = "metric1,memory=hdd speed=5";
        String point3 = "metric1,memory=hdd speed=6";
        String point4 = "metric1,name=samsung cost=2000";
        String point5 = "metric1,name=sony cost=3000";

        influxDBClient.write(List.of(point1, point2));

    }
}