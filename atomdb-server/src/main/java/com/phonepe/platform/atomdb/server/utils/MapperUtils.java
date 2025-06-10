package com.phonepe.platform.atomdb.server.utils;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import lombok.experimental.UtilityClass;

@UtilityClass
public class MapperUtils {

    public ObjectMapper configureMapper(final ObjectMapper mapper) {
        return mapper.registerModule(new GuavaModule())
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
                .enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES)
                .registerModule(new ParameterNamesModule(JsonCreator.Mode.PROPERTIES));
    }
}
