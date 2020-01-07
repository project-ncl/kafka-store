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
package org.jboss.pnc.kafkastore.common;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Set that preserve relative order of items when a new list is added.
 *
 * Assume the first list added is ["a", "d", "f"]
 *
 * and the second list added is ["b", "d", "f"]
 *
 * Then the accepted order of the sorted set is:
 *
 * - ["a", "b", "d", "f"]  OR
 * - ["b", "a", "d", "f"]
 *
 * Items before the "d" in both lists will always be before the "d" in the final sorted list
 */
public class SortedSetOrder {

    ArrayList<String> sorted = new ArrayList<>();
    HashSet<String> cache = new HashSet<>();

    public SortedSetOrder addList(List<String> toAdd) {

        if (sorted.isEmpty()) {
            sorted.addAll(toAdd);
        } else {
            ArrayList<String> finalSolution = new ArrayList<>();
            int copiedIndex = -1;

            for (int i = 0; i < sorted.size(); i++) {

                String item = sorted.get(i);

                int index = toAdd.indexOf(item);
                boolean added = false;

                while (copiedIndex < index) {
                    copiedIndex++;
                    finalSolution.add(toAdd.get(copiedIndex));
                    added = true;
                }

                if (!added) {
                    finalSolution.add(sorted.get(i));
                }
            }
            // add the rest of un-copied data from the secondary list
            while (copiedIndex < toAdd.size() - 1) {
                copiedIndex++;
                finalSolution.add(toAdd.get(copiedIndex));
            }

            sorted = finalSolution;
        }

        return this;
    }

    public List<String> getSorted() {
        return ImmutableList.copyOf(sorted);
    }
}
