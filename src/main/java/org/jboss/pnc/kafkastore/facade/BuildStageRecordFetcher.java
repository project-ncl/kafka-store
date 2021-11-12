/**
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
package org.jboss.pnc.kafkastore.facade;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.pnc.kafkastore.dto.rest.BuildStageRecordDTO;
import org.jboss.pnc.kafkastore.model.BuildStageRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import io.micrometer.core.annotation.Timed;

@ApplicationScoped
public class BuildStageRecordFetcher {

    private static final Logger log = LoggerFactory.getLogger(BuildStageRecordFetcher.class);

    private ObjectMapper mapper = new ObjectMapper().findAndRegisterModules()
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, false)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Timed
    public List<BuildStageRecordDTO> findBuildStageRecordNewerThan(Instant lastUpdateTime) {

        List<BuildStageRecord> buildStageRecordList = BuildStageRecord.findNewerThan(lastUpdateTime);
        List<BuildStageRecordDTO> response = Optional.ofNullable(buildStageRecordList)
                .orElse(Collections.emptyList())
                .stream()
                .map(buildStageRecord -> {
                    try {
                        return mapper.convertValue(buildStageRecord, BuildStageRecordDTO.class);
                    } catch (IllegalArgumentException e) {
                        log.error("Error while converting build stage record {} to DTO!", buildStageRecord, e);
                    }
                    return null;
                })
                .collect(Collectors.toList());

        return response;
    }
}
