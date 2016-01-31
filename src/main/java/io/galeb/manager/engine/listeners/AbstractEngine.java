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

package io.galeb.manager.engine.listeners;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import io.galeb.core.cluster.ClusterLocker;
import io.galeb.core.cluster.ignite.IgniteClusterLocker;
import io.galeb.manager.engine.provisioning.Provisioning;
import io.galeb.manager.engine.provisioning.impl.NullProvisioning;
import io.galeb.manager.engine.util.ManagerToFarmConverter;
import io.galeb.manager.entity.AbstractEntity;
import io.galeb.manager.entity.Farm;
import io.galeb.manager.entity.WithFarmID;
import io.galeb.manager.queue.FarmQueue;
import org.apache.commons.logging.Log;
import org.springframework.security.core.Authentication;

import io.galeb.manager.common.Properties;
import io.galeb.manager.repository.FarmRepository;
import io.galeb.manager.security.user.CurrentUser;
import io.galeb.manager.security.services.SystemUserService;

public abstract class AbstractEngine<T> {

    public static final String SEPARATOR = "__";

    protected abstract void create(T entity);

    protected abstract void remove(T entity);

    protected abstract void update(T entity);

    protected abstract FarmRepository getFarmRepository();

    protected abstract FarmQueue farmQueue();

    protected abstract Log getLogger();

    protected ClusterLocker locker = IgniteClusterLocker.INSTANCE;

    protected Optional<Farm> findFarm(AbstractEntity<?> entity) {
        if (entity instanceof Farm) {
            return Optional.of((Farm)entity);
        }
        long farmId = -1L;
        if (entity instanceof WithFarmID) {
            farmId = ((WithFarmID<?>)entity).getFarmId();
        }
        return findFarmById(farmId);
    }

    protected Properties fromEntity(AbstractEntity<?> entity) {
        Properties properties = new Properties();
        properties.put("api", findApi(entity));
        return properties;
    }

    protected String findApi(AbstractEntity<?> entity) {
        Optional<Farm> farm = findFarm(entity);
        return farm.isPresent() ? farm.get().getApi() : "UNDEF";
    }

    protected Provisioning getProvisioning(AbstractEntity<?> entity) {
        return new NullProvisioning();
    }

    private Optional<Farm> findFarmById(long farmId) {
        final Authentication originalAuth = CurrentUser.getCurrentAuth();
        SystemUserService.runAs();
        Optional<Farm> farm = Optional.ofNullable(getFarmRepository().findOne(farmId));
        SystemUserService.runAs(originalAuth);
        return farm;
    }

    protected String getManagerEntityType(String farmEntityType) {
        Class<?> internalEntityType = ManagerToFarmConverter.FARM_TO_MANAGER_ENTITY_MAP.get(farmEntityType);
        return internalEntityType != null ? internalEntityType.getSimpleName().toLowerCase() : null;
    }

}
