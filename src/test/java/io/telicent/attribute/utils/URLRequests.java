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

import org.apache.jena.atlas.lib.IRILib;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.http.HttpOp;

public final class URLRequests {

    private URLRequests() {
    }

    /** Execute an HTTP request using POST and return the request body or throw an exception.
     * @param URL - remote endpoint
     * @param user - The user id
     * @param label - The ABAC label
     * @throws HttpException Failure when posting request
     */
    public static String postEvalRequest(String URL, String user, String label) {
        String requestURL = URL + "?user=%s&label=%s";
        return postRequest(requestURL, IRILib.encodeUriQueryFrag(user), IRILib.encodeUriQueryFrag(label));
    }

    /** Execute an HTTP request using HTTP POST, return the request body or throw an exception.
     * @param URL - remote endpoint as printf string template
     * @param params - URL parameters for string template
     * @throws HttpException Failure when posting request
     */
    public static String postRequest(String URL, Object... params) {
        String requestURL = String.format(URL, params);
        return HttpOp.httpPostRtnString(requestURL);
    }

    /** Execute an HTTP request using HTTP GET and return the request body, or throw an exception.
     * @param URL - remote endpoint as printf string template
     * @param params - URL parameters for string template
     * @throws HttpException Failure when posting request
     */
    public static String getRequest(String URL, Object... params) {
        String requestURL = String.format(URL, params);
        return HttpOp.httpGetString(requestURL);
    }
}
