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
package org.jboss.pnc.kafkastore.kafka;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.jboss.pnc.kafkastore.dto.ingest.KafkaMessageDTO;
import org.jboss.pnc.kafkastore.mapper.BuildStageRecordMapper;

import io.smallrye.mutiny.Multi;
import lombok.extern.slf4j.Slf4j;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

@ApplicationScoped
@Slf4j
public class KafkaMessageGenerator {

    private PodamFactory factory = new PodamFactoryImpl();

    @Inject
    BuildStageRecordMapper mapper;

    @Outgoing("duration")
    public Multi<String> generate() {

        // run it for about 2000 milliseconds
        return Multi.createFrom().range(0, 10).map(tick -> {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            KafkaMessageDTO kafkaMessageDTO = factory.manufacturePojo(KafkaMessageDTO.class);
            kafkaMessageDTO.getMdc().setProcessContext("consumer-testing");
            kafkaMessageDTO.setLoggerName("org.jboss.pnc._userlog_.process-stage-update");
            kafkaMessageDTO.getMdc().setProcessStageStep("END");
            try {
                String data = mapper.toJsonString(kafkaMessageDTO);
                log.debug("Sending: {}", data);
                return data;
            } catch (Exception e) {
                e.printStackTrace();
                return "";
            }
        });
    }
}
