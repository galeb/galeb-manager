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
import io.galeb.manager.routermap.RouterState;
import org.apache.commons.logging.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.PagingAndSortingRepository;

import io.galeb.manager.entity.AbstractEntity;

public abstract class AbstractHandler<T extends AbstractEntity<?>> {

    protected abstract void setBestFarm(T entity) throws Exception;

    @Autowired
    private RouterState routerState;

    private void registerHasChange(T entity) {
        routerState.registerChanges(entity);
    }

    public void beforeCreate(T entity, Log logger) throws Exception {
        logger.info(entity.getClass().getSimpleName()+": HandleBeforeCreate");
        setBestFarm(entity);
        if (entity instanceof WithFarmID && ((WithFarmID)entity).getFarmId() < 0) {
            throw new BadRequestException("Farm does not exists");
        }
    }

    public void afterCreate(T entity, Log logger) throws Exception {
        registerHasChange(entity);
        logger.info(entity.getClass().getSimpleName()+": HandleAfterCreate");
    }

    public void beforeSave(T entity, PagingAndSortingRepository<T, Long> repository, Log logger) throws Exception {
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
        registerHasChange(entity);
        String entityTypeName = entity.getClass().getSimpleName();
        logger.info(entityTypeName+": HandleAfterSave");
        if (entity.isSaveOnly()) {
            logger.info(entityTypeName+": SaveOnly enabled");
            entity.setSaveOnly(false);
        }
    }

    public void beforeDelete(T entity, Log logger) {
        logger.info(entity.getClass().getSimpleName()+": HandleBeforeDelete");
    }

    public void afterDelete(T entity, Log logger) throws Exception {
        registerHasChange(entity);
        logger.info(entity.getClass().getSimpleName()+": HandleAfterDelete");
    }

}
