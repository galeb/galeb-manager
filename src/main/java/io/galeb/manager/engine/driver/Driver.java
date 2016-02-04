/*
 *   Galeb - Load Balance as a Service Plataform
 *
 *   Copyright (C) 2014-2015 Globo.com
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

package io.galeb.manager.engine.driver;

import java.util.Collections;
import java.util.Map;

import io.galeb.manager.common.Properties;

public interface Driver {

    String DEFAULT_DRIVER_NAME = "NULL";

    enum ActionOnDiff {
        CREATE,
        REMOVE,
        UPDATE,
        CALLBACK
    }

    default EntityFarm info(Properties properties) {
        return () -> "";
    }

    default boolean exist(Properties properties) {
        return false;
    }

    default boolean create(Properties properties) {
        return true;
    }

    default boolean update(Properties properties) {
        return true;
    }

    default boolean remove(Properties properties) {
        return true;
    }

    default Map<String, Map<String, Object>> diff(Properties properties,
              Map<String, Map<String, Map<String, String>>> getAll) throws Exception {
        return Collections.emptyMap();
    }

    default Driver addResource(Object resource) {
        return this;
    }

    default Map<String,Map<String,Map<String,String>>> getAll(Properties properties) throws Exception {
        return Collections.emptyMap();
    }

}
