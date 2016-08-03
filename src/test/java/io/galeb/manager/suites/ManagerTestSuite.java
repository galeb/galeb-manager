package io.galeb.manager.suites;

import io.galeb.core.cluster.ignite.IgniteCacheFactory;
import io.galeb.manager.cucumber.CucumberTest;
import io.galeb.manager.cache.DistMapTest;
import org.apache.ignite.Ignite;
import org.junit.AfterClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({DistMapTest.class, CucumberTest.class})
public class ManagerTestSuite {

    @AfterClass
    public static void after() {
        ((Ignite) IgniteCacheFactory.getInstance().getClusterInstance()).cluster().stopNodes();
    }
}
