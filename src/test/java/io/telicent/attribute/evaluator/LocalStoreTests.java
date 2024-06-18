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
package io.telicent.attribute.evaluator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.jena.sys.JenaSystem;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.telicent.attribute.utils.URLRequests.postEvalRequest;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalStoreTests {
    private static final String PORT_PARAMETER = "--port";
    private static final String PORT = "0";
    private static final String STORE_PARAMETER = "--store";
    private static final String CONFIG_FILE_URI = "file:src/test/resources/sample_attributes.ttl";
    private static final String[] WORKING_CONFIG = {STORE_PARAMETER, CONFIG_FILE_URI, PORT_PARAMETER, PORT};
    private static String url;

    @BeforeAll
    public static void setupClass() {
        JenaSystem.init();
        ALEServer server = new ALEServer(WORKING_CONFIG);
        server.mainRun();
        url = server.getURL();
    }

    @Test
    void test_happyPath_directMatch() throws JsonProcessingException {
        // given
        // when
        String actual = postEvalRequest(url, "employee1", "credentials = ordinary-degree");
        // then
        TestEvaluationResult result = new ObjectMapper().readValue(actual, TestEvaluationResult.class);
        assertTrue(result.result);
    }

    @Test
    void test_happyPath_hierarchyMatch() throws JsonProcessingException {
        // given
        // when
        String actual = postEvalRequest(url, "employee1", "credentials = hnd");

        // then
        TestEvaluationResult result = new ObjectMapper().readValue(actual, TestEvaluationResult.class);
        assertTrue(result.result);
    }


    @Test
    void test_hierarchyMisMatch() throws JsonProcessingException {
        // given
        // when
        String actual = postEvalRequest(url, "employee1", "credentials = phd");

        // then
        TestEvaluationResult result = new ObjectMapper().readValue(actual, TestEvaluationResult.class);
        assertFalse(result.result);
    }


    @Test
    void test_userMisMatch() throws JsonProcessingException {
        // given
        // when
        String actual = postEvalRequest(url, "employee3", "credentials = phd");
        // then
        TestEvaluationResult result = new ObjectMapper().readValue(actual, TestEvaluationResult.class);
        assertFalse(result.result);
    }

    @Test
    void test_labelsMissing() throws JsonProcessingException {
        // given
        // when
        String actual = postEvalRequest(url, "user1", "credentials = phd");
        // then
        TestEvaluationResult result = new ObjectMapper().readValue(actual, TestEvaluationResult.class);
        assertFalse(result.result);
    }

    @Test
    void test_labelMisMatch() throws JsonProcessingException {
        // given
        // when
        String actual = postEvalRequest(url, "user1", "clearance = normal");
        // then
        TestEvaluationResult result = new ObjectMapper().readValue(actual, TestEvaluationResult.class);
        assertFalse(result.result);
    }
}
