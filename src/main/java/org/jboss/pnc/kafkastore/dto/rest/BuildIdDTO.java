/*
 * SPDX-FileCopyrightText: Copyright © 2019 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.kafkastore.dto.rest;

import java.util.List;

import lombok.Data;

@Data
public class BuildIdDTO {
    List<String> buildIds;
}
