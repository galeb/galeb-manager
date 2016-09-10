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

package io.galeb.manager.engine.driver.impl;

import io.galeb.manager.common.Properties;
import io.galeb.manager.engine.driver.Driver;
import io.galeb.manager.engine.driver.DriverBuilder;
import io.galeb.manager.httpclient.FakeHttpClient;
import org.junit.Test;
import org.springframework.util.Assert;

public class GalebV32DriverTest {

    private Driver driver = DriverBuilder.build(GalebV32Driver.DRIVER_NAME).addResource(new FakeHttpClient());

    @Test
    public void notExist() {
        Properties properties = new Properties();
        boolean result = driver.exist(properties);

        Assert.isTrue(!result);
    }

}
