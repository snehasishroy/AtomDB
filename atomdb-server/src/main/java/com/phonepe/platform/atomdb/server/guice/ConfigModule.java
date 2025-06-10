package com.phonepe.platform.atomdb.server.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.phonepe.platform.atomdb.server.AtomDbConfiguration;
import com.phonepe.platform.atomdb.server.guice.bindings.BindingNames;
import com.phonepe.platform.atomdb.server.utils.ConfigUtils;

import java.util.function.Supplier;

public class ConfigModule extends AbstractModule {
    @Provides
    @Singleton
    @Named(BindingNames.EVENT_INGESTION)
    public Supplier<Boolean> getEventIngestionDisabled(final Supplier<AtomDbConfiguration> configuration) {
        return ConfigUtils.mappedSupplier(configuration, AtomDbConfiguration::isEventIngestionDisabled);
    }
}
