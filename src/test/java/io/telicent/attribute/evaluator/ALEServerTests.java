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
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.sys.JenaSystem;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static io.telicent.attribute.utils.URLRequests.postEvalRequest;
import static io.telicent.attribute.utils.URLRequests.postRequest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class ALEServerTests {
    private static final String PORT_PARAMETER = "--port";
    private static final String PORT = "0";
    private static final String STORE_PARAMETER = "--store";
    private static final String CONFIG_FILE_URI = "file:src/test/resources/sample_attributes.ttl";
    private static final String[] WORKING_CONFIG = {STORE_PARAMETER, CONFIG_FILE_URI, PORT_PARAMETER, PORT};
    private static final String USER_PARAMETER = "--userEndpoint";
    private static final String USER_URL = "http://userEndpoint/";
    private static final String HIERARCHY_PARAMETER = "--hierarchyEndpoint";
    private static final String HIERARCHY_URL = "http://hierarchyEndpoint/";
    private static final String STORE_URL = "http://storeUrl/";
    private static final String[] ALTERNATE_WORKING_CONFIG = {STORE_PARAMETER, STORE_URL, USER_PARAMETER, USER_URL, HIERARCHY_PARAMETER, HIERARCHY_URL, PORT_PARAMETER, PORT};
    private static String url;

    @BeforeAll
    public static void setupClass() {
        JenaSystem.init();
        ALEServer server = new ALEServer(WORKING_CONFIG);
        server.mainRun();
        url = server.getURL();
    }

    private static Stream<Arguments> populateExpressions() {
        return Stream.of(
                Arguments.of(
                        "clearance=secret,nationality=GBR&(deployed_organisation=Telicent||deployed_organisation=TOG)",
                        true,
                        "Expression list with ampersand and (OR combination, only one correct)"
                ),
                Arguments.of(
                        "clearance=secret,nationality=GBR&(deployed_organisation=TOG||deployed_organisation=UN)",
                        true,
                        "Expression list with ampersand and (OR combination, other one correct)"
                ),
                Arguments.of(
                        "clearance=secret,nationality=GBR&(deployed_organisation=Telicent||deployed_organisation=TOG)",
                        true,
                        "Expression without surrounding quotes and ampersand and (or combination)"
                ),
                Arguments.of(
                        "clearance=secret,nationality=GBR,(deployed_organisation=Telicent||deployed_organisation=TOG)",
                        true,
                        "Expression without surrounding quotes and commas and (or combination)"
                ),
                Arguments.of(
                        "clearance=secret,nationality=GBR,(email='u3@telicent.io'||deployed_organisation=TOG)",
                        true,
                        "Expression list with commas and (email OR combination)"
                ),
                Arguments.of(
                        "(email='u3@telicent.io'&&deployed_organisation=TOG)",
                        true,
                        "Expression list with commas and (email OR combination)"
                ),
                Arguments.of(
                        "(email='u3@telicent.io'||deployed_organisation=TOG)",
                        true,
                        "Email OR combination, both correct"
                ),
                Arguments.of(
                        "(email='u3@telicent.io'||deployed_organisation=NATO)",
                        true,
                        "Email OR combination, only email correct"
                ),
                Arguments.of(
                        "(email='u2@telicent.io'||deployed_organisation=TOG)",
                        true,
                        "Email OR combination other correct"
                ),
                Arguments.of(
                        "email='u3@telicent.io'",
                        true,
                        "Solo email address (correct)"
                ),
                Arguments.of(
                        "email='u2@telicent.io'",
                        false,
                        "Solo email address (wrong)"
                ),
                Arguments.of(
                        "nationality=GBR&(deployed_organisation=Telicent||deployed_organisation=BBC)||(clearance=secret||clearance=top-secret)",
                        true,
                        "Expression list with ampersand and (OR combination, only one correct) OR (OR combination)"
                ),
                Arguments.of(
                        "nationality=GBR&(deployed_organisation=Telicent||deployed_organisation=BBC)||(nationality=USA||clearance=top-secret)",
                        false,
                        "Expression list with ampersand and (OR combination, only one correct) OR (OR combination with one correct)"
                ),
                Arguments.of("clearance=secret&nationality=GBR&(deployed_organisation=Telicent || deployed_organisation=TOG)",
                        true,
                        "Issue 8 example 1 w/o quotes"
                ),
                Arguments.of("clearance=secret,nationality=GBR,(deployed_organisation=Telicent || deployed_organisation=TOG)",
                        true,
                        "Issue 8 example 2 w/o quotes"
                )
        );
    }

    static Stream<Arguments> populateBadURLRequests() {
        return Stream.of(
                Arguments.of(url + "?user=u1",
                             "No label"
                ),
                Arguments.of(url + "?label=l1",
                             "No user"
                ),
                Arguments.of(url + "?test=test",
                             "No user or label"
                ),
                Arguments.of(url + "?user=u1&label=l1&user=u2",
                             "More than one user"
                ),
                Arguments.of(url + "?user=u1&label=l1&label=l2",
                             "More than one label"
                ),
                Arguments.of(url + "?user=u1&label=email=u1@telicent.io",
                             "Email address without quotes"
                )
        );
    }

    @ParameterizedTest(name = "{2}")
    @MethodSource("populateExpressions")
    void test_expressionListCombos(String label, boolean expectedResult, String description)
            throws JsonProcessingException {
        // given
        // when
        String actual = postEvalRequest(url, "u3", label);
        // then
        TestEvaluationResult result = new ObjectMapper().readValue(actual, TestEvaluationResult.class);
        boolean actualResult = result.result;
        assertEquals(expectedResult, actualResult, description);
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource("populateBadURLRequests")
    void test_invalidParameters(String requestURL, String description) {
        // given an invalid request
        // when POST'ed
        // then 400 / HTTP exception.
        expectHttpException(400, () -> postRequest(requestURL, "", "")) ;
    }

    private static void expectHttpException(int expectedStatusCode, Runnable action) {
        try {
            action.run();
            fail("Expected HttpException " + expectedStatusCode);
        } catch (HttpException ex) {
            if (expectedStatusCode > 0) {
                assertEquals(expectedStatusCode, ex.getStatusCode(), ex.getMessage() + ":: ");
            }
        }
    }

    @Test
    void test_invalidExpressions_noUserAttributes() throws JsonProcessingException {
        // given
        String requestURL = url + "?user=&label=";
        boolean expectedResult = false;
        String description = "Invalid Expression";
        // when
        String actual = postRequest(requestURL, "", "");
        // then
        TestEvaluationResult result = new ObjectMapper().readValue(actual, TestEvaluationResult.class);
        boolean actualResult = result.result;
        assertEquals(expectedResult, actualResult, description);
    }

    @Test
    public void test_AlternateConfig() {
        // given
        ALEServer altServer = new ALEServer(ALTERNATE_WORKING_CONFIG);
        // when
        altServer.process();
        // then
        assertEquals(USER_URL, altServer.getLookupUserEndpoint());
        assertEquals(HIERARCHY_URL, altServer.getLookupHierarchyEndpoint());
    }
}
