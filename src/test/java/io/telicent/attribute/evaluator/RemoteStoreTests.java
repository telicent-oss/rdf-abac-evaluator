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
import com.github.tomakehurst.wiremock.WireMockServer;
import org.apache.jena.sys.JenaSystem;
import org.junit.jupiter.api.*;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static io.telicent.attribute.utils.URLRequests.postEvalRequest;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RemoteStoreTests {
    private static WireMockServer wireMockServer;
    private static String URL;

    @BeforeAll
    public static void setupClass() {
        JenaSystem.init();
        wireMockServer = new WireMockServer(wireMockConfig().dynamicPort());
        wireMockServer.start();
        String url = "http://localhost:" + wireMockServer.port();
        String[] args = {"--store", url, "--port", "0"};
        ALEServer server = new ALEServer(args);
        server.mainRun();
        URL = server.getURL();
    }

    @AfterAll
    public static void cleanUp() {
        wireMockServer.stop();
    }

    @BeforeEach
    void setup() {
        wireMockServer.stubFor(get(urlEqualTo("/users/lookup/user1"))
                                         .willReturn(aResponse()
                                                             .withBody("""
                                                                               {
                                                                                 "attributes" : [
                                                                                     "credentials=ordinary-degree" ,
                                                                                     "employee" ,
                                                                                     "engineer"
                                                                                   ]
                                                                               }""")
                                         ));
        wireMockServer.stubFor(get(urlEqualTo("/hierarchies/lookup/credentials"))
                                         .willReturn(aResponse()
                                                             .withBody("""
                                                                               {
                                                                                 "tiers" : [
                                                                                     "hnc" ,
                                                                                     "hnd" ,
                                                                                     "ordinary-degree" ,
                                                                                     "honours-degree" ,
                                                                                     "phd"
                                                                                   ]
                                                                               }""")));

    }

    @AfterEach
    void tidy() {
        wireMockServer.resetAll();
    }

    @Test
    void test_happyPath_directMatch() throws JsonProcessingException {
        // given
        // when
        String actual = postEvalRequest(URL, "user1", "credentials = ordinary-degree");

        // then
        TestEvaluationResult result = new ObjectMapper().readValue(actual, TestEvaluationResult.class);
        assertTrue(result.result);
        wireMockServer.verify(getRequestedFor(urlEqualTo("/users/lookup/user1")));
        wireMockServer.verify(exactly(0), getRequestedFor(urlEqualTo("/hierarchies/lookup/credentials")));
    }

    @Test
    void test_happyPath_hierarchyMatch() throws JsonProcessingException {
        // given
        // when
        String actual = postEvalRequest(URL, "user1", "credentials = hnd");

        // then
        TestEvaluationResult result = new ObjectMapper().readValue(actual, TestEvaluationResult.class);
        assertTrue(result.result);
        wireMockServer.verify(getRequestedFor(urlEqualTo("/users/lookup/user1")));
        wireMockServer.verify(getRequestedFor(urlEqualTo("/hierarchies/lookup/credentials")));
    }


    @Test
    void test_hierarchyMisMatch() throws JsonProcessingException {
        // given
        // when
        String actual = postEvalRequest(URL, "user1", "credentials = phd");

        // then
        TestEvaluationResult result = new ObjectMapper().readValue(actual, TestEvaluationResult.class);
        assertFalse(result.result);
        wireMockServer.verify(getRequestedFor(urlEqualTo("/users/lookup/user1")));
        wireMockServer.verify(getRequestedFor(urlEqualTo("/hierarchies/lookup/credentials")));
    }


    @Test
    void test_userMisMatch() throws JsonProcessingException {
        // given
        wireMockServer.stubFor(get(urlEqualTo("/users/lookup/user2"))
                                         .willReturn(aResponse()
                                                             .withBody("""
                                                                               {
                                                                                  "code" : 404 ,
                                                                                  "message" : "User not found"
                                                                                }""")
                                         ));

        // when
        String actual = postEvalRequest(URL, "user2", "credentials = phd");

        // then
        TestEvaluationResult result = new ObjectMapper().readValue(actual, TestEvaluationResult.class);
        assertFalse(result.result);
        wireMockServer.verify(getRequestedFor(urlEqualTo("/users/lookup/user2")));
        wireMockServer.verify(exactly(0), getRequestedFor(urlEqualTo("/hierarchies/lookup/credentials")));
    }

    @Test
    void test_labelsMissing() throws JsonProcessingException {
        // given
        wireMockServer.stubFor(get(urlEqualTo("/hierarchies/lookup/credentials"))
                                         .willReturn(aResponse()
                                                             .withBody("""
                                                                               {
                                                                                  "code" : 404 ,
                                                                                  "message" : "Hierarchy not found"
                                                                                }""")));

        // when
        String actual = postEvalRequest(URL, "user1", "credentials = phd");

        // then
        TestEvaluationResult result = new ObjectMapper().readValue(actual, TestEvaluationResult.class);
        assertFalse(result.result);
        wireMockServer.verify(getRequestedFor(urlEqualTo("/users/lookup/user1")));
        wireMockServer.verify(getRequestedFor(urlEqualTo("/hierarchies/lookup/credentials")));
    }


    @Test
    void test_labelMisMatch() throws JsonProcessingException {
        // given
        // when
        String actual = postEvalRequest(URL, "user1", "clearance = normal");
        // then
        TestEvaluationResult result = new ObjectMapper().readValue(actual, TestEvaluationResult.class);
        assertFalse(result.result);
        wireMockServer.verify(getRequestedFor(urlEqualTo("/users/lookup/user1")));
        wireMockServer.verify(exactly(0), getRequestedFor(urlEqualTo("/hierarchies/lookup/credentials")));
        wireMockServer.verify(exactly(0), getRequestedFor(urlEqualTo("/hierarchies/lookup/clearance")));
    }


}
