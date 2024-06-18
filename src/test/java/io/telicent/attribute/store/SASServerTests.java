/*
 * Copyright (C) Telicent Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package io.telicent.attribute.store;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.telicent.attribute.utils.URLRequests.getRequest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

class SASServerTests {
    private static final String PORT_PARAMETER = "--port";
    private static final String PORT = "12349";
    private static final String STORE_PARAMETER = "--store";
    private static final String CONFIG_FILE_URI = "file:src/test/resources/sample_attributes.ttl";
    private static final String[] WORKING_CONFIG = {STORE_PARAMETER, CONFIG_FILE_URI, PORT_PARAMETER, PORT};
    private static String userUrl;
    private static String hierarchyUrl;

    @BeforeAll
    public static void setupClass() {
        SASServer server = new SASServer(WORKING_CONFIG);
        server.mainRun();
        String baseURL = server.getURL();
        userUrl = baseURL + "/users/lookup/%s";
        hierarchyUrl = baseURL + "/hierarchies/lookup/%s";
    }

    @Test
    void test_happyPath_userRequest() throws JsonProcessingException {
        // given
        List<String> expected =
                List.of("credentials=ordinary-degree", "employee", "engineer", "email=\"e1@telicent.io\"",
                        "status=public");
        // when
        String result = getRequest(userUrl, "employee1");
        // then
        TestUserAttributesResult actual = new ObjectMapper().readValue(result, TestUserAttributesResult.class);
        assertIterableEquals(expected, actual.attributes);
    }

    @Test
    void test_happyPath_hierarchyRequest() throws JsonProcessingException {
        // given
        List<String> expected = List.of("hnc", "hnd", "ordinary-degree", "honours-degree", "phd");
        // when
        String result = getRequest(hierarchyUrl, "credentials");
        // then
        TestHierarchyResult actual = new ObjectMapper().readValue(result, TestHierarchyResult.class);
        assertIterableEquals(expected, actual.tiers);
    }

    @Test
    void test_commandName() {
        // given
        String[] args = {};
        SASServer server = new SASServer(args);
        // when
        String actualCommandName = server.getCommandName();
        // then
        String expectedName = "SASServer";
        assertEquals(expectedName, actualCommandName);
    }

    @Test
    void test_summary() {
        // given
        String[] args = {};
        SASServer server = new SASServer(args);
        // when
        String actualSummary = server.getSummary();
        // then
        String expectedSummary = "SASServer --store=<file:filename> [--port]";
        assertEquals(expectedSummary, actualSummary);
    }
}
