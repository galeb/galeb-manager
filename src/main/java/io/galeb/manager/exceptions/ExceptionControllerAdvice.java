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

import io.galeb.manager.common.JsonMapper;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ExceptionControllerAdvice {

    @ExceptionHandler(ResourceException.class)
    public ResponseEntity badException(ResourceException e) {
        String result;
        JsonMapper json = new JsonMapper();
        result = json.putString("statusText", e.getMessage())
                     .putString("status", e.getHttpStatus().toString())
                     .toString();
        return ResponseEntity.status(e.getHttpStatus()).contentType(MediaType.APPLICATION_JSON).body(result);
    }
}
