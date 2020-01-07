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
package org.jboss.pnc.kafkastore.facade;

import org.jboss.pnc.kafkastore.common.SortedSetOrder;
import org.jboss.pnc.kafkastore.dto.rest.BuildIdDTO;
import org.jboss.pnc.kafkastore.dto.rest.BuildMetricDTO;
import org.jboss.pnc.kafkastore.model.BuildStageRecord;

import javax.enterprise.context.ApplicationScoped;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

@ApplicationScoped
public class BuildMetricsFetcher {

    public List<BuildMetricDTO> getMetricForBuildIds(BuildIdDTO buildIdDTO) {

        List<BuildMetricDTO> response = new LinkedList<>();

        // Step 1: get final list of metrics and populate cache to not query the database again
        SortedSetOrder sortedSet = new SortedSetOrder();

        LinkedHashMap<String, Long> cache = new LinkedHashMap<>();

        for (String id : buildIdDTO.getBuildIds()) {

            List<BuildStageRecord> buildStageRecordList = BuildStageRecord.getForBuildId(id);
            List<String> metric = new LinkedList<>();

            for (BuildStageRecord buildStageRecord : buildStageRecordList) {
                metric.add(buildStageRecord.getBuildStage());
                cache.put(getKeyForCache(id, buildStageRecord.getBuildStage()), buildStageRecord.getDuration());
            }
            sortedSet.addList(metric);
        }

        // Step 2: from the list of metrics, populate the final list
        for (String metric : sortedSet.getSorted()) {

            BuildMetricDTO buildMetricDTO = new BuildMetricDTO(metric);

            for (String id : buildIdDTO.getBuildIds()) {
                buildMetricDTO.addData(cache.getOrDefault(getKeyForCache(id, metric), null));
            }

            response.add(buildMetricDTO);
        }

        return response;
    }

    private static String getKeyForCache(final String buildId, final String buildStage) {
        return buildId + ":" + buildStage;
    }
}
