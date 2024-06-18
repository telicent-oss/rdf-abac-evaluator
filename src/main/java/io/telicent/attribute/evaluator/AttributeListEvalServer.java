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

import io.telicent.attribute.utils.ExcludeFromJacocoGeneratedReport;
import io.telicent.jena.abac.AttributeValueSet;
import io.telicent.jena.abac.attributes.AttributeExpr;
import io.telicent.jena.abac.attributes.AttributeParser;
import io.telicent.jena.abac.attributes.AttributeSyntaxError;
import io.telicent.jena.abac.attributes.ValueTerm;
import io.telicent.jena.abac.core.AttributesStore;
import io.telicent.jena.abac.core.CxtABAC;
import io.telicent.jena.abac.services.LibAuthService;
import jakarta.servlet.ServletOutputStream;
import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.fuseki.servlets.ActionService;
import org.apache.jena.fuseki.servlets.HttpAction;
import org.apache.jena.fuseki.servlets.ServletOps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Remove this once RDF-ABAC-Eval is available. Move any bug-fixes from here to that module. 1. getOneOnly() reg-ex fix
 */
public final class AttributeListEvalServer {

    private AttributeListEvalServer() {}

    /**
     * Logger
     */
    public static final Logger LOGGER = LoggerFactory.getLogger("ALE");

    /**
     * Run the Server
     * @param port Port to use
     * @param path Path to use
     * @param attrStore Attribute Store to reference
     * @return The URL for the server.
     */
    public static String run(int port, String path, AttributesStore attrStore) {
        ActionService actionService = new AttributeListLabelEvaluator(attrStore);
        String url = LibAuthService.run(port, LOGGER, List.of(Pair.create(path, actionService)));
        return LibAuthService.serviceURL(url, path);
    }

    /**
     * <pre>
     * path?user=...&label=...
     * </pre>
     */
    static class AttributeListLabelEvaluator extends ActionService {

        private static final String paramUser = "user";
        private static final String paramLabel = "label";
        private static final ValueTerm dftResult = ValueTerm.FALSE;
        private final AttributesStore attributesStore;

        public AttributeListLabelEvaluator(AttributesStore attrStore) {
            this.attributesStore = attrStore;
        }

        // Implemented methods.
        @Override
        public void execPost(HttpAction action) {
            executeLifecycle(action);
        }

        @Override
        @ExcludeFromJacocoGeneratedReport
        public void validate(HttpAction action) {
            Map<String, String[]> m = action.getRequestParameterMap();
            if (!m.containsKey(paramLabel) && !m.containsKey(paramUser)) {
                ServletOps.errorBadRequest("No 'label' and no 'user' query parameter");
            }
            if (!m.containsKey(paramLabel)) {
                ServletOps.errorBadRequest("No 'label' query parameter");
            }
            if (!m.containsKey(paramUser)) {
                ServletOps.errorBadRequest("No 'user' query parameter");
            }
            if (m.get(paramUser).length >= 2) {
                ServletOps.errorBadRequest("More than one 'user' query parameter");
            }
            if (m.get(paramLabel).length >= 2) {
                ServletOps.errorBadRequest("More than one 'label' query parameter");
            }
        }

        /** execute
         * <p>
         * request: query string:
         * </p>
         * <pre>
         *   {@code ?user=USER&label=LABEL}
         * </pre>
         * <p>
         * response:
         * <pre>
         *   400 - Bad request
         *   200 - JSON:
         *   {
         *      "user" : "..." ;
         *      "result" : "STRING"
         *   }
         * </pre>
         * <p>
         * where STRING is "true" or "false".
         * </p>
         * <p>
         * If the user is unknown, return "false"
         * </p>
         */
        @Override
        @ExcludeFromJacocoGeneratedReport
        public void execute(HttpAction action) {
            // c.f. GSPLib. Move to one place!
            String user = getOneOnly(action, paramUser);
            String label = getOneOnly(action, paramLabel);

            JsonObject jObj = JSON.buildObject(jb -> {
                jb.pair("user", user);

                AttributeValueSet avSet = attributesStore.attributes(user);
                if (avSet == null) {
                    LOGGER.info("No attributes for user: " + user);
                    // Return false.
                    jb.pair("result", dftResult.asString());
                    return;
                }

                // Parse - evaluate
                List<AttributeExpr> attrExprs;
                try {
                    attrExprs = AttributeParser.parseAttrExprList(label);
                } catch (AttributeSyntaxError ex) {
                    ServletOps.errorBadRequest("Bad syntax: " + ex.getMessage());
                    /*does not*/
                    return;
                }

                CxtABAC context = CxtABAC.context(avSet, attributesStore, null);
                // Default of a zero length list.
                boolean allow = dftResult.getBoolean();
                for (AttributeExpr attrExpr : attrExprs) {
                    ValueTerm vt = attrExpr.eval(context);
                    allow = vt.getBoolean();
                    if (!allow) {
                        break;
                    }
                }
                ValueTerm vt = ValueTerm.value(allow);
                jb.pair("result", vt.asString());
                LOGGER.info("Result for user: " + user + " :: " + allow);
            });

            try (ServletOutputStream out = action.getResponseOutputStream()) {
                JSON.write(out, jObj);
                ServletOps.success(action);
            } catch (IOException e) {
                LOGGER.error("Failure in returning results", e);
            }
        }

        private String getOneOnly(HttpAction action, String param) {
            return action.getRequestParameter(param);
        }
    }
}
