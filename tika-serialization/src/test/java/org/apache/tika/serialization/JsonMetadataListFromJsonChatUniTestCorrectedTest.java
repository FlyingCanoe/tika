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
package org.apache.tika.serialization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.databind.JsonMappingException;
import org.junit.jupiter.api.Test;

import org.apache.tika.metadata.Metadata;

/**
 * Corrected from ChatUniTest's JsonMetadataList_fromJson_5_0_Test proposal.
 */
public class JsonMetadataListFromJsonChatUniTestCorrectedTest {

    @Test
    public void testGetStreamReadConstraintsReturnsConfiguredConstraints() {
        StreamReadConstraints originalConstraints = JsonMetadataList.getStreamReadConstraints();
        StreamReadConstraints configuredConstraints = StreamReadConstraints.builder()
                .maxNestingDepth(10)
                .maxStringLength(5)
                .maxNumberLength(500)
                .build();

        try {
            JsonMetadataList.setStreamReadConstraints(configuredConstraints);

            assertEquals(configuredConstraints, JsonMetadataList.getStreamReadConstraints());
        } finally {
            JsonMetadataList.setStreamReadConstraints(originalConstraints);
        }
    }

    @Test
    public void testFromJsonUsesUpdatedStreamReadConstraints() {
        StreamReadConstraints originalConstraints = JsonMetadataList.getStreamReadConstraints();
        StreamReadConstraints shortStringConstraints = StreamReadConstraints.builder()
                .maxNestingDepth(10)
                .maxStringLength(5)
                .maxNumberLength(500)
                .build();

        try {
            JsonMetadataList.setStreamReadConstraints(shortStringConstraints);

            JsonMappingException exception = assertThrows(JsonMappingException.class,
                    () -> JsonMetadataList.fromJson(new StringReader("[{\"key\":\"123456\"}]")));
            assertTrue(exception.getCause() instanceof com.fasterxml.jackson.core.exc.StreamConstraintsException);
        } finally {
            JsonMetadataList.setStreamReadConstraints(originalConstraints);
        }
    }

    @Test
    public void testFromJsonWithNullReader() throws Exception {
        assertNull(JsonMetadataList.fromJson(null));
    }

    @Test
    public void testToJsonRoundTrip() throws Exception {
        Metadata metadata = new Metadata();
        metadata.set("key", "value");
        List<Metadata> expected = List.of(metadata);
        StringWriter writer = new StringWriter();

        JsonMetadataList.setPrettyPrinting(false);
        JsonMetadataList.toJson(expected, writer);

        assertEquals(expected, JsonMetadataList.fromJson(new StringReader(writer.toString())));
    }

    @Test
    public void testToJsonPrettyPrintsWhenRequested() throws Exception {
        Metadata metadata = new Metadata();
        metadata.set("key", "value");
        StringWriter writer = new StringWriter();

        JsonMetadataList.toJson(List.of(metadata), writer, true);

        assertTrue(writer.toString().contains("\n"));
    }
}
