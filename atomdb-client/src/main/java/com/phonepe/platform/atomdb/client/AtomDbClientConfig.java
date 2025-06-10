package com.phonepe.platform.atomdb.client;

import com.phonepe.platform.http.v2.common.HttpConfiguration;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
@Builder
@Jacksonized
public class AtomDbClientConfig {
    @Valid
    @NotNull
    private HttpConfiguration httpConfiguration;
}
