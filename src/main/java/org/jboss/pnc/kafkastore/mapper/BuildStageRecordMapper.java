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
import io.prometheus.client.Counter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.metrics.annotation.Gauge;
import org.jboss.pnc.kafkastore.dto.ingest.KafkaMessageDTO;
import org.jboss.pnc.kafkastore.model.BuildStageRecord;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.Optional;

@ApplicationScoped
@Timed
@Slf4j
public class BuildStageRecordMapper {

    static final Counter exceptionsTotal = Counter.build()
            .name("BuildStageRecordMapper_Exceptions_Total")
            .help("Errors and Warnings counting metric")
            .labelNames("severity")
            .register();

    ObjectMapper mapper = new ObjectMapper().findAndRegisterModules()
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, false);

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
            exceptionsTotal.labels("error").inc();
            throw new BuildStageRecordMapperException(e);
        }
    }

    public String toJsonString(KafkaMessageDTO kafkaMessageDTO) throws BuildStageRecordMapperException {

        try {
            return mapper.writeValueAsString(kafkaMessageDTO);
        } catch (IOException e) {
            exceptionsTotal.labels("error").inc();
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

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Gauge(name = "BuildStageRecordMapper_Err_Count", unit = MetricUnits.NONE, description = "Errors count")
    public int showCurrentErrCount() {
        return (int) exceptionsTotal.labels("error").get();
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Gauge(name = "BuildStageRecordMapper_Warn_Count", unit = MetricUnits.NONE, description = "Warnings count")
    public int showCurrentWarnCount() {
        return (int) exceptionsTotal.labels("warning").get();
    }
}
