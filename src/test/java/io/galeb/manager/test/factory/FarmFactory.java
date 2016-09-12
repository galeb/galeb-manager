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

import com.fasterxml.jackson.core.JsonProcessingException;
import io.galeb.manager.common.JsonMapper;
import io.galeb.manager.common.Properties;
import io.galeb.manager.engine.GalebV32Driver;
import io.galeb.manager.entity.Environment;
import io.galeb.manager.entity.Farm;
import io.galeb.manager.entity.Provider;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FarmFactory {

    private static final Log LOGGER = LogFactory.getLog(FarmFactory.class);

    public Farm build(String api) {
        Environment environment = new Environment("NULL");
        Provider provider = new Provider(GalebV32Driver.class.getSimpleName());
        String name = UUID.randomUUID().toString();
        String domain = UUID.randomUUID().toString();
        return new Farm(name, domain, api, environment, provider);
    }

    public Properties makeProperties() {
        String api = "api";
        Map<String, List<?>> entitiesMap = Collections.emptyMap();
        Properties properties = new Properties();
        properties.put("api", api);
        properties.put("entitiesMap", entitiesMap);
        properties.put("lockName", "lock_0");
        properties.put("path", "farm");
        try {
            properties.put("json", new JsonMapper().makeJson(build(api)).toString());
        } catch (JsonProcessingException e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        }

        return properties;
    }
}
