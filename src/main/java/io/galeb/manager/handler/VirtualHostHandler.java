package io.galeb.manager.handler;

import io.galeb.manager.engine.farm.VirtualHostEngine;
import io.galeb.manager.entity.Farm;
import io.galeb.manager.entity.VirtualHost;
import io.galeb.manager.entity.AbstractEntity.EntityStatus;
import io.galeb.manager.repository.FarmRepository;
import io.galeb.manager.repository.VirtualHostRepository;

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

@RepositoryEventHandler(VirtualHost.class)
public class VirtualHostHandler extends RoutableToEngine<VirtualHost> {

    private static final Log LOGGER = LogFactory.getLog(VirtualHostHandler.class);

    @Autowired
    private JmsTemplate jms;

    @Autowired
    private FarmRepository farmRepository;

    @Autowired
    private VirtualHostRepository virtualHostRepository;

    public VirtualHostHandler() {
        setQueueCreateName(VirtualHostEngine.QUEUE_CREATE);
        setQueueUpdateName(VirtualHostEngine.QUEUE_UPDATE);
        setQueueRemoveName(VirtualHostEngine.QUEUE_REMOVE);
    }

    @Override
    protected void setBestFarm(final VirtualHost virtualhost) {
        final Farm farm = farmRepository.findByEnvironmentAndStatus(virtualhost.getEnvironment(), EntityStatus.OK)
                .stream().findFirst().orElse(null);
        if (farm!=null) {
            virtualhost.setFarmId(farm.getId());
        }
    }

    @HandleBeforeCreate
    public void beforeCreate(VirtualHost virtualhost) throws Exception {
        virtualhost.setFarmId(-1L);
        beforeCreate(virtualhost, LOGGER);
    }

    @HandleAfterCreate
    public void afterCreate(VirtualHost virtualhost) throws Exception {
        afterCreate(virtualhost, jms, LOGGER);
    }

    @HandleBeforeSave
    public void beforeSave(VirtualHost virtualhost) throws Exception {
        beforeSave(virtualhost, virtualHostRepository, LOGGER);
    }

    @HandleAfterSave
    public void afterSave(VirtualHost virtualhost) throws Exception {
        afterSave(virtualhost, jms, LOGGER);
    }

    @HandleBeforeDelete
    public void beforeDelete(VirtualHost virtualhost) {
        beforeDelete(virtualhost, LOGGER);
    }

    @HandleAfterDelete
    public void afterDelete(VirtualHost virtualhost) throws Exception {
        afterDelete(virtualhost, jms, LOGGER);
    }

}
