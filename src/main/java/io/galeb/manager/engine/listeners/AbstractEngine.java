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

import java.util.Map;
import java.util.Optional;

import io.galeb.manager.engine.driver.Driver;
import io.galeb.manager.engine.driver.DriverBuilder;
import io.galeb.manager.engine.provisioning.Provisioning;
import io.galeb.manager.engine.provisioning.impl.NullProvisioning;
import io.galeb.manager.engine.util.ManagerToFarmConverter;
import io.galeb.manager.entity.AbstractEntity;
import io.galeb.manager.entity.Farm;
import io.galeb.manager.entity.VirtualHost;
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
    public static final String JSON_PROP = "json";
    public static final String PATH_PROP = "path";
    public static final String API_PROP  = "api";
    public static final String PARENTID_PROP  = "parentId";

    private Driver driver = null;

    public AbstractEngine<T> setDriver(Driver driver) {
        this.driver = driver;
        return this;
    }

    public Driver getDriver(AbstractEntity<?> entity) {
        final Farm farm = findFarm(entity).orElse(null);
        return entity != null ? DriverBuilder.getDriver(farm) :
                (driver != null ? driver : DriverBuilder.getDriver(null));
    }

    public abstract void create(T entity, final Map<String, String> jmsHeaders);

    public abstract void remove(T entity, final Map<String, String> jmsHeaders);

    public abstract void update(T entity, final Map<String, String> jmsHeaders);

    public abstract Farm getFarmById(long id);

    protected abstract FarmQueue farmQueue();

    protected abstract Log getLogger();

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

    protected Properties fromEntity(AbstractEntity<?> entity, final Map<String, String> jmsHeaders) {
        Properties properties = new Properties();
        jmsHeaders.entrySet().forEach(entry -> {
            properties.put(entry.getKey(), entry.getValue());
        });
        return properties;
    }

    protected Provisioning getProvisioning(AbstractEntity<?> entity) {
        return new NullProvisioning();
    }

    private Optional<Farm> findFarmById(long farmId) {
        final Authentication originalAuth = CurrentUser.getCurrentAuth();
        SystemUserService.runAs();
        Optional<Farm> farm = Optional.ofNullable(getFarmById(farmId));
        SystemUserService.runAs(originalAuth);
        return farm;
    }

    protected String getManagerEntityType(String farmEntityType) {
        Class<?> internalEntityType = ManagerToFarmConverter.FARM_TO_MANAGER_ENTITY_MAP.get(farmEntityType);
        return internalEntityType != null ? internalEntityType.getSimpleName().toLowerCase() : null;
    }

}
