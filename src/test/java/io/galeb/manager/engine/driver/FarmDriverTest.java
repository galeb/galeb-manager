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

package io.galeb.manager.engine.driver;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.galeb.manager.common.Properties;
import io.galeb.manager.engine.GalebV32Driver;
import io.galeb.manager.engine.driver.Driver;
import io.galeb.manager.engine.driver.DriverBuilder;
import io.galeb.manager.httpclient.FakeFarmClient;
import io.galeb.manager.test.factory.FarmFactory;
import io.galeb.manager.test.factory.PoolFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.Assert;

public class FarmDriverTest {

    private static final Log LOGGER = LogFactory.getLog(GalebV32Driver.class);

    private final FarmFactory farmFactory = new FarmFactory();
    private final PoolFactory poolFactory = new PoolFactory();
    private final FakeFarmClient fakeFarmClient = new FakeFarmClient();
    private final Driver driver = DriverBuilder.build(GalebV32Driver.DRIVER_NAME).addResource(fakeFarmClient);

    private void logTestedMethod() {
        LOGGER.info("Testing " + this.getClass().getSimpleName() + "." +
                    Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Before
    public void setUp() {
        fakeFarmClient.deleteAll();
    }

    @Test
    public void notExist() {
        logTestedMethod();
        Properties properties = new Properties();
        boolean result = driver.exist(properties);
        Assert.isTrue(!result);
    }

    @Test
    public void getFarmAlwaysReturnOK() throws JsonProcessingException {
        logTestedMethod();
        boolean result = driver.exist(farmFactory.makeProperties());
        Assert.isTrue(result);
    }

    @Test
    public void removeFarmAlwaysReturnAccept() throws JsonProcessingException {
        logTestedMethod();
        boolean result = driver.remove(farmFactory.makeProperties());
        Assert.isTrue(result);
    }

    @Test
    public void createAndUpdateFarmIsNotPossible() {
        logTestedMethod();
        boolean resultCreate = driver.create(farmFactory.makeProperties());
        boolean resultUpdate = driver.update(farmFactory.makeProperties());

        Assert.isTrue(!resultCreate, "Create Farm is possible?");
        Assert.isTrue(!resultUpdate, "Update Farm is possible?");
    }

}
