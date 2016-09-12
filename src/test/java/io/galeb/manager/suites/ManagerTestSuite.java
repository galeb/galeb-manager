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

package io.galeb.manager.suites;

import io.galeb.core.cluster.ignite.IgniteCacheFactory;
import io.galeb.manager.cucumber.CucumberTest;
import io.galeb.manager.cache.DistMapTest;
import io.galeb.manager.engine.driver.impl.FarmDriverTest;
import io.galeb.manager.engine.driver.impl.PoolDriverTest;
import org.apache.ignite.Ignite;
import org.junit.AfterClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({FarmDriverTest.class, PoolDriverTest.class, DistMapTest.class, CucumberTest.class})
public class ManagerTestSuite {

    @AfterClass
    public static void after() {
        ((Ignite) IgniteCacheFactory.getInstance().getClusterInstance()).cluster().stopNodes();
    }
}
