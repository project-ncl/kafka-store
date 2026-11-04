/*
 * SPDX-FileCopyrightText: Copyright © 2019 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.kafkastore.model;

import java.time.Instant;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@Entity
@ToString
@Table(
        indexes = {
                @Index(name = "idx_build_ids", columnList = "buildid"),
                @Index(name = "idx_lastupdate_time", columnList = "lastupdatetime") },
        uniqueConstraints = @UniqueConstraint(
                name = "uq_buildId_stage_dur_tstamp",
                columnNames = { "buildid", "buildstage", "duration", "timestamp" }))
public class BuildStageRecord extends PanacheEntity {

    String buildStage;

    long duration;

    String buildId;

    String processContextVariant;

    Instant timestamp;

    @UpdateTimestamp
    Instant lastUpdateTime;

    public static List<BuildStageRecord> getForBuildId(String buildId) {
        return list("buildId", Sort.by("timestamp").ascending(), buildId);
    }

    public static List<BuildStageRecord> getForBuildId(int buildId) {
        return getForBuildId(String.valueOf(buildId));
    }

    public static List<BuildStageRecord> findNewerThan(Instant lastUpdate, Integer pageIndex, Integer pageSize) {
        return find(
                "lastUpdateTime > :lastUpdate",
                Sort.by("lastUpdateTime").ascending(),
                Parameters.with("lastUpdate", lastUpdate)).page(Page.of(pageIndex, pageSize)).list();
    }

    public static Long countNewerThan(Instant lastUpdate) {
        return find(
                "lastUpdateTime > :lastUpdate",
                Sort.by("lastUpdateTime").ascending(),
                Parameters.with("lastUpdate", lastUpdate)).count();
    }

    public static Integer countPagesNewerThan(Instant lastUpdate, Integer pageSize) {
        return find(
                "lastUpdateTime > :lastUpdate",
                Sort.by("lastUpdateTime").ascending(),
                Parameters.with("lastUpdate", lastUpdate)).page(Page.ofSize(pageSize)).pageCount();
    }
}
