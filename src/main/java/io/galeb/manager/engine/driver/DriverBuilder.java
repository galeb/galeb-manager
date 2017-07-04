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
import io.galeb.manager.engine.util.CounterDownLatch;
import io.galeb.manager.entity.Farm;

public class DriverBuilder {

    private static CounterDownLatch counterDownLatch;

    public static void setCounterDownLatch(CounterDownLatch c) {
        counterDownLatch = c;
    }

    public static Driver build(String driverName) {
        if (driverName.equals(GalebV32Driver.class.getSimpleName().replace("Driver",""))) {
            return new GalebV32Driver().setCounterDownLatch(counterDownLatch);
        }
        return new NullDriver();
    }

    public static Driver getDriver(Farm farm) {
        if (farm != null) {
            String driverName = farm.getProvider().getDriver();
            return build(driverName);
        } else {
            return new NullDriver();
        }
    }

}
