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
package org.jboss.pnc.kafkastore.facade;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.assertj.core.util.Lists;
import org.jboss.pnc.kafkastore.dto.rest.BuildIdDTO;
import org.jboss.pnc.kafkastore.dto.rest.BuildMetricDTO;
import org.jboss.pnc.kafkastore.model.BuildStageRecord;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class BuildMetricsFetcherTest {

    @Inject
    BuildMetricsFetcher fetcher;

    @Test
    void testNoDuplicateMetrics() {
        createBuildStageRecordWithBuildId("1234-duplicate", "TEST", 1000, "0");
        createBuildStageRecordWithBuildId("1234-duplicate", "TEST", 2000, "1");
        createBuildStageRecordWithBuildId("1234-duplicate", "TEST", 3000, "2");
        createBuildStageRecordWithBuildId("1234-duplicate", "DONE", 10, "0");

        BuildIdDTO buildIdDTO = new BuildIdDTO();
        buildIdDTO.setBuildIds(Lists.list("1234-duplicate"));
        List<BuildMetricDTO> response = fetcher.getMetricForBuildIds(buildIdDTO);

        Set<String> metric = new HashSet<>();
        for (BuildMetricDTO dto : response) {
            assertFalse(metric.contains(dto.getName()), "There are duplicate metrics!");
            metric.add(dto.getName());

            // test if duration for "TEST" is 6000
            if (dto.getName().equals("TEST")) {
                assertEquals(6000, dto.getData().get(0));
            }
        }
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
        a.setBuildId(buildId);
        a.setBuildStage(buildStage);
        a.setDuration(duration);
        a.setProcessContextVariant(processContextVariant);
        a.persist();
    }
}