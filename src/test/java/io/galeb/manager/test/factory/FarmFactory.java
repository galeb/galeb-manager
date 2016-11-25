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

import io.galeb.manager.engine.VirtualhostDriverTest;
import io.galeb.manager.engine.driver.impl.GalebV32Driver;
import io.galeb.manager.entity.*;
import io.galeb.manager.repository.FarmRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FarmFactory extends AbstractFactory<Farm> {

    private static final Log LOGGER = LogFactory.getLog(FarmFactory.class);

    @Override
    public Farm build(String api) {
        Environment environment = new Environment("NULL");
        Provider provider = new Provider(GalebV32Driver.class.getSimpleName());
        provider.setDriver(GalebV32Driver.class.getSimpleName().replaceAll("Driver", ""));
        String name = UUID.randomUUID().toString();
        String domain = UUID.randomUUID().toString();
        return new Farm(name, domain, api, environment, provider);
    }

    public Map<String, List<?>> entitiesMap() {
        final Map<String, List<?>> entitiesMap = new HashMap<>();
        entitiesMap.put(VirtualHost.class.getSimpleName().toLowerCase(), new ArrayList<VirtualHost>());
        entitiesMap.put(Pool.class.getSimpleName().toLowerCase(), new ArrayList<Pool>());
        entitiesMap.put(Target.class.getSimpleName().toLowerCase(), new ArrayList<Target>());
        entitiesMap.put(Rule.class.getSimpleName().toLowerCase(), new ArrayList<Rule>());
        return entitiesMap;
    }

    public FarmRepository mockFarmRepository() {
        Farm farm = build(UUID.randomUUID().toString());
        FarmRepository mockFarmRepository = mock(FarmRepository.class);
        when(mockFarmRepository.findOne(anyLong())).thenReturn(farm);
        return mockFarmRepository;
    }
}
