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
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

public class StringUtilsRepeatTest {

    @Test
    public void testRepeat_nullString() {
        String result = StringUtils.repeat(null, 5);
        assertNull(result);
    }

    @Test
    public void testRepeat_zeroRepeat() {
        String result = StringUtils.repeat("abc", 0);
        assertEquals("", result);
    }

    @Test
    public void testRepeat_oneRepeat() {
        String result = StringUtils.repeat("abc", 1);
        assertEquals("abc", result);
    }

    @Test
    public void testRepeat_positiveRepeat() {
        String result = StringUtils.repeat("abc", 3);
        assertEquals("abcabcabc", result);
    }

    @Test
    public void testRepeat_charRepeat() {
        String result = StringUtils.repeat('a', 5);
        assertEquals("aaaaa", result);
    }

    @Test
    public void testRepeat_charZeroRepeat() {
        String result = StringUtils.repeat('a', 0);
        assertEquals("", result);
    }

    @Test
    public void testRepeat_charOneRepeat() {
        String result = StringUtils.repeat('a', 1);
        assertEquals("a", result);
    }

    @Test
    public void testRepeat_charPositiveRepeat() {
        String result = StringUtils.repeat('a', 3);
        assertEquals("aaa", result);
    }
}
