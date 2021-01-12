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
package org.jboss.pnc.kafkastore.kafka;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.pnc.kafkastore.mapper.BuildStageRecordMapper;
import org.jboss.pnc.kafkastore.model.BuildStageRecord;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Consume from a Kafka topic, parse the data and store it in the database
 */
@ApplicationScoped
@Slf4j
public class Consumer {

    @Inject
    BuildStageRecordMapper mapper;

    private final MeterRegistry registry;
    private final Counter errCounter;

    Consumer(MeterRegistry registry) {
        this.registry = registry;
        this.errCounter = registry.counter("error.count");
    }

    /**
     * Main method to consume information from a Kafka topic into the database.
     *
     * @param jsonString
     * @throws Exception
     */
    @Timed
    @Incoming("duration")
    public void consume(String jsonString) {
        System.out.print(".");

        try {
            Optional<BuildStageRecord> buildStageRecord = mapper.mapKafkaMsgToBuildStageRecord(jsonString);

            // TODO: handle error better
            // do this because method running in an IO thread and we can only store a POJO in a worker thread
            buildStageRecord.ifPresent(br -> {
                log.info(br.toString());
                CompletableFuture.runAsync(() -> store(br)).exceptionally(e -> {
                    errCounter.increment();
                    log.error("Error while saving data", e);
                    return null;
                });
            });
        } catch (Exception e) {
            errCounter.increment();
            log.error("Something wrong happened during consumption", e);
        }
    }

    @Timed
    @Transactional
    void store(BuildStageRecord message) {
        message.persist();
    }
}
