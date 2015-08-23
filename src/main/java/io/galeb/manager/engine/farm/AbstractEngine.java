/*
 *   Galeb - Load Balance as a Service Plataform
 *
 *   Copyright (C) 2014-2015 Globo.com
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

package io.galeb.manager.engine.farm;

import java.util.Optional;

import org.springframework.security.core.Authentication;

import io.galeb.manager.common.Properties;
import io.galeb.manager.engine.Driver;
import io.galeb.manager.engine.DriverBuilder;
import io.galeb.manager.engine.Provisioning;
import io.galeb.manager.entity.AbstractEntity;
import io.galeb.manager.entity.Farm;
import io.galeb.manager.repository.FarmRepository;
import io.galeb.manager.security.CurrentUser;
import io.galeb.manager.security.SystemUserService;

public abstract class AbstractEngine {

    protected abstract Optional<Farm> findFarm(AbstractEntity<?> entity);

    protected Properties fromEntity(AbstractEntity<?> entity) {
        Properties properties = new Properties();
        properties.put("api", findApi(entity));
        return properties;
    }

    protected String findApi(AbstractEntity<?> entity) {
        Optional<Farm> farm = findFarm(entity);
        return farm.isPresent() ? farm.get().getApi() : "UNDEF";
    }

    protected Driver getDriver(AbstractEntity<?> entity) {
        String driverName = Driver.DEFAULT_DRIVER_NAME;
        Optional<Farm> farm = findFarm(entity);
        if (farm.isPresent()) {
            driverName = farm.get().getProvider().getDriver();
        }
        return DriverBuilder.build(driverName);
    }

    protected Provisioning getProvisioning(AbstractEntity<?> entity) {
        return new Provisioning() {
            // NULL
        };
    }

    protected Optional<Farm> findFarmById(final FarmRepository farmRepository, long farmId) {
        final Authentication originalAuth = CurrentUser.getCurrentAuth();
        SystemUserService.runAs();
        Optional<Farm> farm = Optional.ofNullable(farmRepository.findOne(farmId));
        SystemUserService.runAs(originalAuth);
        return farm;
    }
}
