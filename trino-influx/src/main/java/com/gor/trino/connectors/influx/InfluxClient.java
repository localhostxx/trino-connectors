package com.gor.trino.connectors.influx;

import java.util.List;
import com.google.inject.Inject;

import lombok.NonNull;

public class InfluxClient {

    private final InfluxConfig config;

    @Inject
    public InfluxClient(@NonNull InfluxConfig config) {
        this.config = config;

        if (InfluxCache.getClient() == null) {
            InfluxCache.connectToInflux(
                    config.getInfluxUrl(),
                    config.getInfluxUsername(),
                    config.getInfluxPassword(),
                    config.getInfluxBucket());
        }

        if (InfluxCache.getTablesCache().isEmpty()) {
            InfluxCache.refreshMetadata();
        }

        System.out.println("InfluxClient created");
        System.out.println(this);
    }

    public List<String> getMeasurementNames() {

        if (InfluxCache.getTablesCache().isEmpty()) {
            InfluxCache.refreshMetadata();
        }

        return InfluxCache.getTablesCache().keySet().stream().toList();
    }

    public InfluxTable getTable(String measurementName) {

        if (InfluxCache.getTablesCache().isEmpty()) {
            InfluxCache.refreshMetadata();
        }

        return InfluxCache.getTablesCache().get(measurementName);
    }

}
