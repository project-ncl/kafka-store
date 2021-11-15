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
package org.jboss.pnc.kafkastore.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Instant;
import java.util.List;

import javax.transaction.Transactional;

import org.jboss.pnc.kafkastore.dto.rest.BuildStageRecordDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.base.Optional;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class BuildStageRecordTest {

    private static final Logger log = LoggerFactory.getLogger(BuildStageRecordTest.class);

    @Test
    void getForBuildId() {

        String buildId = "5";
        for (int i = 0; i < 15; i++) {
            createBuildStageRecordWithBuildId(buildId, "test", 123, "processVariant");
        }
        assertThat(BuildStageRecord.getForBuildId("5")).hasSize(15);
    }

    @Test
    void testLastUpdateTime() {

        String buildId = "13";
        createBuildStageRecordWithBuildId(buildId, "testLastUpdate", 123, "processVariant");
        List<BuildStageRecord> buildStageRecords = BuildStageRecord.getForBuildId("13");
        assertThat(buildStageRecords).hasSize(1);
        BuildStageRecord buildStageRecord = buildStageRecords.get(0);
        assertNotNull(buildStageRecord.getLastUpdateTime());

        BuildStageRecord lastUpdatedBuildStageRecord = buildStageRecord;
        List<BuildStageRecord> allBuildStageRecords = BuildStageRecord.findAll().list();
        for (BuildStageRecord bsr : allBuildStageRecords) {
            log.info("Build stage record found: {}", bsr);
            if (bsr.getLastUpdateTime() != null
                    && bsr.getLastUpdateTime().isAfter(lastUpdatedBuildStageRecord.getLastUpdateTime())) {
                lastUpdatedBuildStageRecord = bsr;
            }
        }
        ;

        log.info("Last update time of all build stage records: {}", lastUpdatedBuildStageRecord.getLastUpdateTime());

        int pageIndex = 0;
        int pageSize = 50;

        List<BuildStageRecord> postBuildStageRecords = BuildStageRecord
                .findNewerThan(lastUpdatedBuildStageRecord.getLastUpdateTime(), pageIndex, pageSize);

        log.info(
                "Asserting that no build stage record exists after last update time: {}",
                lastUpdatedBuildStageRecord.getLastUpdateTime());
        assertThat(postBuildStageRecords).hasSize(0);

        Long totalHits = BuildStageRecord.countNewerThan(lastUpdatedBuildStageRecord.getLastUpdateTime());
        Integer totalPages = BuildStageRecord
                .countPagesNewerThan(lastUpdatedBuildStageRecord.getLastUpdateTime(), pageSize);
        log.info("Found totalHits: {}, totalPages: {}", totalHits, totalPages);
        assertEquals(totalHits, 0);

        Instant priorOfLastUpdateTime = lastUpdatedBuildStageRecord.getLastUpdateTime().minusSeconds(1);
        List<BuildStageRecord> priorBuildStageRecords = BuildStageRecord
                .findNewerThan(priorOfLastUpdateTime, pageIndex, pageSize);
        log.info(
                "Asserting that at least one build stage record exists before latest update time: {}",
                priorOfLastUpdateTime);
        assertThat(priorBuildStageRecords).contains(lastUpdatedBuildStageRecord);

        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules()
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, false)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        BuildStageRecordDTO dto = mapper.convertValue(lastUpdatedBuildStageRecord, BuildStageRecordDTO.class);
        log.info("Converted buildStageRecord model object to DTO: {}", dto);
    }

    // *****************************************************************************************************************
    // Helper methods
    // *****************************************************************************************************************
    @Transactional
    void createBuildStageRecordWithBuildId(
            String buildId,
            String buildStage,
            int duration,
            String processContextVariant) {
        BuildStageRecord a = new BuildStageRecord();
        a.buildId = buildId;
        a.buildStage = buildStage;
        a.duration = duration;
        a.processContextVariant = processContextVariant;
        a.persist();
    }

    @Transactional
    @AfterEach
    void cleanup() {
        BuildStageRecord.deleteAll();
    }
}