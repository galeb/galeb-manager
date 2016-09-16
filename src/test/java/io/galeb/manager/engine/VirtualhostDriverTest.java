/*
 * Galeb - Load Balance as a Service Plataform
 *
 * Copyright (C) 2014-2016 Globo.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.galeb.manager.engine;

import io.galeb.manager.common.Properties;
import io.galeb.manager.engine.driver.Driver;
import io.galeb.manager.engine.driver.DriverBuilder;
import io.galeb.manager.engine.driver.impl.GalebV32Driver;
import io.galeb.manager.engine.listeners.VirtualHostEngine;
import io.galeb.manager.entity.Rule;
import io.galeb.manager.entity.VirtualHost;
import io.galeb.manager.httpclient.FakeFarmClient;
import io.galeb.manager.test.factory.VirtualhostFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.Assert;

import java.util.*;

import static io.galeb.manager.engine.listeners.AbstractEngine.*;

public class VirtualhostDriverTest {

    private static final Log LOGGER = LogFactory.getLog(GalebV32Driver.class);

    private final VirtualhostFactory virtualhostFactory = new VirtualhostFactory();
    private final FakeFarmClient fakeFarmClient = new FakeFarmClient();
    private final Driver driver = DriverBuilder.build(GalebV32Driver.DRIVER_NAME).addResource(fakeFarmClient);
    private final VirtualHostEngine virtualhostEngine = virtualhostFactory.buildVirtualHostEngine(driver);
    private final Map<String, String> jmsHeaders = virtualhostFactory.jmsHeaderProperties();

    private void logTestedMethod() {
        LOGGER.info("Testing " + this.getClass().getSimpleName() + "." +
                Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Before
    public void setUp() {
        fakeFarmClient.deleteAll();
    }

    @Test
    public void virtualhostNotExist() {
        logTestedMethod();

        // given
        String virtualhostName = UUID.randomUUID().toString();
        Properties properties = virtualhostEngine.makeProperties(virtualhostFactory.build(virtualhostName), jmsHeaders);

        // when
        boolean result = driver.exist(properties);

        // then
        Assert.isTrue(!result);
    }

    @Test
    public void createVirtualhost() {
        logTestedMethod();

        // given
        String virtualhostName = UUID.randomUUID().toString();
        VirtualHost virtualHost = virtualhostFactory.build(virtualhostName);
        Properties properties = virtualhostEngine.makeProperties(virtualhostFactory.build(virtualhostName), jmsHeaders);

        // when
        virtualhostEngine.create(virtualHost, jmsHeaders);
        boolean resultExist = driver.exist(properties);

        // then
        Assert.isTrue(resultExist);
    }

    @Test
    public void updateVirtualhost() throws Exception {
        logTestedMethod();

        // given
        String virtualhostName = UUID.randomUUID().toString();
        VirtualHost virtualHost = virtualhostFactory.build(virtualhostName);
        Properties properties = virtualhostEngine.makeProperties(virtualhostFactory.build(virtualhostName), jmsHeaders);
        String api = properties.getOrDefault(API_PROP, "UNDEF").toString();
        String virtualHostName = virtualHost.getName();

        // when
        virtualhostEngine.create(virtualHost, jmsHeaders);
        String multiMapKey = api + "/virtualhost/" + virtualHostName + "@";
        Map<String, String> map = driver.getAll(properties).get("virtualhost").get(multiMapKey);
        String versionStr = map.get("version");
        int versionOrig = Integer.parseInt(versionStr);
        boolean resultExist = driver.exist(properties);
        virtualHost.updateHash();
        virtualhostEngine.update(virtualHost, jmsHeaders);
        map = driver.getAll(properties).get("virtualhost").get(multiMapKey);
        versionStr = map.get("version");
        int versionNew = Integer.parseInt(versionStr);

        // then
        boolean isNewVersionGreaterThenOldVersion = versionNew > versionOrig;
        Assert.isTrue(resultExist && isNewVersionGreaterThenOldVersion);
    }

    @Test
    public void removeVirtualhost() {
        logTestedMethod();

        // given
        String virtualhostName = UUID.randomUUID().toString();
        VirtualHost virtualHost = virtualhostFactory.build(virtualhostName);
        Properties properties = virtualhostEngine.makeProperties(virtualhostFactory.build(virtualhostName), jmsHeaders);

        // when
        virtualhostEngine.create(virtualHost, jmsHeaders);
        boolean resultExist = driver.exist(properties);
        virtualhostEngine.remove(virtualHost, jmsHeaders);
        boolean resultNotExist = !driver.exist(properties);

        // then
        Assert.isTrue(resultExist && resultNotExist);
    }

    @Test
    public void createAliasesWithNewVirtualhost() {
        logTestedMethod();

        // given
        String virtualhostName = UUID.randomUUID().toString();
        String anAliasName = UUID.randomUUID().toString();
        String otherAliasName = UUID.randomUUID().toString();

        VirtualHost virtualHost = virtualhostFactory.build(virtualhostName);

        Properties virtualhostProperties = virtualhostEngine.makeProperties(virtualhostFactory.build(virtualhostName), jmsHeaders);
        Properties anAliasProperties = virtualhostEngine.makeProperties(virtualhostFactory.build(anAliasName), jmsHeaders);
        Properties otherAliasProperties = virtualhostEngine.makeProperties(virtualhostFactory.build(otherAliasName), jmsHeaders);
        Set<String> aliases = new HashSet<>();

        // when
        aliases.add(anAliasName); // add an alias
        aliases.add(otherAliasName); // add an other alias
        virtualHost.setAliases(aliases);
        virtualhostEngine.create(virtualHost, jmsHeaders); // create virtualhost with two alias

        boolean resultExistVirtualhost = driver.exist(virtualhostProperties);
        boolean resultExistAnAlias     = driver.exist(anAliasProperties);
        boolean resultExistOtherAlias  = driver.exist(otherAliasProperties);

        // then
        Assert.isTrue(resultExistVirtualhost && resultExistAnAlias && resultExistOtherAlias);
    }

    @Test
    public void createAliasesWithSavedVirtualhost() {
        logTestedMethod();

        // given
        String virtualhostName = UUID.randomUUID().toString();
        String anAliasName = UUID.randomUUID().toString();
        String otherAliasName = UUID.randomUUID().toString();

        VirtualHost virtualHost = virtualhostFactory.build(virtualhostName);
        Properties virtualhostProperties = virtualhostEngine.makeProperties(virtualhostFactory.build(virtualhostName), jmsHeaders);
        Properties anAliasProperties = virtualhostEngine.makeProperties(virtualhostFactory.build(anAliasName), jmsHeaders);
        Properties otherAliasProperties = virtualhostEngine.makeProperties(virtualhostFactory.build(otherAliasName), jmsHeaders);
        Set<String> aliases = new HashSet<>();

        // when
        aliases.add(anAliasName); // add only one alias
        virtualHost.setAliases(aliases);
        virtualhostEngine.create(virtualHost, jmsHeaders); // create virtualhost with one alias
        aliases.add(otherAliasName); // add an other alias
        virtualHost.setAliases(aliases);
        virtualhostEngine.update(virtualHost, jmsHeaders); // update virtualhost (now with two aliases)

        boolean resultExistVirtualhost = driver.exist(virtualhostProperties);
        boolean resultExistAnAlias     = driver.exist(anAliasProperties);
        boolean resultExistOtherAlias  = driver.exist(otherAliasProperties);

        // then
        Assert.isTrue(resultExistVirtualhost && resultExistAnAlias && resultExistOtherAlias);
    }

    @Test
    public void promoteAliasToVirtualhost() {
        logTestedMethod();

        // given
        String virtualhostName = UUID.randomUUID().toString();
        String anAliasName = UUID.randomUUID().toString();

        VirtualHost virtualHost = virtualhostFactory.build(virtualhostName);
        Properties anAliasProperties = virtualhostEngine.makeProperties(virtualhostFactory.build(anAliasName), jmsHeaders);
        Set<String> aliases = new HashSet<>();
        Set<Rule> rules = Collections.emptySet();

        // when
        aliases.add(anAliasName); // add only one alias
        virtualHost.setAliases(aliases);
        virtualHost.setRules(rules);
        virtualhostEngine.create(virtualHost, jmsHeaders); // create virtualhost with one alias
        virtualHost.setName(anAliasName); // update name
        Properties virtualhostProperties = virtualhostEngine.makeProperties(virtualhostFactory.build(virtualhostName), jmsHeaders);
        aliases.clear(); // clear aliases
        virtualHost.setAliases(aliases);
        driver.remove(anAliasProperties); // remove aliases
        virtualhostEngine.update(virtualHost, jmsHeaders); // update virtualhost (really?)

        boolean resultExistVirtualhost = driver.exist(virtualhostProperties);
        boolean resultExistAnAlias     = !driver.exist(anAliasProperties);

        // then
        Assert.isTrue(resultExistVirtualhost && resultExistAnAlias);
    }

}
