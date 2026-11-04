/*
 * SPDX-FileCopyrightText: Copyright © 2019 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.kafkastore.rest;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Liveness;
import org.eclipse.microprofile.reactive.messaging.spi.Connector;

import io.smallrye.reactive.messaging.health.HealthReport;
import io.smallrye.reactive.messaging.kafka.KafkaConnector;

/**
 * // The health check prob for kafka store.
 *
 * @author Dennis Zhou
 * @since 4.0
 */
@Liveness
@ApplicationScoped
public class KafkaHealthCheck implements HealthCheck {
    @Inject
    @Connector(value = "smallrye-kafka")
    KafkaConnector kafkaConnector;

    @Override
    public HealthCheckResponse call() {
        HealthCheckResponseBuilder responseBuilder = HealthCheckResponse.named("Kafka Store connection health check");

        HealthReport healthReport = kafkaConnector.getLiveness();
        if (healthReport.isOk()) {
            responseBuilder.up();
        } else {
            responseBuilder.down();
        }

        return responseBuilder.build();
    }
}
