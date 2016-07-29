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
import org.springframework.security.access.prepost.PreAuthorize;

import io.galeb.manager.entity.Team;

@RepositoryEventHandler(Team.class)
public class TeamHandler {

    private static Log LOGGER = LogFactory.getLog(TeamHandler.class);

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @HandleBeforeCreate
    public void beforeCreate(Team team) {
        LOGGER.info("Team: HandleBeforeCreate");
    }

    @HandleAfterCreate
    public void afterCreate(Team team) {
        LOGGER.info("Team: HandleAfterCreate");
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @HandleBeforeSave
    public void beforeSave(Team team) {
        LOGGER.info("Team: HandleBeforeSave");
    }

    @HandleAfterSave
    public void afterSave(Team team) {
        LOGGER.info("Team: HandleAfterSave");
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @HandleBeforeDelete
    public void beforeDelete(Team team) {
        team.getAccounts().stream().forEach(account -> account.getTeams().remove(team));
        team.getProjects().stream().forEach(project -> project.getTeams().remove(team));
        LOGGER.info("Team: HandleBeforeDelete");
    }

    @HandleAfterDelete
    public void afterDelete(Team team) {
        LOGGER.info("Team: HandleAfterDelete");
    }

}
