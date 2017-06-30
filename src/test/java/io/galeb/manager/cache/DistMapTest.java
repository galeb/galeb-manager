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

package io.galeb.manager.cache;

import io.galeb.core.json.JsonObject;
import io.galeb.core.model.Entity;
import io.galeb.manager.entity.AbstractEntity;
import io.galeb.manager.entity.Environment;
import io.galeb.manager.entity.Project;
import io.galeb.manager.entity.VirtualHost;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.springframework.util.Assert;

import java.io.File;

import static io.galeb.manager.cache.DistMap.DIST_MAP_FARM_ID_PROP;

public class DistMapTest {

    private String nameEntityDefault = "EntityDefault";
    private long farmIdDefault = 1L;
    private String versionDefault = "1";

    @ClassRule
    public static final EnvironmentVariables environmentVariables
            = new EnvironmentVariables();

    @BeforeClass
    public static void before() {
        File resourcesDirectory = new File("src/test/resources");
        environmentVariables.set("PWD", resourcesDirectory.getAbsolutePath());
    }

    @Test
    public void putAndGetValues() {

        DistMap mapDistributed = DistMap.getInstance();

        Entity entity = getEntityDefault();
        VirtualHost virtualHost = getVirtualHostDefault();

        mapDistributed.put(entity, JsonObject.toJsonString(entity));

        String returnValue = mapDistributed.get(virtualHost);
        Assert.notNull(returnValue);

        AbstractEntity.EntityStatus status = virtualHost.getStatus();
        Assert.isTrue(status.equals(AbstractEntity.EntityStatus.OK));
    }

    @Test
    public void removeAllKeysByFarm() {

        DistMap mapDistributed = DistMap.getInstance();

        Entity entity = getEntityDefault();
        mapDistributed.put(entity, JsonObject.toJsonString(entity));

        VirtualHost virtualHost = getVirtualHostDefault();

        mapDistributed.resetFarm(virtualHost.getFarmId());

        String returnValue = mapDistributed.get(virtualHost);
        Assert.isNull(returnValue);

    }

    private Entity getEntityDefault() {
        Entity entity = new Entity();
        String id = nameEntityDefault;
        String version = versionDefault;
        String entityType = "virtualhost";
        String parentId = "";
        entity.setId(id);
        entity.setParentId(parentId);
        entity.setVersion(Integer.parseInt(version));
        entity.setEntityType(entityType);
        entity.getProperties().put(DIST_MAP_FARM_ID_PROP, farmIdDefault);
        return entity;
    }

    private VirtualHost getVirtualHostDefault() {
        Environment env = new Environment("Env-Test");
        Project prj = new Project("Prj-Test");
        VirtualHost virtualHost = new VirtualHost(nameEntityDefault, env, prj);
        virtualHost.setFarmId(farmIdDefault);
        virtualHost.setHash(Integer.parseInt(versionDefault));
        return virtualHost;
    }

}
