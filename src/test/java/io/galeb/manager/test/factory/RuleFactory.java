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

package io.galeb.manager.test.factory;

import io.galeb.manager.engine.driver.Driver;
import io.galeb.manager.engine.listeners.RuleEngine;
import io.galeb.manager.entity.Pool;
import io.galeb.manager.entity.Rule;
import io.galeb.manager.entity.RuleType;

import java.util.UUID;

public class RuleFactory extends AbstractFactory<Rule> {

    private Pool pool = null;
    private RuleType ruleType = null;
    private PoolFactory poolFactory = new PoolFactory();

    @Override
    public Rule build(String arg) {
        if (pool == null) {
            pool = poolFactory.build(UUID.randomUUID().toString());
        }
        if (ruleType == null) {
            ruleType = new RuleType(UUID.randomUUID().toString());
        }
        Rule rule = new Rule(arg, ruleType, pool);
        pool = null;
        ruleType = null;
        return rule;
    }

    public RuleFactory withPool(final Pool aPool) {
        this.pool = aPool;
        return this;
    }

    public RuleFactory withRuleType(final RuleType aRuleType) {
        this.ruleType = aRuleType;
        return this;
    }

    public RuleEngine buildRuleEngine(Driver driver) {
        return (RuleEngine) new RuleEngine().setFarmRepository(new FarmFactory().mockFarmRepository())
                                            .setDriver(driver);
    }
}
