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

import io.galeb.manager.jms.AbstractJmsEnqueuer;
import io.galeb.manager.jms.FarmQueue;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.security.core.Authentication;

import io.galeb.manager.common.Properties;
import io.galeb.manager.engine.Provisioning;
import io.galeb.manager.entity.AbstractEntity;
import io.galeb.manager.entity.Farm;
import io.galeb.manager.entity.WithFarmID;
import io.galeb.manager.entity.AbstractEntity.EntityStatus;
import io.galeb.manager.repository.FarmRepository;
import io.galeb.manager.security.CurrentUser;
import io.galeb.manager.security.SystemUserService;

public abstract class AbstractEngine {

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
        return new Provisioning() {
            // NULL
        };
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
}
