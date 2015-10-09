/*
 * Galeb - Load Balance as a Service Plataform
 *
 * Copyright (C) 2014-2015 Globo.com
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
 *
 *
 */

package io.galeb.manager.exceptions;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@ResponseStatus(value=HttpStatus.SERVICE_UNAVAILABLE, reason="service unavaliable")
public class ServiceUnavailableException extends RuntimeException {

    private static final long serialVersionUID = 6986223937559244227L;

    public ServiceUnavailableException() {
        super();
    }

    public ServiceUnavailableException(String message) {
        super(message);
    }
}
