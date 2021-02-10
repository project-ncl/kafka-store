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

import com.google.inject.Inject;
import io.quarkus.test.junit.QuarkusTest;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test the kafka health check prob
 *
 * @author Dennis Zhou
 * @since 4.0
 */
@QuarkusTest
public class HealthCheckTest {
    @Inject
    KafkaHealthCheck kafkaHealthCheck;

    @Test
    void connectionTest() {

        // testing Up
        HealthCheckResponse response = kafkaHealthCheck.call();
        assertThat(response.getState()).isEqualTo(HealthCheckResponse.State.UP);

        // //testing Down
        // HealthCheckResponse response2 = kafkaHealthCheck.call();
        // assertThat(response2.getState()).isEqualTo(HealthCheckResponse.State.DOWN);
    }

}
