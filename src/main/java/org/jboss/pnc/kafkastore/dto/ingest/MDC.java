/*
 * SPDX-FileCopyrightText: Copyright © 2019 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.kafkastore.dto.ingest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class MDC {

    String processContext;
    String requestContext;
    String processContextVariant;

    @JsonProperty("process_stage_name")
    String processStageName;

    @JsonProperty("process_stage_step")
    String processStageStep;

    String traceId;
    String spanId;
    String parentId;
}
