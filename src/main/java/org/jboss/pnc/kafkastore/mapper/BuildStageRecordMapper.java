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
import lombok.extern.slf4j.Slf4j;
import org.jboss.pnc.kafkastore.dto.ingest.KafkaMessageDTO;
import org.jboss.pnc.kafkastore.model.BuildStageRecord;

import javax.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.util.Optional;

@ApplicationScoped
@Slf4j
public class BuildStageRecordMapper {

    ObjectMapper mapper = new ObjectMapper().findAndRegisterModules()
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, false);

    public Optional<BuildStageRecord> mapKafkaMsgToBuildStageRecord(String jsonString)
            throws BuildStageRecordMapperException {

        try {
            KafkaMessageDTO kafkaMessageDTO = mapper.readValue(jsonString, KafkaMessageDTO.class);

            if (kafkaMessageDTO.getLoggerName() != null
                    && kafkaMessageDTO.getLoggerName().equals("org.jboss.pnc._userlog_.process-stage-update")
                    && kafkaMessageDTO.getMdc().getProcessStageStep().equals("END")) {

                BuildStageRecord buildStageRecord = new BuildStageRecord();
                buildStageRecord.setDuration(kafkaMessageDTO.getOperationTook());
                buildStageRecord.setTimestamp(kafkaMessageDTO.getTimestamp());
                if (kafkaMessageDTO.getMdc() != null) {
                    buildStageRecord.setBuildStage(kafkaMessageDTO.getMdc().getProcessStageName());
                    buildStageRecord.setBuildId(kafkaMessageDTO.getMdc().getProcessContext());
                    return Optional.of(buildStageRecord);
                } else {
                    return Optional.empty();
                }
            } else {
                return Optional.empty();
            }

        } catch (IOException e) {
            throw new BuildStageRecordMapperException(e);
        }
    }

    public String toJsonString(KafkaMessageDTO kafkaMessageDTO) throws BuildStageRecordMapperException {

        try {
            return mapper.writeValueAsString(kafkaMessageDTO);
        } catch (IOException e) {
            throw new BuildStageRecordMapperException(e);
        }
    }

}
