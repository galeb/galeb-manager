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

package io.galeb.manager.test.factory;

import io.galeb.manager.entity.AbstractEntity;

import java.util.HashMap;
import java.util.Map;

import static io.galeb.manager.engine.listeners.AbstractEngine.*;

public abstract class AbstractFactory<T extends AbstractEntity<T>> {

    public abstract T build(String arg);

    public Map<String, String> jmsHeaderProperties() {
        Map<String, String> jmsHeader= new HashMap<>();
        jmsHeader.put(API_PROP, "api");
        return jmsHeader;
    }
}
