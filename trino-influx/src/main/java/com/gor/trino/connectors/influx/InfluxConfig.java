package com.gor.trino.connectors.influx;

import io.airlift.configuration.Config;
import io.airlift.configuration.ConfigDescription;
import io.airlift.configuration.ConfigSecuritySensitive;

public class InfluxConfig {

    private String influxUrl;
    private String influxUsername;
    private String influxPassword;

    private String influxBucket;

    @Config("influx.url")
    @ConfigDescription("InfluxDB URL")
    public InfluxConfig setInfluxUrl(String influxUrl) {
        this.influxUrl = influxUrl;
        return this;
    }

    @Config("influx.username")
    @ConfigDescription("InfluxDB Username")
    public InfluxConfig setInfluxUsername(String influxUsername) {
        this.influxUsername = influxUsername;
        return this;
    }

    @Config("influx.password")
    @ConfigDescription("InfluxDB Password")
    @ConfigSecuritySensitive
    public InfluxConfig setInfluxPassword(String influxPassword) {
        this.influxPassword = influxPassword;
        return this;
    }

    @Config("influx.bucket")
    @ConfigDescription("InfluxDB Bucket")
    public InfluxConfig setInfluxBucket(String influxBucket) {
        this.influxBucket = influxBucket;
        return this;
    }

    public String getInfluxUrl() {
        return influxUrl;
    }

    public String getInfluxUsername() {
        return influxUsername;
    }

    public String getInfluxPassword() {
        return influxPassword;
    }

    public String getInfluxBucket() {
        return influxBucket;
    }

}
