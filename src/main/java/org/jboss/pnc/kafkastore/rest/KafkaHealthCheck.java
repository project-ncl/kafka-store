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
package org.jboss.pnc.kafkastore.rest;

import io.smallrye.reactive.messaging.health.HealthReport;
import io.smallrye.reactive.messaging.kafka.KafkaConnector;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Liveness;
import org.eclipse.microprofile.reactive.messaging.spi.Connector;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

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
