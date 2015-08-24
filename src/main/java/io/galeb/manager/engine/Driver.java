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

package io.galeb.manager.engine;

import java.io.IOException;

import io.galeb.manager.common.Properties;
import io.galeb.manager.engine.farm.EntityFarm;

public interface Driver {

    public enum StatusFarm {
        OK,
        FAIL,
        UNKNOWN
    }

    public static final String DEFAULT_DRIVER_NAME = "NULL";

    default EntityFarm info(Properties properties) {
        return null;
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

    default boolean reload(Properties properties) throws IOException {
        return true;
    }

    default StatusFarm status(Properties properties) {
        return StatusFarm.UNKNOWN;
    }

}
