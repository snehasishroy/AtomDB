package com.phonepe.platform.atomdb.server.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.phonepe.olympus.im.client.OlympusIMBundle;
import com.phonepe.olympus.im.client.OlympusIMClient;
import com.phonepe.platform.atomdb.server.guice.bindings.OlympusAuthSupplier;
import com.phonepe.platform.atomdb.server.AtomDbConfiguration;
import com.phonepe.platform.atomdb.server.services.SampleService;
import com.phonepe.platform.atomdb.server.services.impl.SampleServiceHttpImpl;

import javax.inject.Singleton;
import java.util.function.Supplier;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CoreModule extends AbstractModule {
    private final OlympusIMBundle<AtomDbConfiguration> olympusIMBundle;

    @Override
    protected void configure() {
        bind(SampleService.class).to(SampleServiceHttpImpl.class);
    }

    @Provides
    @Singleton
    @OlympusAuthSupplier
    public Supplier<String> olympusAuthSupplier() {
        return () -> olympusIMBundle.getOlympusIMClient().getSystemAuthHeader();
    }

    @Provides
    @Singleton
    public OlympusIMClient olympusIMClient() {
        return olympusIMBundle.getOlympusIMClient();
    }
}

