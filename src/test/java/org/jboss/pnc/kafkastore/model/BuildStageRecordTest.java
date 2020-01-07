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

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import javax.transaction.Transactional;

import static org.assertj.core.api.Assertions.*;

@QuarkusTest
class BuildStageRecordTest {

    @Test
    void getForBuildId() {

        String buildId = "5";
        for (int i = 0; i < 15; i++) {
            createBuildStageRecordWithBuildId(buildId, "test", 123);
        }
        assertThat(BuildStageRecord.getForBuildId("5")).hasSize(15);
    }

    // *****************************************************************************************************************
    // Helper methods
    // *****************************************************************************************************************
    @Transactional
    void createBuildStageRecordWithBuildId(String buildId, String buildStage, int duration) {
        BuildStageRecord a = new BuildStageRecord();
        a.buildId = buildId;
        a.buildStage = buildStage;
        a.duration = duration;
        a.persist();
    }

    @Transactional
    @AfterEach
    void cleanup() {
        BuildStageRecord.deleteAll();
    }
}