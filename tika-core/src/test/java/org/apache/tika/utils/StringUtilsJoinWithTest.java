/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tika.utils;


import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class StringUtilsJoinWithTest {

    private List<String> lines;

    @BeforeEach
    public void setUp() {
        lines = Arrays.asList("line1", "line2", "line3");
    }

    @Test
    public void testJoinWith_EmptyList() {
        assertEquals("", StringUtils.joinWith(",", List.of()));
    }

    @Test
    public void testJoinWith_SingleElement() {
        assertEquals("line1", StringUtils.joinWith(",", List.of("line1")));
    }

    @Test
    public void testJoinWith_MultipleElements() {
        assertEquals("line1,line2,line3", StringUtils.joinWith(",", lines));
    }
}
