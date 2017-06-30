/*
 * Copyright (c) 2014-2017 Globo.com - ATeam
 * All rights reserved.
 *
 * This source is subject to the Apache License, Version 2.0.
 * Please see the LICENSE file for more information.
 *
 * Authors: See AUTHORS file
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.galeb.manager.entity;

import io.galeb.manager.cache.DistMap;
import io.galeb.manager.routermap.RouterMap;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static io.galeb.manager.entity.AbstractEntity.EntityStatus.*;
import static io.galeb.manager.routermap.RouterMap.State.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AbstractEntitySyncronizableTest {

    private RouterMap routerMap = mock(RouterMap.class);
    private DistMap distMap = mock(DistMap.class);
    private Farm farm;
    private AbstractEntity entity;

    /**
     * field1 = farm enable?
     * field2 = distMap.get result
     * field3 = routeMap.state result
     * field4 = STATUS EXPECTED
     */
    private List<Object[]> truthTable = Arrays.asList(
            new Object[]{false, null,    EMPTY,  PENDING}, //1
            new Object[]{false, null,    SYNC,   OK},      //2
            new Object[]{false, null,    NOSYNC, PENDING}, //3
            new Object[]{false, OK,      EMPTY,  PENDING}, //4
            new Object[]{false, PENDING, EMPTY,  PENDING}, //5
            new Object[]{false, OK,      NOSYNC, PENDING}, //6
            new Object[]{false, PENDING, NOSYNC, PENDING}, //7
            new Object[]{false, OK,      SYNC,   OK},      //8
            new Object[]{false, PENDING, SYNC,   OK},      //9
            new Object[]{true,  null,    EMPTY,  PENDING}, //10
            new Object[]{true,  null,    SYNC,   PENDING}, //11
            new Object[]{true,  null,    NOSYNC, PENDING}, //12
            new Object[]{true,  OK,      EMPTY,  OK},      //13
            new Object[]{true,  PENDING, EMPTY,  PENDING}, //14
            new Object[]{true,  OK,      NOSYNC, PENDING}, //15
            new Object[]{true,  PENDING, NOSYNC, PENDING}, //16
            new Object[]{true,  OK,      SYNC,   OK},      //17
            new Object[]{true,  PENDING, SYNC,   PENDING}  //18
            );

    public Farm getFarmTest() {
        return farm;
    }

    public DistMap getDistMapTest() {
        return distMap;
    }

    public RouterMap getRouterMapTest() {
        return routerMap;
    }

    @Before
    public void setup() {
        entity = new VirtualHost() {
            @Override
            protected DistMap getDistMap() { return getDistMapTest(); }
            @Override
            protected RouterMap getRouterMap() { return getRouterMapTest(); }
            @Override
            protected String getEnvName() { return "NULL"; }
            @Override
            public Farm getFarm() { return getFarmTest(); }
        };
        farm = new Farm() {
            @Override
            protected DistMap getDistMap() { return getDistMapTest(); }
            @Override
            protected RouterMap getRouterMap() { return getRouterMapTest(); }
            @Override
            public String getEnvName() { return "NULL"; }
        };
    }

    @Test
    public void instanceOfAbstractEntitySyncronizable() {
        assertThat(entity, instanceOf(AbstractEntitySyncronizable.class));
    }

    @Test
    public void checkEntityTruthTable() {
        final AtomicInteger linePos = new AtomicInteger(0);
        truthTable.forEach(f -> {
            int line = linePos.incrementAndGet();
            farm.setAutoReload((boolean) f[0]);
            when(distMap.get(entity)).thenReturn(f[1] == null ? null : (f[1]).toString());
            when(routerMap.state(anyString())).thenReturn((RouterMap.State) f[2]);
            try {
                assertThat(entity.getDynamicStatus(), equalTo((AbstractEntity.EntityStatus) f[3]));
            } catch (AssertionError e) {
                System.out.println(e.getMessage() + " in line " + line);
                throw e;
            }
        });
    }

    @Test
    public void checkFarmTruthTable() {
        final AtomicInteger linePos = new AtomicInteger(0);
        truthTable.forEach(f -> {
            int line = linePos.incrementAndGet();
            farm.setAutoReload((boolean) f[0]);
            when(distMap.get(farm)).thenReturn(f[1] == null ? null : (f[1]).toString());
            when(routerMap.state(anyString())).thenReturn((RouterMap.State) f[2]);
            try {
                assertThat(farm.getDynamicStatus(), equalTo((AbstractEntity.EntityStatus) f[3]));
            } catch (AssertionError e) {
                System.out.println(e.getMessage() + " in line " + line);
                throw e;
            }
        });
    }
}
