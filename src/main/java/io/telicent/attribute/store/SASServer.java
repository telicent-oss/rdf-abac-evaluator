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

import io.telicent.attribute.utils.ExcludeFromJacocoGeneratedReport;
import io.telicent.jena.abac.core.AttributesStore;
import io.telicent.jena.abac.services.SimpleAttributesStore;
import org.apache.jena.cmd.ArgDecl;
import org.apache.jena.cmd.CmdGeneral;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.telicent.attribute.utils.AttributeUtils.createLocalAttributeStore;
import static io.telicent.attribute.utils.AttributeUtils.portNumber;

/**
 * REST Server simulating Access server holding user attributes and hierarchy.
 */
public class SASServer extends CmdGeneral {

    /**
     * Logger for class
     */
    public static final Logger LOGGER = LoggerFactory.getLogger("SAS");
    private static final ArgDecl argPort = new ArgDecl(ArgDecl.HasValue, "port", "Port", "p");
    private static final ArgDecl argStore =
            new ArgDecl(ArgDecl.HasValue, "attrStore", "attrstore", "store", "attributeStore", "attributestore",
                        "attributes", "userAttrStore");
    private static final int DEFAULT_PORT = 64331;
    private int port;
    private String attributeStorePath;

    private String serverURL;

    /**
     * Create SAS Server
     * @param argv configuration parameters
     */
    public SASServer(String[] argv) {
        super(argv);
        add(argStore, "store", "File name for local attribute store");
        add(argPort, "port", "Listen on this port number");
    }

    /**
     * Entrypoint for the Server
     *
     * @param args CLI arguments
     */
    @ExcludeFromJacocoGeneratedReport
    public static void main(String... args) {
        new SASServer(args).mainRun();
    }

    @Override
    protected void processModulesAndArgs() {
        super.processModulesAndArgs();
        port = portNumber(getValue(argPort), DEFAULT_PORT);
        attributeStorePath = super.getValue(argStore);
    }

    @Override
    protected void exec() {
        AttributesStore attributesStore = createLocalAttributeStore(attributeStorePath);
        serverURL = SimpleAttributesStore.run(port, attributesStore);
        LOGGER.info("URL = {}", serverURL);
    }

    @Override
    protected String getSummary() {
        String usage = " --store=<file:filename> [--port]";
        return getCommandName() + usage;
    }

    @Override
    protected String getCommandName() {
        return this.getClass().getSimpleName();
    }

    /**
     * Get the URL for the server
     * @return URL
     */
    public String getURL() {
        return serverURL;
    }
}
