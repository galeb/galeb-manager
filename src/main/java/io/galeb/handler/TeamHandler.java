package io.galeb.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.HandleAfterDelete;
import org.springframework.data.rest.core.annotation.HandleAfterSave;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeDelete;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;

import io.galeb.entity.AbstractEntity.EntityStatus;
import io.galeb.entity.Team;

@RepositoryEventHandler(Team.class)
public class TeamHandler {

    private static Log LOGGER = LogFactory.getLog(TeamHandler.class);

    @HandleBeforeCreate
    public void beforeCreate(Team team) {
        LOGGER.info("Team: HandleBeforeCreate");
        team.setStatus(EntityStatus.OK);
    }

    @HandleAfterCreate
    public void afterCreate(Team team) {
        LOGGER.info("Team: HandleAfterCreate");
    }

    @HandleBeforeSave
    public void beforeSave(Team team) {
        LOGGER.info("Team: HandleBeforeSave");
    }

    @HandleAfterSave
    public void afterSave(Team team) {
        LOGGER.info("Team: HandleAfterSave");
    }

    @HandleBeforeDelete
    public void beforeDelete(Team team) {
        LOGGER.info("Team: HandleBeforeDelete");
    }

    @HandleAfterDelete
    public void afterDelete(Team team) {
        LOGGER.info("Team: HandleAfterDelete");
    }

}
