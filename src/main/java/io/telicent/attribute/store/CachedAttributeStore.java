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

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.telicent.jena.abac.AttributeValueSet;
import io.telicent.jena.abac.Hierarchy;
import io.telicent.jena.abac.attributes.Attribute;
import io.telicent.jena.abac.core.AttributesStore;

import java.time.Duration;
import java.util.Set;

/**
 * Simple cached wrapper around existing Attribute Store.
 */
public class CachedAttributeStore implements AttributesStore {
    final Cache<String, AttributeValueSet> userAttributeCache;
    final Cache<Attribute, Hierarchy> hierarchyCache;
    final AttributesStore underlyingStore;

    /**
     * Create new Cached Attribute Store
     * @param underlyingStore Attribute store to reference
     * @param cacheExpiryTime How long to hold cached entries
     */
    public CachedAttributeStore(AttributesStore underlyingStore, Duration cacheExpiryTime) {
        this.underlyingStore = underlyingStore;
        userAttributeCache = Caffeine.newBuilder().recordStats().expireAfterWrite(cacheExpiryTime).build();
        hierarchyCache = Caffeine.newBuilder().recordStats().expireAfterWrite(cacheExpiryTime).build();
    }

    @Override
    public AttributeValueSet attributes(String user) {
        return userAttributeCache.get(user, underlyingStore::attributes);
    }

    @Override
    public Set<String> users() {
        return underlyingStore.users();
    }

    @Override
    public boolean hasHierarchy(Attribute attribute) {
        Hierarchy hierarchy = getHierarchy(attribute);
        return hierarchy != null && !hierarchy.values().isEmpty();
    }

    @Override
    public Hierarchy getHierarchy(Attribute attribute) {
        return hierarchyCache.get(attribute, underlyingStore::getHierarchy);
    }
}
