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
package org.jboss.pnc.kafkastore.mapper;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.jboss.pnc.kafkastore.dto.ingest.KafkaMessageDTO;
import org.jboss.pnc.kafkastore.model.BuildStageRecord;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.util.Optional;

@ApplicationScoped
@Slf4j
public class BuildStageRecordMapper {

    private static final String className = BuildStageRecordMapper.class.getName();

    ObjectMapper mapper = new ObjectMapper().findAndRegisterModules()
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, false);

    @Inject
    MeterRegistry registry;

    private Counter errCounter;

    @PostConstruct
    void initMetrics() {
        errCounter = registry.counter(className + ".error.count");
    }

    @Timed
    public Optional<BuildStageRecord> mapKafkaMsgToBuildStageRecord(String jsonString)
            throws BuildStageRecordMapperException {

        try {
            KafkaMessageDTO kafkaMessageDTO = mapper.readValue(jsonString, KafkaMessageDTO.class);

            if (kafkaMessageDTO.getMdc() != null && kafkaMessageDTO.getMdc().getProcessStageName() != null
                    && kafkaMessageDTO.getMdc().getProcessContext() != null
                    && kafkaMessageDTO.getMdc().getProcessStageStep() != null
                    && kafkaMessageDTO.getMdc().getProcessStageStep().equals("END")
                    && kafkaMessageDTO.getTimestamp() != null && kafkaMessageDTO.getOperationTook() != null) {

                BuildStageRecord buildStageRecord = new BuildStageRecord();
                buildStageRecord.setDuration(kafkaMessageDTO.getOperationTook());
                buildStageRecord.setTimestamp(kafkaMessageDTO.getTimestamp());

                buildStageRecord.setBuildStage(kafkaMessageDTO.getMdc().getProcessStageName());
                buildStageRecord.setBuildId(buildId(kafkaMessageDTO.getMdc().getProcessContext()));
                return Optional.of(buildStageRecord);
            } else {
                return Optional.empty();
            }

        } catch (IOException e) {
            errCounter.increment();
            throw new BuildStageRecordMapperException(e);
        }
    }

    public String toJsonString(KafkaMessageDTO kafkaMessageDTO) throws BuildStageRecordMapperException {

        try {
            return mapper.writeValueAsString(kafkaMessageDTO);
        } catch (IOException e) {
            errCounter.increment();
            throw new BuildStageRecordMapperException(e);
        }
    }

    /**
     * The processContext format is 'build-<build-id>'
     *
     * As such, we extract the build-id from the process context. If the process context does not start with 'build-',
     * then just return the process context as the build id.
     *
     * @param processContext
     * @return build id
     */
    private String buildId(String processContext) {

        if (processContext.startsWith("build-")) {
            return processContext.replace("build-", "");
        } else {
            return processContext;
        }
    }
}
