package com.gor.trino.connectors.influx;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Scopes;

import static io.airlift.configuration.ConfigBinder.configBinder;
import static io.airlift.json.JsonCodec.listJsonCodec;
import static io.airlift.json.JsonCodecBinder.jsonCodecBinder;

public class InfluxModule implements Module {

    @Override
    public void configure(Binder binder) {

        binder.bind(InfluxClient.class).in(Scopes.SINGLETON);
        binder.bind(InfluxMetadata.class).in(Scopes.SINGLETON);
        binder.bind(InfluxSplitManager.class).in(Scopes.SINGLETON);
        binder.bind(InfluxRecordSetProvider.class).in(Scopes.SINGLETON);
        binder.bind(InfluxConnector.class).in(Scopes.SINGLETON);

        configBinder(binder).bindConfig(InfluxConfig.class);
        jsonCodecBinder(binder).bindMapJsonCodec(String.class, listJsonCodec(InfluxTable.class));

    }

}
