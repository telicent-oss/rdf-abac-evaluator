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
import io.telicent.jena.abac.core.AttributesStore;
import org.apache.jena.atlas.RuntimeIOException;
import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.cmd.ArgDecl;
import org.apache.jena.cmd.CmdException;
import org.apache.jena.cmd.CmdGeneral;
import org.apache.jena.irix.IRIException;
import org.apache.jena.irix.IRIs;
import org.apache.jena.irix.IRIx;
import org.apache.jena.sys.JenaSystem;
import org.slf4j.Logger;

import java.time.Duration;
import java.time.format.DateTimeParseException;

import static io.telicent.attribute.utils.AttributeUtils.*;

/**
 * REST Server that evaluates given user and labels.
 */
public class ALEServer extends CmdGeneral {

    private static final Logger LOGGER = AttributeListEvalServer.LOGGER;
    private static final ArgDecl argPort = new ArgDecl(ArgDecl.HasValue, "port", "Port", "p");
    private static final ArgDecl argConfig =
            new ArgDecl(ArgDecl.HasValue, "config", "conf", "configuration", "configfile", "configFile", "c");
    private static final ArgDecl argStoreURL =
            new ArgDecl(ArgDecl.HasValue, "attrStore", "attrstore", "store", "attributeStore", "attributestore",
                        "attributes", "userAttrStore");
    private static final ArgDecl argStoreUserURL =
            new ArgDecl(ArgDecl.HasValue, "userEndpoint", "user_endpoint", "userendpoint", "user", "userService");
    private static final ArgDecl argStoreHierarchyURL =
            new ArgDecl(ArgDecl.HasValue, "hierarchyEndpoint", "labelEndpoint", "labelendpoint", "label",
                        "hierarchyService");
    private static final ArgDecl argCacheEnabled =
            new ArgDecl(ArgDecl.HasValue, "cacheEnabled", "cache", "enableCache", "enableCaching", "cachingEnabled");
    private static final ArgDecl argCacheExpiry =
            new ArgDecl(ArgDecl.HasValue, "cacheExpiryTime", "expire", "expiryTime", "expireCache", "cacheTime",
                        "cacheExpiry");
    private static final String DEFAULT_USER_URL_SUFFIX = "/users/lookup/{user}";
    private static final String DEFAULT_HIERARCHY_URL_SUFFIX = "/hierarchies/lookup/{name}";
    private static final Duration DEFAULT_CACHE_EXPIRY_TIME = Duration.ofSeconds(10);
    private static final int DEFAULT_PORT = 64431;
    private String configFile;
    private String storeURL;
    private int port;
    private String localAttributeStore;
    private String lookupUserEndpoint = "";
    private String lookupHierarchyEndpoint = "";
    private boolean cachingEnabled;
    private Duration cacheExpiryTime;
    private String serverURL;

    /**
     * Create new ALE Server deployment
     * @param argv configuration
     */
    public ALEServer(String[] argv) {
        super(argv);
        add(argConfig, "config", "Configuration file (JSON)");
        add(argStoreURL, "store", "File name for local attribute store or URL for remote store");
        add(argPort, "port", "Listen on this port number");
        add(argStoreUserURL, "userEndpoint", "Endpoint for user requests (if using remote service)");
        add(argStoreHierarchyURL, "hierarchyEndpoint", "Endpoint for hierarchy requests (if using remote service)");
        add(argCacheEnabled, "cache", "Boolean flag to turn on internal cache (if using remote service)");
        add(argCacheExpiry, "cacheExpiry",
            "Duration for how long to cache user/hierarchy data (if using remote service)");
    }

    /**
     * Run App
     * @param args configuration
     */
    public static void main(String... args) {
        JenaSystem.init();
        new ALEServer(args).mainRun();
    }

    /**
     * Extract String value from JSON Argument
     * @param jObject JSON configuration value
     * @param argDecl Argument Declaration
     * @return String value
     */
    static String getJSONValue(JsonObject jObject, ArgDecl argDecl) {
        for (String arg : argDecl.getNames()) {
            if (jObject.hasKey(arg)) {
                return jObject.getString(arg);
            }
        }
        return null;
    }

    @Override
    protected String getSummary() {
        String usage = """
                 --store=[<file:filename>|<http:url>] ([--port]) ([--hierarchyEndpoint]) ([--userEndpoint]) (--[cache])\s
                 --config=<file:filename> ([--port])\s
                """;
        return getCommandName() + usage;
    }

    @Override
    protected String getCommandName() {
        return this.getClass().getSimpleName();
    }

    @Override
    protected void processModulesAndArgs() {
        super.processModulesAndArgs();
        configFile = getValue(argConfig);
        storeURL = getValue(argStoreURL);
        lookupHierarchyEndpoint = getValue(argStoreHierarchyURL);
        lookupUserEndpoint = getValue(argStoreUserURL);
        cachingEnabled = Boolean.parseBoolean(getValue(argCacheEnabled));
        if (cachingEnabled) {
            cacheExpiryTime = parseDuration(getValue(argCacheExpiry), DEFAULT_CACHE_EXPIRY_TIME);
        }

        port = portNumber(getValue(argPort), DEFAULT_PORT);

        if (configFile == null && storeURL == null) {
            throw new CmdException("Required: --attrstore or --config");
        }
        if (configFile != null && storeURL != null) {
            throw new CmdException("Required: --attrstore or --config (not both)");
        }

        if (configFile != null) {
            processConfigFile();
        }
        processStoreConfig();
    }

    @Override
    protected void exec() {
        AttributesStore attrStore = buildAttributeStore();
        serverURL = AttributeListEvalServer.run(port, "/eval", attrStore);
        LOGGER.info("URL = {}}", serverURL);
    }

    /**
     * Get the Server's internal URL
     * @return the URL of the server
     */
    public String getURL() {
        return serverURL;
    }

    private AttributesStore buildAttributeStore() {
        AttributesStore store;
        if (null != localAttributeStore) {
            LOGGER.info("Using local attribute store");
            store = createLocalAttributeStore(localAttributeStore);
        } else if (cachingEnabled) {
            LOGGER.info("Using remote (cached) attribute store");
            store = createCachedRemoteAttributeStore(lookupUserEndpoint, lookupHierarchyEndpoint, cacheExpiryTime);
        } else {
            LOGGER.info("Using remote attribute store");
            store = createRemoteAttributeStore(lookupUserEndpoint, lookupHierarchyEndpoint);
        }
        return store;
    }

    private void processStoreConfig() {
        try {
            IRIx iri = IRIx.create(storeURL);
            if (!iri.isAbsolute()) {
                throw new CmdException("Bad URI for attribute store: " + storeURL);
            }
            String scheme = IRIs.scheme(storeURL);
            if (scheme == null || "file".equalsIgnoreCase(scheme)) {
                processLocalAttributeStoreConfig();
            } else {
                processRemoteAttributeStoreConfig();
            }
        } catch (IRIException ex) {
            throw new CmdException("Bad syntax in URI for attribute store: " + storeURL, ex);
        }
    }

    void processLocalAttributeStoreConfig() {
        localAttributeStore = storeURL;
        lookupUserEndpoint = null;
        lookupHierarchyEndpoint = null;
    }

    void processRemoteAttributeStoreConfig() {
        if (lookupHierarchyEndpoint == null) {
            lookupHierarchyEndpoint = storeURL + DEFAULT_HIERARCHY_URL_SUFFIX;
            LOGGER.info("Hierarchy Endpoint not supplied so defaulting to {}", lookupHierarchyEndpoint);
        }
        if (lookupUserEndpoint == null) {
            lookupUserEndpoint = storeURL + DEFAULT_USER_URL_SUFFIX;
            LOGGER.info("User Endpoint not supplied so defaulting to {}", lookupUserEndpoint);
        }
        localAttributeStore = null;
    }

    void processConfigFile() {
        try {
            JsonObject jObject = JSON.read(configFile);
            storeURL = getJSONValue(jObject, argStoreURL);
            if (null == storeURL) {
                throw new CmdException("Configuration file needs 'attrStore' entry: " + configFile);
            }
            port = portNumber(getJSONValue(jObject, argPort), port);
            lookupUserEndpoint = getJSONValue(jObject, argStoreUserURL);
            lookupHierarchyEndpoint = getJSONValue(jObject, argStoreHierarchyURL);
            if (!cachingEnabled) {
                cachingEnabled = Boolean.parseBoolean(getJSONValue(jObject, argCacheEnabled));
            }
            if (cachingEnabled) {
                cacheExpiryTime = parseDuration(getJSONValue(jObject, argCacheExpiry), cacheExpiryTime);
            }
        } catch (RuntimeIOException | NumberFormatException | DateTimeParseException exception) {
            throw new CmdException("Configuration file in bad format: " + configFile, exception);
        }
    }

    /**
     * For testing purposes
     * @return the hierarchy endpoint being used
     */
    public String getLookupHierarchyEndpoint() {
        return lookupHierarchyEndpoint;
    }

    /**
     * For testing purposes
     * @return the user endpoint being used
     */
    public String getLookupUserEndpoint() {
        return lookupUserEndpoint;
    }
}
