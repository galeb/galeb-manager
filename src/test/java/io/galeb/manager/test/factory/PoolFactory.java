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
import io.galeb.manager.entity.Pool;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PoolFactory {

    private static final Log LOGGER = LogFactory.getLog(PoolFactory.class);

    public Pool build() {
        final Pool pool= new Pool(UUID.randomUUID().toString());
        pool.getProperties().put(io.galeb.core.model.BackendPool.PROP_LOADBALANCE_POLICY, "DEFAULT");
        pool.updateHash();
        return pool;
    }

    public Properties makeProperties(Pool pool) {
        String api = "api";
        Map<String, List<?>> entitiesMap = Collections.emptyMap();
        Properties properties = new Properties();
        properties.put("api", api);
        properties.put("entitiesMap", entitiesMap);
        properties.put("path", io.galeb.core.model.BackendPool.class.getSimpleName().toLowerCase());
        try {
            properties.put("json", new JsonMapper().makeJson(pool).toString());
        } catch (JsonProcessingException e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        }

        return properties;
    }

}
