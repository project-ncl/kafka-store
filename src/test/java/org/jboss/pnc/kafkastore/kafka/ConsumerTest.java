/*
 * SPDX-FileCopyrightText: Copyright © 2019 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.kafkastore.kafka;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.inject.Inject;

import org.jboss.pnc.kafkastore.model.BuildStageRecord;
import org.junit.jupiter.api.Test;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@QuarkusTestResource(H2DatabaseTestResource.class)
class ConsumerTest {

    @Inject
    KafkaMessageGenerator kafkaMessageGenerator;

    @Test
    void consumeTest() throws Exception {
        // wait for some messages to be consumed
        Thread.sleep(3000);
        assertThat(BuildStageRecord.getForBuildId("consumer-testing")).isNotEmpty();
    }

}