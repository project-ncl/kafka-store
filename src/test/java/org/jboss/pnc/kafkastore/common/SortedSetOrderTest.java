/*
 * SPDX-FileCopyrightText: Copyright © 2019 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.kafkastore.common;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;

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