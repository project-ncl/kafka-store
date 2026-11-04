/*
 * SPDX-FileCopyrightText: Copyright © 2019 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.kafkastore.facade;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;

import org.jboss.pnc.kafkastore.dto.rest.BuildStageRecordDTO;
import org.jboss.pnc.kafkastore.dto.rest.PagedBuildStageRecordDTO;
import org.jboss.pnc.kafkastore.model.BuildStageRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import io.micrometer.core.annotation.Timed;
import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;

@ApplicationScoped
public class BuildStageRecordFetcher {

    private static final Logger log = LoggerFactory.getLogger(BuildStageRecordFetcher.class);

    private ObjectMapper mapper = new ObjectMapper().findAndRegisterModules()
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, false)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Timed
    @WithSpan()
    public PagedBuildStageRecordDTO findBuildStageRecordNewerThan(
            @SpanAttribute(value = "lastUpdateTime") Instant lastUpdateTime,
            @SpanAttribute(value = "pageIndex") Integer pageIndex,
            @SpanAttribute(value = "pageSize") Integer pageSize) {

        Long totalHits = BuildStageRecord.countNewerThan(lastUpdateTime);
        Integer totalPages = BuildStageRecord.countPagesNewerThan(lastUpdateTime, pageSize);
        List<BuildStageRecord> pagedBuildStageRecordList = BuildStageRecord
                .findNewerThan(lastUpdateTime, pageIndex, pageSize);
        List<BuildStageRecordDTO> content = Optional.ofNullable(pagedBuildStageRecordList)
                .orElse(Collections.emptyList())
                .stream()
                .map(buildStageRecord -> {
                    try {
                        return mapper.convertValue(buildStageRecord, BuildStageRecordDTO.class);
                    } catch (IllegalArgumentException e) {
                        log.error("Error while converting build stage record {} to DTO!", buildStageRecord, e);
                    }
                    return null;
                })
                .collect(Collectors.toList());

        PagedBuildStageRecordDTO response = PagedBuildStageRecordDTO.builder()
                .pageIndex(pageIndex)
                .pageSize(pageSize)
                .totalHits(totalHits)
                .totalPages(totalPages)
                .content(content)
                .build();
        return response;
    }
}
