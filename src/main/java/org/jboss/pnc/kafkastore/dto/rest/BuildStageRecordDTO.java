/*
 * SPDX-FileCopyrightText: Copyright © 2019 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.kafkastore.dto.rest;

import java.time.Instant;

import lombok.Data;

@Data
public class BuildStageRecordDTO {

    String buildStage;
    long duration;
    String buildId;
    Instant timestamp;
    Instant lastUpdateTime;
    String processContextVariant;

}
