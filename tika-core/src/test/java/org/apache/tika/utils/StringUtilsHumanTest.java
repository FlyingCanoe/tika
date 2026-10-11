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

import org.junit.jupiter.api.Test;



public class StringUtilsHumanTest {
    @Test
    public void testRepeat_emptyString() {
        // create a string that is not interned to test that
        // the function return the *same* string
        var input = new String("");
        var result =StringUtils.repeat(input, 10);
        // use == to test object identity, not equality
        assertSame(input, result);
    }

    @Test
    public void testRepeat_zeroRep() {
        var result =StringUtils.repeat("aa", 0);
        assertEquals("", result);
    }

    @Test
    public void testRepeat_singleCharString() {
        var result = StringUtils.repeat("a", 3);
        assertEquals(3, result.length());
        assertEquals("aaa", result);
    }

    @Test
    public  void testRepeat_singleCharStringOverPadLimit() {
        var result = StringUtils.repeat("a", StringUtils.PAD_LIMIT + 1);
        assertTrue(result.matches(String.format("a{%d}", StringUtils.PAD_LIMIT+1)));
    }

    @Test
    public  void testRepeat_twoChar() {
        var result = StringUtils.repeat("ab", 2);
        assertEquals("abab", result);
    }

    @Test
    public void testLeftPad_nullInput() {
        assertNull(StringUtils.leftPad(null, 0, ""));
    }

    @Test
    public void testLeftPad_nullPaddingStr() {
        var result =StringUtils.leftPad("a", 2, null);
        assertEquals(" a", result);
    }

    @Test
    public void testLeftPad_onePadding() {
        var input = "";
        var result =StringUtils.leftPad(input, 1, "b");
        assertEquals("b", result);
    }

    @Test
    public void testLeftPad_noPadding() {
        // create a string that is not interned to test that
        // the function return the *same* string
        var input = new String("a");
        var result =StringUtils.leftPad(input, 1, "bb");
        assertSame(input, result);
    }

    @Test
    public void testLeftPad_noCharPadding() {
        // create a string that is not interned to test that
        // the function return the *same* string
        var input = new String("a");
        var result =StringUtils.leftPad(input, 1, 'b');
        assertSame(input, result);
    }

    @Test
    public void testLeftPad_partialPadding() {
        var result =StringUtils.leftPad("a", 3, "123456");
        assertEquals("12a", result);
    }

    @Test
    public void testLeftPad_fullPadding() {
        var result =StringUtils.leftPad("a", 3, "12");
        assertEquals("12a", result);
    }

    @Test
    public void testLeftPad_padCharOverPaddingLimit() {
        var result =StringUtils.leftPad("b", StringUtils.PAD_LIMIT + 2, 'a');
        assertTrue(result.matches(String.format("a{%d}b", StringUtils.PAD_LIMIT + 1)));
    }

    @Test
    public void testLeftPad_padOverPaddingLimit() {
        var result =StringUtils.leftPad("b", StringUtils.PAD_LIMIT + 1, "a");
        assertTrue(result.matches(String.format("a{%d}b", StringUtils.PAD_LIMIT)));
    }
}
