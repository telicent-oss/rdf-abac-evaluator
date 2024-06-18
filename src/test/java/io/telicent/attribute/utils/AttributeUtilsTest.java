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
package io.telicent.attribute.utils;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Random;

import static io.telicent.attribute.utils.AttributeUtils.parseDuration;
import static io.telicent.attribute.utils.AttributeUtils.portNumber;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AttributeUtilsTest {
    @Test
    public void portNumber_emptyString_returnDefault() {
        // given
        int expected = new Random().nextInt();
        // when
        int actual = portNumber("", expected);
        // then
        assertEquals(actual, expected);
    }

    @Test
    public void parseDuration_emptyString_returnDefault() {
        // given
        Duration expected = Duration.ofSeconds(10);
        // when
        Duration actual = parseDuration("", expected);
        // then
        assertEquals(actual, expected);
    }
}
