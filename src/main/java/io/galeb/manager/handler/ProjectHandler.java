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

import io.galeb.manager.entity.Project;

@RepositoryEventHandler(Project.class)
public class ProjectHandler {

    private static Log LOGGER = LogFactory.getLog(ProjectHandler.class);

    @HandleBeforeCreate
    public void beforeCreate(Project project) {
        LOGGER.info("Project: HandleBeforeCreate");
        project.setStatus(OK);
    }

    @HandleAfterCreate
    public void afterCreate(Project project) {
        LOGGER.info("Project: HandleAfterCreate");
    }

    @HandleBeforeSave
    public void beforeSave(Project project) {
        LOGGER.info("Project: HandleBeforeSave");
    }

    @HandleAfterSave
    public void afterSave(Project project) {
        LOGGER.info("Project: HandleAfterSave");
    }

    @HandleBeforeDelete
    public void beforeDelete(Project project) {
        LOGGER.info("Project: HandleBeforeDelete");
    }

    @HandleAfterDelete
    public void afterDelete(Project project) {
        LOGGER.info("Project: HandleAfterDelete");
    }

}
