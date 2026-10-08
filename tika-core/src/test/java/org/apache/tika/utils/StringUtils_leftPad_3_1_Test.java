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

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.*;



public class StringUtils_leftPad_3_1_Test {

    @Test
    public void testLeftPadNullString() {
        String result = StringUtils.leftPad(null, 5, 'x');
        assertNull(result);
    }

    @Test
    public void testLeftPadEmptyString() {
        String result = StringUtils.leftPad("", 5, 'x');
        assertEquals("xxxxx", result);
    }

    @Test
    public void testLeftPadStringWithNoPadding() {
        String result = StringUtils.leftPad("hello", 5, 'x');
        assertEquals("hello", result);
    }

    @Test
    public void testLeftPadStringWithPadding() {
        String result = StringUtils.leftPad("hello", 10, 'x');
        assertEquals(result.length(), 10);
        assertEquals("xxxxxhello", result);
    }

    @Test
    public void testLeftPadStringWithLargePadding() {
        String result = StringUtils.leftPad("hello", 20, 'x');
        assertEquals(result.length(), 20);
        assertEquals("xxxxxxxxxxxxxxxhello", result);
    }

    @Test
    public void testLeftPadStringWithLargePaddingChar() {
        String result = StringUtils.leftPad("hello", 20, 'a');
        assertEquals(result.length(), 20);
        assertEquals("aaaaaaaaaaaaaaahello", result);
    }

    @Test
    public void testLeftPadStringWithEmptyPadding() {
        String result = StringUtils.leftPad("hello", 10, " ");
        assertEquals("     hello", result);
    }

    @Test
    public void testLeftPadStringWithLargePaddingString() {
        String result = StringUtils.leftPad("hello", 20, "world");
        assertEquals(result.length(), 20);
        assertEquals("worldworldworldhello", result);
    }

    @Test
    public void testLeftPadStringWithLargePaddingStringChar() {
        String result = StringUtils.leftPad("hello", 20, "ab");
        assertEquals("abababababababahello", result);
    }
}
