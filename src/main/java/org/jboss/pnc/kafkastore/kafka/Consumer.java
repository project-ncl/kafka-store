/*
 * SPDX-FileCopyrightText: Copyright © 2019 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.kafkastore.kafka;

import java.util.Optional;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.hibernate.exception.ConstraintViolationException;
import org.jboss.pnc.kafkastore.mapper.BuildStageRecordMapper;
import org.jboss.pnc.kafkastore.model.BuildStageRecord;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.smallrye.common.annotation.Blocking;
import lombok.extern.slf4j.Slf4j;

/**
 * Consume from a Kafka topic, parse the data and store it in the database
 */
@ApplicationScoped
@Slf4j
public class Consumer {

    private static final String className = Consumer.class.getName();

    @Inject
    BuildStageRecordMapper mapper;

    @Inject
    MeterRegistry registry;

    private Counter errCounter;

    @PostConstruct
    void initMetrics() {
        errCounter = registry.counter(className + ".error.count");
    }

    /**
     * Main method to consume information from a Kafka topic into the database.
     *
     * @param jsonString
     * @throws Exception
     */
    @Timed
    @Blocking
    @Incoming("duration")
    @Transactional
    @WithSpan()
    public void consume(@SpanAttribute(value = "jsonString") String jsonString) {
        try {
            Optional<BuildStageRecord> buildStageRecord = mapper.mapKafkaMsgToBuildStageRecord(jsonString);

            buildStageRecord.ifPresent(br -> {
                log.info(br.toString());
                br.persistAndFlush();
            });

        } catch (PersistenceException e) {
            errCounter.increment();
            if (e.getCause() instanceof ConstraintViolationException) {
                log.error("Kafka-store is receiving duplicate messages", e);
            } else {
                log.error("Error while saving data", e);
            }
        } catch (Exception e) {
            errCounter.increment();
            log.error("Error while saving data", e);
        }
    }
}
