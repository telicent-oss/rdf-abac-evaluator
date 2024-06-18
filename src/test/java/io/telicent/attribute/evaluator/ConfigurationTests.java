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

import org.apache.jena.atlas.json.JsonParseException;
import org.apache.jena.cmd.CmdException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConfigurationTests {

    @Test
    void test_invalidPort() {
        // given
        String badPort = "-500";
        String[] args = {"--port", badPort};
        // when
        ALEServer server = new ALEServer(args);
        // then
        assertThrowsExactly(CmdException.class, server::process, "port : bad port number: " + badPort);
    }

    @Test
    void test_invalidStoreURIPath() {
        // given
        String badURI = "/tmp/rubbish.ttl";
        String[] args = {"--store", badURI};
        // when
        ALEServer server = new ALEServer(args);
        // then
        assertThrowsExactly(CmdException.class, server::process, "Bad URI for attribute store: " + badURI);
    }

    @Test
    void test_invalidStoreURI() {
        // given
        String badURI = "!broken::@doesntwork";
        String[] args = {"--store", badURI};
        // when
        ALEServer server = new ALEServer(args);
        // then
        assertThrowsExactly(CmdException.class, server::process, "Bad syntax in URI for attribute store: " + badURI);
    }

    @Test
    void test_emptyConfig() {
        // given
        String[] args = {};
        // when
        ALEServer server = new ALEServer(args);
        // then
        assertThrowsExactly(CmdException.class, server::process, "Required: --attrstore or --config");
    }

    @Test
    void test_tooMuchConfig() {
        // given
        String[] args = {"--attrstore", "something", "--config", "else"};
        // when
        ALEServer server = new ALEServer(args);
        // then
        assertThrowsExactly(CmdException.class, server::process, "Required: --attrstore or --config (not both)");
    }

    @Test
    void test_commandName() {
        // given
        String[] args = {};
        ALEServer server = new ALEServer(args);
        // when
        String actualCommandName = server.getCommandName();
        // then
        String expectedName = "ALEServer";
        assertEquals(expectedName, actualCommandName);
    }

    @Test
    void test_summary() {
        // given
        String[] args = {};
        ALEServer server = new ALEServer(args);
        // when
        String actualSummary = server.getSummary();
        // then
        String expectedSummary = """
                ALEServer --store=[<file:filename>|<http:url>] ([--port]) ([--hierarchyEndpoint]) ([--userEndpoint]) (--[cache])\s
                 --config=<file:filename> ([--port])\s
                """;
        assertEquals(expectedSummary, actualSummary);
    }

    @Test
    void test_missingConfigFile() {
        // given
        String missingFile = "/tmp/doesnotexist";
        String[] args = {"--config", missingFile};
        // when
        ALEServer server = new ALEServer(args);
        // then
        assertThrowsExactly(CmdException.class, server::process, "File not found: " + missingFile);
    }

    @Test
    void test_emptyConfigFile() {
        // given
        String[] args = {"--config", "file:src/test/resources/empty_file.ttl"};
        // when
        ALEServer server = new ALEServer(args);
        // then
        assertThrowsExactly(JsonParseException.class, server::process);
    }

    @Test
    void test_blankConfigFile() {
        // given
        String[] args = {"--config", "file:src/test/resources/blank_json.ttl"};
        // when
        ALEServer server = new ALEServer(args);
        // then
        assertThrowsExactly(CmdException.class, server::process);
    }


    @Test
    void test_badFormatConfigFile() {
        // given
        String[] args = {"--config", "file:src/test/resources/bad_format.ttl"};
        // when
        ALEServer server = new ALEServer(args);
        // then
        assertThrowsExactly(CmdException.class, server::process);
    }

    @Test
    void test_invalidCachingEnabledFormatHandled() {
        // given
        String[] args =
                {"--cacheEnabled", "RUBBISH", "--cacheExpiryTime", "10 seconds", "--store", "http://some.url/"};
        // when
        ALEServer server = new ALEServer(args);
        // then
        assertDoesNotThrow(server::process);
    }

    @Test
    void test_invalidCachingExpiryTime() {
        // given
        String[] args = {"--cacheEnabled", "true", "--cacheExpiryTime", "10 seconds", "--store", "http://some.url/"};
        // when
        ALEServer server = new ALEServer(args);
        // then
        assertThrowsExactly(CmdException.class, server::process, "Text cannot be parsed to a Duration");
    }

    @Test
    void test_validConfig() {
        // given
        String[] args = {"--config", "file:src/test/resources/test_config_remote_cache.json"};
        // when
        ALEServer server = new ALEServer(args);
        // then
        assertDoesNotThrow(server::process);
    }
}
