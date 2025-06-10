package com.phonepe.platform.atomdb.server;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.phonepe.platform.http.v2.common.HttpConfiguration;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
@FieldNameConstants
@JsonIgnoreProperties(ignoreUnknown = true)
public class AtomDbConfiguration extends BaseConfiguration {

    @Valid
    @NotNull
    private HttpConfiguration sampleAppConfiguration;

}
