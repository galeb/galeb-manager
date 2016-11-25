/*
 *   Galeb - Load Balance as a Service Plataform
 *
 *   Copyright (C) 2014-2016 Globo.com
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.galeb.manager.httpclient;

import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.net.URISyntaxException;

public interface CommonHttpRequester {

    ResponseEntity<String> get(String uriPath) throws URISyntaxException;

    ResponseEntity<String> post(String uriPath, String body) throws URISyntaxException;

    ResponseEntity<String> put(String uriPath, String body) throws URISyntaxException;

    ResponseEntity<String> delete(String uriPath, String body) throws URISyntaxException, IOException;

    boolean isStatusCodeEqualOrLessThan(final ResponseEntity<String> response, int status);

    boolean bodyIsEmptyOrEmptyArray(final ResponseEntity<String> response);

}
