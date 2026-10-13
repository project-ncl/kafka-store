/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2019 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.pnc.kafkastore.rest;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.pnc.api.dto.ComponentVersion;
import org.jboss.pnc.kafkastore.Constants;
import org.jboss.pnc.kafkastore.dto.rest.BuildIdDTO;
import org.jboss.pnc.kafkastore.dto.rest.BuildMetricDTO;
import org.jboss.pnc.kafkastore.dto.rest.PagedBuildStageRecordDTO;
import org.jboss.pnc.kafkastore.facade.BuildMetricsFetcher;
import org.jboss.pnc.kafkastore.facade.BuildStageRecordFetcher;
import org.jboss.pnc.kafkastore.model.BuildStageRecord;

import io.micrometer.core.annotation.Timed;

@Path("/")
@ApplicationScoped
public class MainRest {

    @ConfigProperty(name = "quarkus.application.name")
    String name;

    @Inject
    BuildMetricsFetcher buildMetricsFetcher;

    @Inject
    BuildStageRecordFetcher buildStageRecordFetcher;

    @GET
    @RolesAllowed({ "pnc-users", "pnc-users-admin" })
    public long getCount() {
        return BuildStageRecord.count();
    }

    @POST
    @Path("/builds")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ "pnc-users", "pnc-users-admin" })
    @Timed
    public List<BuildMetricDTO> getBuildMetricsForBuildId(BuildIdDTO buildIdDTO) {
        return buildMetricsFetcher.getMetricForBuildIds(buildIdDTO);
    }

    @GET
    @Path("/build-stages-newer-than-timestamp")
    @RolesAllowed({ "pnc-users", "pnc-users-admin" })
    public PagedBuildStageRecordDTO getAllBuildStageRecordsNewerThanTimestamp(
            @QueryParam("timestamp") long timestamp,
            @QueryParam("pageIndex") int pageIndex,
            @QueryParam("pageSize") int pageSize) {
        return buildStageRecordFetcher
                .findBuildStageRecordNewerThan(Instant.ofEpochMilli(timestamp), pageIndex, pageSize);
    }

    @GET
    @Path("/version")
    public ComponentVersion getVersion() {
        return ComponentVersion.builder()
                .name(name)
                .version(Constants.KAFKA_STORE_VERSION)
                .commit(Constants.COMMIT_HASH)
                .builtOn(ZonedDateTime.parse(Constants.BUILD_TIME))
                .build();
    }

}
