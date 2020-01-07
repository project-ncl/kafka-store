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

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

public class SortedSetOrderTest {

    @Test
    public void testAddListOrderMaintained() {

        List<String> first = Lists.newArrayList("a", "d", "f");
        List<String> second = Lists.newArrayList("b", "d", "f");

        SortedSetOrder sortedSetOrder = new SortedSetOrder();
        sortedSetOrder.addList(first);
        sortedSetOrder.addList(second);

        List<String> result = sortedSetOrder.getSorted();

        assertThat(result.indexOf("a")).isLessThan(result.indexOf("d"));
        assertThat(result.indexOf("b")).isLessThan(result.indexOf("d"));
        assertThat(result.indexOf("d")).isLessThan(result.indexOf("f"));
    }
}