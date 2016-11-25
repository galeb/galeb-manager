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

package io.galeb.manager.engine;

import io.galeb.manager.common.Properties;
import io.galeb.manager.engine.driver.Driver;
import io.galeb.manager.engine.driver.DriverBuilder;
import io.galeb.manager.engine.driver.impl.GalebV32Driver;
import io.galeb.manager.engine.listeners.RuleEngine;
import io.galeb.manager.entity.Rule;
import io.galeb.manager.entity.VirtualHost;
import io.galeb.manager.httpclient.FakeFarmClient;
import io.galeb.manager.test.factory.RuleFactory;
import io.galeb.manager.test.factory.VirtualhostFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.Assert;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class RuleDriveTest {

    private static final Log LOGGER = LogFactory.getLog(GalebV32Driver.class);


    private final RuleFactory ruleFactory = new RuleFactory();
    private final VirtualhostFactory virtualhostFactory = new VirtualhostFactory();
    private final FakeFarmClient fakeFarmClient = new FakeFarmClient();
    private final Driver driver = DriverBuilder.build(GalebV32Driver.DRIVER_NAME).addResource(fakeFarmClient);
    private final RuleEngine ruleEngine = ruleFactory.buildRuleEngine(driver);


    private void logTestedMethod() {
        LOGGER.info("Testing " + this.getClass().getSimpleName() + "." +
                Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Before
    public void setUp() {
        fakeFarmClient.deleteAll();
    }

    @Test
    public void ruleNotExist() {
        logTestedMethod();

        // given
        String ruleName = UUID.randomUUID().toString();
        String virtualhostName = UUID.randomUUID().toString();
        Properties properties = ruleEngine.makeProperties(ruleFactory.build(ruleName), virtualhostFactory.build(virtualhostName), ruleFactory.jmsHeaderProperties());

        // when
        boolean exist = !driver.exist(properties);

        // then
        Assert.isTrue(exist);

    }

    @Test
    public void ruleNotExistIfNotHaveParents() {
        logTestedMethod();

        // given
        String ruleName = UUID.randomUUID().toString();
        String virtualhostName = UUID.randomUUID().toString();
        Rule rule = ruleFactory.build(ruleName);
        Properties properties = ruleEngine.makeProperties(rule, virtualhostFactory.build(virtualhostName), ruleFactory.jmsHeaderProperties());

        // when
        ruleEngine.create(rule, ruleFactory.jmsHeaderProperties());
        boolean exist = !driver.exist(properties);

        // then
        Assert.isTrue(exist);
    }

    @Test
    public void ruleExistIfHaveParents() {
        logTestedMethod();

        // given
        String ruleName = UUID.randomUUID().toString();
        String virtualhostName = UUID.randomUUID().toString();
        Rule rule = ruleFactory.build(ruleName);
        VirtualHost virtualHost = virtualhostFactory.build(virtualhostName);
        Set<VirtualHost> virtualhosts = new HashSet<>();
        virtualhosts.add(virtualHost);
        rule.setParents(virtualhosts);

        // when
        Properties properties = ruleEngine.makeProperties(rule, virtualHost, ruleFactory.jmsHeaderProperties());
        ruleEngine.create(rule, ruleFactory.jmsHeaderProperties());
        boolean exist = driver.exist(properties);

        // then
        Assert.isTrue(exist);
    }

}
