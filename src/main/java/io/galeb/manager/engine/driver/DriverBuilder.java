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

import java.util.HashMap;
import java.util.Map;

import io.galeb.manager.engine.driver.impl.*;
import io.galeb.manager.entity.Farm;

public class DriverBuilder {

    private static Map<String, Driver> drivers = new HashMap<>();
    static {
        drivers.put(Driver.DEFAULT_DRIVER_NAME, new NullDriver());
        drivers.put(GalebV32Driver.DRIVER_NAME, new GalebV32Driver());
    }

    public static Driver addResource(Driver driver, Object resource) {
        return driver.addResource(resource);
    }

    public static Driver build(String driverName) {
        final Driver driver = drivers.get(driverName);
        if (driver == null) {
            return drivers.get(Driver.DEFAULT_DRIVER_NAME);
        }
        return driver;
    }

    public static Driver getDriver(Farm farm) {
        String driverName = farm.getProvider().getDriver();
        return build(driverName);
    }

}
