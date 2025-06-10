package com.phonepe.platform.atomdb.server.resources;

import com.phonepe.platform.atomdb.models.AtomDbResponse;
import com.phonepe.platform.atomdb.server.services.SampleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Slf4j
@Path("/housekeeping")
@Tag(name = "Housekeeping APIs")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@AllArgsConstructor(onConstructor_ = @Inject)
public class Housekeeping {
    private final SampleService sampleService;

    @GET
    @Path("/v1/ping/{prefix}")
    @Operation(description = "Ping on service")
    public AtomDbResponse<String> sync(
            final @PathParam("prefix") String prefix,
            final @QueryParam("message") @DefaultValue("Hello world") String message
    ) {
        return AtomDbResponse.ok(String.format("%s %s", prefix, message));
    }

    @GET
    @Path("/v1/sample")
    @Operation(description = "Sample service")
    public AtomDbResponse<Boolean> sampleService(final @QueryParam("queryParam") String queryParam) {
        return AtomDbResponse.ok(sampleService.sampleApi(queryParam));
    }
}
