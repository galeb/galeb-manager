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

import io.galeb.manager.common.Properties;
import io.galeb.manager.engine.listeners.PoolEngine;
import io.galeb.manager.entity.BalancePolicy;
import io.galeb.manager.entity.BalancePolicyType;
import io.galeb.manager.entity.Pool;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

public class PoolFactory {

    private static final Log LOGGER = LogFactory.getLog(PoolFactory.class);

    public Pool build() {
        final Pool pool= new Pool(UUID.randomUUID().toString());
        final BalancePolicyType balancePolicyType = new BalancePolicyType("DEFAULT");
        final BalancePolicy balancePolicy = new BalancePolicy("DEFAULT", balancePolicyType);
        pool.setBalancePolicy(balancePolicy);
        pool.updateHash();
        return pool;
    }

    public Properties makeProperties(Pool pool) {
        Map<String, String> jmsHeaderProperties = new HashMap<>();
        jmsHeaderProperties.put("api", "api");
        return new PoolEngine().makeProperties(pool, jmsHeaderProperties);
    }

}
