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

import java.util.Optional;

import io.galeb.core.model.Backend;
import io.galeb.core.model.BackendPool;
import io.galeb.core.model.Rule;
import io.galeb.core.model.VirtualHost;
import io.galeb.manager.engine.provisioning.Provisioning;
import io.galeb.manager.engine.provisioning.impl.NullProvisioning;
import io.galeb.manager.entity.AbstractEntity;
import io.galeb.manager.entity.Farm;
import io.galeb.manager.entity.Pool;
import io.galeb.manager.entity.Target;
import io.galeb.manager.entity.WithFarmID;
import io.galeb.manager.queue.FarmQueue;
import io.galeb.manager.redis.DistributedLocker;
import org.springframework.security.core.Authentication;

import io.galeb.manager.common.Properties;
import io.galeb.manager.entity.AbstractEntity.EntityStatus;
import io.galeb.manager.repository.FarmRepository;
import io.galeb.manager.security.user.CurrentUser;
import io.galeb.manager.security.services.SystemUserService;

public abstract class AbstractEngine<T> {

    protected abstract void create(T entity);

    protected abstract void remove(T entity);

    protected abstract void update(T entity);

    protected abstract FarmRepository getFarmRepository();

    protected abstract FarmQueue farmQueue();

    protected Optional<Farm> findFarm(AbstractEntity<?> entity) {
        if (entity instanceof Farm) {
            return Optional.ofNullable((Farm)entity);
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

    protected void setFarmStatusOnError(AbstractEntity<?> entity) {
        if (entity.getStatus().equals(EntityStatus.ERROR)) {
            Optional<Farm> farm = findFarm(entity);
            if (farm.isPresent()) {
                farm.get().setStatus(EntityStatus.ERROR);
                farmQueue().sendToQueue(FarmQueue.QUEUE_CALLBK, farm.get());
            }
        }
    }

    protected String getInternalEntityType(String entityType) {
        return entityType.toLowerCase().equals(BackendPool.class.getSimpleName().toLowerCase()) ?
                Pool.class.getSimpleName().toLowerCase() :
                entityType.toLowerCase().equals(Backend.class.getSimpleName().toLowerCase()) ?
                        Target.class.getSimpleName().toLowerCase() : entityType;
    }

    protected Class<?> getExternalEntityType(String entityType) {
        return entityType.toLowerCase().equals(
                Pool.class.getSimpleName().toLowerCase()) ? BackendPool.class :
                entityType.toLowerCase().equals(
                        Target.class.getSimpleName().toLowerCase()) ? Backend.class :
                        entityType.toLowerCase().equals(
                                VirtualHost.class.getSimpleName().toLowerCase()) ? VirtualHost.class :
                                entityType.toLowerCase().equals(
                                        Rule.class.getSimpleName().toLowerCase()) ? Rule.class : null;
    }

    protected void releaseLocks(AbstractEntity<?> entity, String parentName, final DistributedLocker distributedLocker) {
        String lockPrefix = DistributedLocker.LOCK_PREFIX + ((WithFarmID)entity).getFarmId() +
                "." + getExternalEntityType(entity.getClass().getSimpleName().toLowerCase()).getSimpleName();
        distributedLocker.release(lockPrefix + "__" + entity.getName() + "__" + parentName);
        if (!distributedLocker.containsLockWithPrefix(lockPrefix + "__")) {
            distributedLocker.release(lockPrefix);
        }
    }
}
