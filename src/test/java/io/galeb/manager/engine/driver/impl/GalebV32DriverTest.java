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
import io.galeb.manager.common.JsonMapper;
import io.galeb.manager.common.Properties;
import io.galeb.manager.engine.driver.Driver;
import io.galeb.manager.engine.driver.DriverBuilder;
import io.galeb.manager.entity.Environment;
import io.galeb.manager.entity.Farm;
import io.galeb.manager.entity.Provider;
import io.galeb.manager.httpclient.FakeHttpClient;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GalebV32DriverTest {

    private FakeHttpClient fakeHttpClient = new FakeHttpClient();
    private Driver driver = DriverBuilder.build(GalebV32Driver.DRIVER_NAME).addResource(fakeHttpClient);
    private static final Log LOGGER = LogFactory.getLog(GalebV32Driver.class);

    @Before
    public void setUp() {
        fakeHttpClient.deleteAll();
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
        boolean result = driver.exist(farmPropertiesBuild());
        Assert.isTrue(result);
    }

    @Test
    public void removeFarmAlwaysReturnAccept() throws JsonProcessingException {
        logTestedMethod();
        boolean result = driver.remove(farmPropertiesBuild());
        Assert.isTrue(result);
    }

    @Test
    public void createAndUpdateFarmIsNotPossible() {
        logTestedMethod();
        boolean resultCreate = driver.create(farmPropertiesBuild());
        boolean resultUpdate = driver.update(farmPropertiesBuild());

        Assert.isTrue(!resultCreate, "Create Farm is possible?");
        Assert.isTrue(!resultUpdate, "Update Farm is possible?");
    }

    private Farm newFarm(String api) {
        Environment environment = new Environment("NULL");
        Provider provider = new Provider(GalebV32Driver.class.getSimpleName());
        String name = UUID.randomUUID().toString();
        String domain = UUID.randomUUID().toString();
        return new Farm(name, domain, api, environment, provider);
    }

    private Properties farmPropertiesBuild() {
        String api = "api";
        Map<String, List<?>> entitiesMap = Collections.emptyMap();
        Properties properties = new Properties();
        properties.put("api", api);
        properties.put("entitiesMap", entitiesMap);
        properties.put("lockName", "lock_0");
        properties.put("path", "farm");
        try {
            properties.put("json", new JsonMapper().makeJson(newFarm(api)).toString());
        } catch (JsonProcessingException e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        }

        return properties;
    }

    private void logTestedMethod() {
        LOGGER.info("Testing " + this.getClass().getSimpleName() + "." + Thread.currentThread().getStackTrace()[2].getMethodName());
    }

}
