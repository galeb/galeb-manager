package io.galeb.manager.handler;

import static io.galeb.manager.entity.AbstractEntity.EntityStatus.OK;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.HandleAfterDelete;
import org.springframework.data.rest.core.annotation.HandleAfterSave;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeDelete;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.jms.core.JmsTemplate;

import io.galeb.manager.engine.farm.FarmEngine;
import io.galeb.manager.entity.Farm;
import io.galeb.manager.repository.FarmRepository;

@RepositoryEventHandler(Farm.class)
public class FarmHandler extends RoutableToEngine<Farm> {

    private static Log LOGGER = LogFactory.getLog(FarmHandler.class);

    @Autowired
    private JmsTemplate jms;

    @Autowired
    private FarmRepository farmRepository;

    @Override
    protected void setBestFarm(Farm entity) {
        // its me !!!
    }

    public FarmHandler() {
        setQueueCreateName(FarmEngine.QUEUE_CREATE);
        setQueueUpdateName(FarmEngine.QUEUE_UPDATE);
        setQueueRemoveName(FarmEngine.QUEUE_REMOVE);
    }

    @HandleBeforeCreate
    public void beforeCreate(Farm farm) throws Exception {
        beforeCreate(farm, LOGGER);
        farm.setStatus(OK);
    }

    @HandleAfterCreate
    public void afterCreate(Farm farm) throws Exception {
        afterCreate(farm, jms, LOGGER);
    }

    @HandleBeforeSave
    public void beforeSave(Farm farm) throws Exception {
        beforeSave(farm, farmRepository, LOGGER);
    }

    @HandleAfterSave
    public void afterSave(Farm farm) throws Exception {
        afterSave(farm, jms, LOGGER);
    }

    @HandleBeforeDelete
    public void beforeDelete(Farm farm) throws Exception {
        beforeDelete(farm, LOGGER);
    }

    @HandleAfterDelete
    public void afterDelete(Farm farm) throws Exception {
        afterDelete(farm, jms, LOGGER);
    }

}
