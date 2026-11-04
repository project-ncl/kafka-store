/*
 * SPDX-FileCopyrightText: Copyright © 2019 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.kafkastore.dto.rest;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

public class BuildMetricDTO {

    @Getter
    String name;

    @Getter
    List<Long> data = new ArrayList<>();

    public BuildMetricDTO(String name) {
        this.name = name;
    }

    public void addData(Long duration) {
        data.add(duration);
    }
}
