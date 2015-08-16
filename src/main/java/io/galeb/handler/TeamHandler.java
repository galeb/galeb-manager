package io.galeb.handler;

import static io.galeb.entity.AbstractEntity.EntityStatus.OK;

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

import io.galeb.entity.Team;

@RepositoryEventHandler(Team.class)
public class TeamHandler {

    private static Log LOGGER = LogFactory.getLog(TeamHandler.class);

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @HandleBeforeCreate
    public void beforeCreate(Team team) {
        LOGGER.info("Team: HandleBeforeCreate");
        team.setStatus(OK);
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
        LOGGER.info("Team: HandleBeforeDelete");
    }

    @HandleAfterDelete
    public void afterDelete(Team team) {
        LOGGER.info("Team: HandleAfterDelete");
    }

}
