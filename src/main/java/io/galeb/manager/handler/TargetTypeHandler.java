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

import static io.galeb.manager.entity.AbstractEntity.EntityStatus.OK;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.HandleAfterDelete;
import org.springframework.data.rest.core.annotation.HandleAfterSave;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeDelete;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;

import io.galeb.manager.entity.TargetType;

@RepositoryEventHandler(TargetType.class)
public class TargetTypeHandler {

    private static Log LOGGER = LogFactory.getLog(TargetTypeHandler.class);

    @HandleBeforeCreate
    public void beforeCreate(TargetType targetType) {
        LOGGER.info("TargetType: HandleBeforeCreate");
        targetType.setStatus(OK);
    }

    @HandleAfterCreate
    public void afterCreate(TargetType targetType) {
        LOGGER.info("TargetType: HandleAfterCreate");
    }

    @HandleBeforeSave
    public void beforeSave(TargetType targetType) {
        LOGGER.info("TargetType: HandleBeforeSave");
    }

    @HandleAfterSave
    public void afterSave(TargetType targetType) {
        LOGGER.info("TargetType: HandleAfterSave");
    }

    @HandleBeforeDelete
    public void beforeDelete(TargetType targetType) {
        LOGGER.info("TargetType: HandleBeforeDelete");
    }

    @HandleAfterDelete
    public void afterDelete(TargetType targetType) {
        LOGGER.info("TargetType: HandleAfterDelete");
    }

}
