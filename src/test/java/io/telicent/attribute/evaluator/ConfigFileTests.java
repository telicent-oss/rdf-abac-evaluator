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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.telicent.attribute.utils.URLRequests.postEvalRequest;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigFileTests {
    private static final String PORT_PARAMETER = "--port";
    private static final String PORT = "12347";
    private static final String CONFIG_FILE_PARAMETER = "--config";
    private static final String CONFIG_FILE_URI = "file:src/test/resources/test_config.json";
    private static final String URL = "http://localhost:" + PORT + "/eval";

    private static final String[] WORKING_CONFIG = {CONFIG_FILE_PARAMETER, CONFIG_FILE_URI, PORT_PARAMETER, PORT};

    @BeforeAll
    public static void setupClass() {
        ALEServer.main(WORKING_CONFIG);
    }

    @Test
    void test_happyPath_directMatch() throws JsonProcessingException {
        // given
        // when
        String actual = postEvalRequest(URL, "employee1", "email = 'e1@telicent.io'");
        // then
        TestEvaluationResult result = new ObjectMapper().readValue(actual, TestEvaluationResult.class);
        assertTrue(result.result);
    }

    @Test
    void test_happyPath_hierarchyMatch() throws JsonProcessingException {
        // given
        // when
        String actual = postEvalRequest(URL, "employee1", "credentials = hnd");

        // then
        TestEvaluationResult result = new ObjectMapper().readValue(actual, TestEvaluationResult.class);
        assertTrue(result.result);
    }


    @Test
    void test_hierarchyMisMatch() throws JsonProcessingException {
        // given
        // when
        String actual = postEvalRequest(URL, "employee1", "credentials = phd");

        // then
        TestEvaluationResult result = new ObjectMapper().readValue(actual, TestEvaluationResult.class);
        assertFalse(result.result);
    }


    @Test
    void test_userMisMatch() throws JsonProcessingException {
        // given
        // when
        String actual = postEvalRequest(URL, "employee3", "credentials = phd");
        // then
        TestEvaluationResult result = new ObjectMapper().readValue(actual, TestEvaluationResult.class);
        assertFalse(result.result);
    }

    @Test
    void test_labelsMissing() throws JsonProcessingException {
        // given
        // when
        String actual = postEvalRequest(URL, "user1", "credentials = phd");
        // then
        TestEvaluationResult result = new ObjectMapper().readValue(actual, TestEvaluationResult.class);
        assertFalse(result.result);
    }

    @Test
    void test_labelMisMatch() throws JsonProcessingException {
        // given
        // when
        String actual = postEvalRequest(URL, "user1", "clearance = normal");
        // then
        TestEvaluationResult result = new ObjectMapper().readValue(actual, TestEvaluationResult.class);
        assertFalse(result.result);
    }
}
