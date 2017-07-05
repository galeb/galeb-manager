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

package io.galeb.manager.handler;

import io.galeb.manager.entity.WithFarmID;
import io.galeb.manager.exceptions.BadRequestException;
import org.apache.commons.logging.Log;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.repository.PagingAndSortingRepository;

import io.galeb.manager.entity.AbstractEntity;

import static io.galeb.manager.entity.AbstractEntitySyncronizable.PREFIX_HAS_CHANGE;

public abstract class AbstractHandler<T extends AbstractEntity<?>> {

    protected abstract void setBestFarm(T entity) throws Exception;

    protected StringRedisTemplate template() {
        // ugly, but NULL is the default
        return null;
    }

    private void registerEnv(T entity) {
        if (template() != null) {
            String env = entity.getEnvName();
            String suffix = entity.getClass().getSimpleName().toLowerCase() + ":" + entity.getId() + ":" + entity.getLastModifiedAt().getTime();
            final ValueOperations<String, String> valueOperations = template().opsForValue();
            valueOperations.setIfAbsent(PREFIX_HAS_CHANGE + ":" + env + ":" + suffix, env);
        }
    }

    public void beforeCreate(T entity, Log logger) throws Exception {
        entity.releaseSync();
        logger.info(entity.getClass().getSimpleName()+": HandleBeforeCreate");
        setBestFarm(entity);
        if (entity instanceof WithFarmID && ((WithFarmID)entity).getFarmId() < 0) {
            throw new BadRequestException("Farm does not exists");
        }
    }

    public void afterCreate(T entity, Log logger) throws Exception {
        registerEnv(entity);
        logger.info(entity.getClass().getSimpleName()+": HandleAfterCreate");
    }

    public void beforeSave(T entity, PagingAndSortingRepository<T, Long> repository, Log logger) throws Exception {
        entity.releaseSync();
        String entityTypeName = entity.getClass().getSimpleName();
        logger.info(entityTypeName+": HandleBeforeSave");
        if (entity.isSaveOnly()) {
            logger.info(entityTypeName+": SaveOnly enabled");
            return;
        }
        setBestFarm(entity);
        if (entity instanceof WithFarmID && ((WithFarmID)entity).getFarmId() < 0) {
            throw new BadRequestException("Farm does not exists");
        }
    }

    public void afterSave(T entity, Log logger) throws Exception {
        registerEnv(entity);
        String entityTypeName = entity.getClass().getSimpleName();
        logger.info(entityTypeName+": HandleAfterSave");
        if (entity.isSaveOnly()) {
            logger.info(entityTypeName+": SaveOnly enabled");
            entity.setSaveOnly(false);
        }
    }

    public void beforeDelete(T entity, Log logger) {
        entity.releaseSync();
        logger.info(entity.getClass().getSimpleName()+": HandleBeforeDelete");
    }

    public void afterDelete(T entity, Log logger) throws Exception {
        registerEnv(entity);
        logger.info(entity.getClass().getSimpleName()+": HandleAfterDelete");
    }

}
