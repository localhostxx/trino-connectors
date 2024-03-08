package com.gor.trino.connectors.influx;

import com.google.common.collect.ImmutableList;

import io.trino.spi.Plugin;
import io.trino.spi.connector.ConnectorFactory;

public class InfluxPlugin implements Plugin {

    @Override
    public Iterable<ConnectorFactory> getConnectorFactories() {
        return ImmutableList.of(new InfluxConnectorFactory());
    }

}
