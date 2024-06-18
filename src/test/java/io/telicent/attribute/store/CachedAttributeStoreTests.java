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

import io.telicent.jena.abac.AttributeValueSet;
import io.telicent.jena.abac.Hierarchy;
import io.telicent.jena.abac.attributes.Attribute;
import io.telicent.jena.abac.core.AttributesStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Set;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CachedAttributeStoreTests {

    private static final AttributeValueSet SAMPLE_ATTRIBUTE_VALUE_SET =
            AttributeValueSet.of("attribute1=value1,attribute2=value2,attribute3=value3");

    private static final Hierarchy SAMPLE_HIERARCHY =
            Hierarchy.create("Test", "attribute1", "attribute2", "attribute3");

    private CachedAttributeStore cut;

    @Mock
    private AttributesStore mockedStore;

    @Test
    public void test_attributes_initialCallMadeToUnderlyingStoreRemainingToCache() {
        //given
        when(mockedStore.attributes("user")).thenReturn(SAMPLE_ATTRIBUTE_VALUE_SET)
                                            .thenThrow(new RuntimeException("Test failed - cache bypassed"));
        cut = new CachedAttributeStore(mockedStore, Duration.ofSeconds(5));
        //when
        AttributeValueSet initialResult = cut.attributes("user");
        AttributeValueSet subsequentResult = cut.attributes("user");
        //then
        assertEquals(initialResult, subsequentResult);
        verify(mockedStore, times(1)).attributes("user");
    }

    @Test
    public void test_getHierarchy_initialCallMadeToUnderlyingStoreRemainingToCache() {
        //given
        Attribute attribute = new Attribute("Test");
        when(mockedStore.getHierarchy(attribute)).thenReturn(SAMPLE_HIERARCHY)
                                                 .thenThrow(new RuntimeException("Test failed - cache bypassed"));
        cut = new CachedAttributeStore(mockedStore, Duration.ofSeconds(5));
        //when
        Hierarchy initialResult = cut.getHierarchy(attribute);
        Hierarchy subsequentResult = cut.getHierarchy(attribute);
        //then
        assertEquals(initialResult, subsequentResult);
        verify(mockedStore, times(1)).getHierarchy(attribute);
    }

    @Test
    public void test_hasHierarchy_initialCallMadeToUnderlyingStoreRemainingToCache() {
        // given
        Attribute attribute = new Attribute("Test");
        when(mockedStore.getHierarchy(attribute)).thenReturn(SAMPLE_HIERARCHY)
                                                 .thenThrow(new RuntimeException("Test failed - cache bypassed"));
        cut = new CachedAttributeStore(mockedStore, Duration.ofSeconds(5));
        // when
        boolean initialResult = cut.hasHierarchy(attribute);
        boolean subsequentResult = cut.hasHierarchy(attribute);
        //then
        assertEquals(initialResult, subsequentResult);
        verify(mockedStore, times(1)).getHierarchy(attribute);
    }

    @Test
    public void test_hasHierarchy_handleNullResult_asFalse() {
        // given
        Attribute attribute = new Attribute("Test");
        when(mockedStore.getHierarchy(attribute)).thenReturn(null);
        cut = new CachedAttributeStore(mockedStore, Duration.ofSeconds(5));
        // when
        boolean result = cut.hasHierarchy(attribute);
        //then
        assertFalse(result);
        verify(mockedStore, times(1)).getHierarchy(attribute);
    }

    @Test
    public void test_hasHierarchy_handleEmptyResult_asFalse() {
        // given
        Attribute attribute = new Attribute("Test");
        Hierarchy emptyHierarchy = Hierarchy.create(attribute, emptyList());
        when(mockedStore.getHierarchy(attribute)).thenReturn(emptyHierarchy);
        cut = new CachedAttributeStore(mockedStore, Duration.ofSeconds(5));
        // when
        boolean result = cut.hasHierarchy(attribute);
        //then
        assertFalse(result);
        verify(mockedStore, times(1)).getHierarchy(attribute);
    }

    @Test
    public void test_users_callGoesToUnderlyingStoreNotCache() {
        //given
        when(mockedStore.users()).thenReturn(Set.of("user1", "user2"))
                                 .thenReturn(Set.of("user1", "user2", "user3", "user4"));
        cut = new CachedAttributeStore(mockedStore, Duration.ofSeconds(5));
        //when
        Set<String> initialResult = cut.users();
        Set<String> subsequentResult = cut.users();
        //then
        assertNotEquals(initialResult, subsequentResult);
        verify(mockedStore, times(2)).users();
    }

    @Test
    public void test_attributes_initialCallMadeToUnderlyingStoreAfterCacheExpriy() {
        //given
        when(mockedStore.attributes("user")).thenReturn(SAMPLE_ATTRIBUTE_VALUE_SET)
                                            .thenReturn(AttributeValueSet.of("attribute1=value1"));
        cut = new CachedAttributeStore(mockedStore, Duration.ofNanos(5));
        //when
        AttributeValueSet initialResult = cut.attributes("user");
        AttributeValueSet subsequentResult = cut.attributes("user");
        //then
        assertNotEquals(initialResult, subsequentResult);
        verify(mockedStore, times(2)).attributes("user");
    }


}
