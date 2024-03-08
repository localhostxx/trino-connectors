package com.gor.trino.connectors.influx;

import java.util.Map;

import com.google.inject.Injector;

import io.airlift.bootstrap.Bootstrap;
import io.airlift.json.JsonModule;
import io.trino.plugin.base.TypeDeserializerModule;
import io.trino.spi.connector.Connector;
import io.trino.spi.connector.ConnectorContext;
import io.trino.spi.connector.ConnectorFactory;

public class InfluxConnectorFactory implements ConnectorFactory {

    @Override
    public String getName() {
        return "influx";
    }

    @Override
    public Connector create(String catalogName, Map<String, String> config, ConnectorContext context) {
        Bootstrap app = new Bootstrap(
                new InfluxModule(),
                new JsonModule(),
                new TypeDeserializerModule(context.getTypeManager()));

        Injector injector = app.doNotInitializeLogging().setRequiredConfigurationProperties(config).initialize();

        return injector.getInstance(InfluxConnector.class);
    }

}
