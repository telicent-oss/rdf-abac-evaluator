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

import io.telicent.attribute.store.CachedAttributeStore;
import io.telicent.jena.abac.core.Attributes;
import io.telicent.jena.abac.core.AttributesStore;
import io.telicent.jena.abac.core.AttributesStoreRemote;
import org.apache.jena.cmd.CmdException;
import org.apache.jena.graph.Graph;
import org.apache.jena.riot.RDFParser;

import java.time.Duration;
import java.time.format.DateTimeParseException;

 /**
  * Utility class for creating attributes
  */
 public final class AttributeUtils {
    private AttributeUtils() {
    }

     /**
      * Create a remote attribute store with a cache
      * @param lookupUserEndpoint URL to obtain user data
      * @param lookupHierarchyEndpoint URL to obtain hiearchy data
      * @param cacheExpiryTime how long to keep data in cahe
      * @return Attribute Store
      */
    public static AttributesStore createCachedRemoteAttributeStore(String lookupUserEndpoint,
                                                                   String lookupHierarchyEndpoint,
                                                                   Duration cacheExpiryTime) {
        return new CachedAttributeStore(new AttributesStoreRemote(lookupUserEndpoint, lookupHierarchyEndpoint),
                                        cacheExpiryTime);
    }

     /**
      * Create a remote attribute store without a cache
      * @param lookupUserEndpoint URL to obtain user data
      * @param lookupHierarchyEndpoint URL to obtain hiearchy data
      * @return Attribute Store
      */
     public static AttributesStore createRemoteAttributeStore(String lookupUserEndpoint,
                                                             String lookupHierarchyEndpoint) {
        return new AttributesStoreRemote(lookupUserEndpoint, lookupHierarchyEndpoint);
    }

     /**
      *  Create an attribute store based on local file
      * @param localAttributeStoreFile File to use for the attribute data
      * @return Attribute Store
      */
     public static AttributesStore createLocalAttributeStore(String localAttributeStoreFile) {
        Graph graph = RDFParser.source(localAttributeStoreFile).toGraph();
        return Attributes.buildStore(graph);
    }

     /**
      * Parse the given Port number
      * @param portStr String representation of the desired port number
      * @param defaultPort What to use if there's a problem with the given value
      * @return integer representation
      */
    public static int portNumber(String portStr, int defaultPort) {
        if (null == portStr || portStr.isEmpty()) {
            return defaultPort;
        }
        try {
            return Integer.parseUnsignedInt(portStr);
        } catch (NumberFormatException ex) {
            throw new CmdException("Bad port number: '" + portStr + "'", ex);
        }
    }

     /**
      * Parse the given string into a duration
      * @param durationStr the desired duration
      * @param defaultDuration what to use if there's a problem
      * @return the relevant duration
      */
    public static Duration parseDuration(String durationStr, Duration defaultDuration) {
        if (null == durationStr || durationStr.isEmpty()) {
            return defaultDuration;
        }
        try {
            return Duration.parse(durationStr);
        } catch (DateTimeParseException ex) {
            throw new CmdException("Bad duration format: '" + durationStr + "'", ex);
        }
    }
}
