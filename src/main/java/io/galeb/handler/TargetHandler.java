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

import io.galeb.engine.farm.TargetEngine;
import io.galeb.entity.AbstractEntity.EntityStatus;
import io.galeb.entity.Environment;
import io.galeb.entity.Farm;
import io.galeb.entity.Target;
import io.galeb.exceptions.BadRequestException;
import io.galeb.repository.FarmRepository;
import io.galeb.repository.TargetRepository;

@RepositoryEventHandler(Target.class)
public class TargetHandler {

    private static Log LOGGER = LogFactory.getLog(TargetHandler.class);

    @Autowired
    private JmsTemplate jms;

    @Autowired
    private TargetRepository targetRepository;

    @Autowired
    private FarmRepository farmRepository;

    private void setBestFarm(final Target target) {
        long farmId = -1L;
        if (target.getParent() != null) {
            final Target targetParent = target.getParent();
            farmId = targetParent.getFarmId();
        } else {
            final Environment environment = target.getEnvironment();
            if (environment != null) {
                final Farm farm = farmRepository.findByEnvironmentAndStatus(environment, EntityStatus.OK)
                        .stream().findFirst().orElse(null);
                if (farm != null) {
                    farmId = farm.getId();
                }
            } else {
                final String errorMgs = "Target.environment and Target.parent are null";
                LOGGER.error(errorMgs);
                throw new BadRequestException(errorMgs);
            }
        }
        target.setFarmId(farmId);
    }

    @HandleBeforeCreate
    public void beforeCreate(Target target) {
        LOGGER.info("Target: HandleBeforeCreate");
        setBestFarm(target);
        if (target.getParent() != null) {
            if (target.getProject() == null) {
                target.setProject(target.getParent().getProject());
            } else {
                if (!target.getProject().equals(target.getParent().getProject())) {
                    final String errorMsg = "Target Project is not equal of the Parent Project";
                    LOGGER.error(errorMsg);
                    throw new BadRequestException(errorMsg);
                }
            }
        } else {
            if (target.getProject()==null) {
                final String errorMsg = "Target Project and Parent is null";
                LOGGER.error(errorMsg);
                throw new BadRequestException(errorMsg);
            }
        }
        target.setStatus(EntityStatus.PENDING);
    }

    @HandleAfterCreate
    public void afterCreate(Target target) {
        LOGGER.info("Target: HandleAfterCreate");
        jms.convertAndSend(TargetEngine.QUEUE_CREATE, target);
    }

    @HandleBeforeSave
    public void beforeSave(Target target) {
        LOGGER.info("Target: HandleBeforeSave");
        checkAndSetStatus(target);
    }

    @HandleAfterSave
    public void afterSave(Target target) {
        LOGGER.info("Target: HandleAfterSave");
        EntityStatus status = target.getStatus();
        if (EntityStatus.DISABLED.equals(status)) {
            jms.convertAndSend(TargetEngine.QUEUE_REMOVE, target);
        } else {
            if (EntityStatus.ENABLE.equals(status)) {
                target.setStatus(EntityStatus.PENDING);
                jms.convertAndSend(TargetEngine.QUEUE_CREATE, target);
            } else {
                jms.convertAndSend(TargetEngine.QUEUE_UPDATE, target);
            }
        }
    }

    @HandleBeforeDelete
    public void beforeDelete(Target target) {
        LOGGER.info("Target: HandleBeforeDelete");
    }

    @HandleAfterDelete
    public void afterDelete(Target target) {
        LOGGER.info("Target: HandleAfterDelete");
        jms.convertAndSend(TargetEngine.QUEUE_REMOVE, target);
    }

    private void checkAndSetStatus(Target target) {
        if (target.getStatus()==null) {
            Target targetPersisted = targetRepository.findOne(target.getId());
            if (targetPersisted != null) {
                target.setStatus(targetPersisted.getStatus());
            }
        }
    }

}
