package io.galeb.manager.commons;

import io.galeb.manager.cache.DistMap;
import io.galeb.manager.common.StatusDistributed;
import io.galeb.manager.entity.LockStatus;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class StatusDistributedTest {

    private final String farmIdName = "1";
    private final String key_api = "api.dev.local";
    private final Integer value_api = 57;

    @InjectMocks
    private StatusDistributed statusDist;

    @Spy
    private DistMap distMap;

    @ClassRule
    public static final EnvironmentVariables environmentVariables
            = new EnvironmentVariables();

    @BeforeClass
    public static void before() {
        File resourcesDirectory = new File("src/test/resources");
        environmentVariables.set("PWD", resourcesDirectory.getAbsolutePath());
    }

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }


    @Test
    public void updateNewStatus() {

        statusDist.updateNewStatus(farmIdName, true);
        Map<String, Integer> mapApis = new HashMap<>();
        mapApis.put(key_api, value_api);
        statusDist.updateCountDownLatch(farmIdName, mapApis);

        List<LockStatus> listAllLockStatus = statusDist.getLockStatus(farmIdName);
        Assert.notEmpty(listAllLockStatus);
        listAllLockStatus.stream().forEach(lock -> {
            Assert.isTrue(lock.isHasLock());
            Assert.isTrue(lock.getCounterDownLatch().containsKey(key_api));
            Assert.isTrue(lock.getCounterDownLatch().get(key_api) == value_api);
        });

    }

}
