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

import com.fasterxml.jackson.core.JsonProcessingException;
import io.galeb.manager.common.Properties;
import io.galeb.manager.engine.driver.Driver;
import io.galeb.manager.engine.driver.DriverBuilder;
import io.galeb.manager.entity.Pool;
import io.galeb.manager.httpclient.FakeFarmClient;
import io.galeb.manager.test.factory.FarmFactory;
import io.galeb.manager.test.factory.PoolFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.Assert;

import java.util.Map;

public class GalebV32DriverTest {

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

    @Test
    public void poolNotExist() {
        logTestedMethod();
        boolean result = driver.exist(poolFactory.makeProperties(poolFactory.build()));
        Assert.isTrue(!result);
    }

    @Test
    public void createPool() {
        logTestedMethod();
        Properties properties = poolFactory.makeProperties(poolFactory.build());
        boolean resultCreate = driver.create(properties);
        boolean resultExist = driver.exist(properties);
        Assert.isTrue(resultCreate && resultExist);
    }

    @Test
    public void updatePool() throws Exception {
        logTestedMethod();
        Pool pool = poolFactory.build();
        Properties properties = poolFactory.makeProperties(pool);
        String api = properties.getOrDefault("api", "UNDEF").toString();
        String poolName = pool.getName();
        boolean resultCreate = driver.create(properties);
        Map<String, String> map = driver.getAll(properties).get("backendpool").get(api + "/backendpool/" + poolName + "@");
        String versionStr = map.get("version");
        int versionOrig = Integer.parseInt(versionStr);
        boolean resultExist = driver.exist(properties);
        pool.updateHash();
        boolean resultUpdate = driver.update(poolFactory.makeProperties(pool));
        map = driver.getAll(properties).get("backendpool").get(api + "/backendpool/" + poolName + "@");
        versionStr = map.get("version");
        int versionNew = Integer.parseInt(versionStr);
        Assert.isTrue(resultCreate && resultExist && resultUpdate && versionNew > versionOrig);
    }

    @Test
    public void removePool() {
        logTestedMethod();
        Properties properties = poolFactory.makeProperties(poolFactory.build());
        boolean resultCreate = driver.create(properties);
        boolean resultExist = driver.exist(properties);
        boolean resultRemove = driver.remove(properties);
        boolean resultNotExist = !driver.exist(properties);

        Assert.isTrue(resultCreate && resultExist && resultRemove && resultNotExist);
    }

}
