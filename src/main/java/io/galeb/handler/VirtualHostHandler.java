package io.galeb.handler;

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

import io.galeb.engine.farm.VirtualHostEngine;
import io.galeb.entity.AbstractEntity.EntityStatus;
import io.galeb.entity.Farm;
import io.galeb.entity.VirtualHost;
import io.galeb.repository.FarmRepository;

@RepositoryEventHandler(VirtualHost.class)
public class VirtualHostHandler {

    private static final Log LOGGER = LogFactory.getLog(VirtualHostHandler.class);

    @Autowired
    private JmsTemplate jms;

    @Autowired
    private FarmRepository farmRepository;

    private void setBestFarm(final VirtualHost virtualhost) {
        Farm farm = farmRepository.findByEnvironmentAndStatus(virtualhost.getEnvironment(), EntityStatus.OK)
                .stream().findFirst().orElse(null);
        if (farm!=null) {
            virtualhost.setFarmId(farm.getId());
        }
    }

    @HandleBeforeCreate
    public void beforeCreate(VirtualHost virtualhost) {
        LOGGER.info("VirtualHost: HandleBeforeCreate");
        virtualhost.setFarmId(-1L);
        setBestFarm(virtualhost);
    }

    @HandleAfterCreate
    public void afterCreate(VirtualHost virtualhost) {
        LOGGER.info("VirtualHost: HandleAfterCreate");
        jms.convertAndSend(VirtualHostEngine.QUEUE_CREATE, virtualhost);
    }

    @HandleBeforeSave
    public void beforeSave(VirtualHost virtualhost) {
        LOGGER.info("VirtualHost: HandleBeforeSave");
        setBestFarm(virtualhost);
    }

    @HandleAfterSave
    public void afterSave(VirtualHost virtualhost) {
        LOGGER.info("VirtualHost: HandleAfterSave");
        jms.convertAndSend(VirtualHostEngine.QUEUE_UPDATE, virtualhost);
    }

    @HandleBeforeDelete
    public void beforeDelete(VirtualHost virtualhost) {
        LOGGER.info("VirtualHost: HandleBeforeDelete");
    }

    @HandleAfterDelete
    public void afterDelete(VirtualHost virtualhost) {
        LOGGER.info("VirtualHost: HandleAfterDelete");
        jms.convertAndSend(VirtualHostEngine.QUEUE_REMOVE, virtualhost);
    }

}
