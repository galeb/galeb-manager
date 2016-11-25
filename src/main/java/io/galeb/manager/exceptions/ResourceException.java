/*
 * Galeb - Load Balance as a Service Plataform
 *
 * Copyright (C) 2014-2016 Globo.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.galeb.manager.exceptions;

import org.springframework.http.HttpStatus;

@SuppressWarnings("ClassWithoutNoArgConstructor")
public class ResourceException extends RuntimeException {

    private HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

    /**
     * Gets the HTTP status code to be returned to the calling system.
     * @return http status code.  Defaults to HttpStatus.INTERNAL_SERVER_ERROR (500).
     * @see HttpStatus
     */
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    /**
     * Constructs a new runtime exception with the specified HttpStatus code and detail message.
     * The cause is not initialized, and may subsequently be initialized by a call to {@link #initCause}.
     * @param httpStatus the http status.  The detail message is saved for later retrieval by the {@link
     *                   #getHttpStatus()} method.
     * @param message    the detail message. The detail message is saved for later retrieval by the {@link
     *                   #getMessage()} method.
     * @see HttpStatus
     */
    public ResourceException(HttpStatus httpStatus, String message) {
        super(message);
        this.httpStatus = httpStatus;
    }
}
