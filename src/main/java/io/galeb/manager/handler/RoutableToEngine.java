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

import static io.galeb.manager.entity.AbstractEntity.EntityStatus.DISABLED;
import static io.galeb.manager.entity.AbstractEntity.EntityStatus.ENABLE;
import static io.galeb.manager.entity.AbstractEntity.EntityStatus.PENDING;

import org.apache.commons.logging.Log;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;

import io.galeb.manager.entity.AbstractEntity;
import io.galeb.manager.entity.AbstractEntity.EntityStatus;

public abstract class RoutableToEngine<T extends AbstractEntity<?>> {

    private static final String QUEUE_UNDEF = "UNDEF";

    private String queueCreateName = QUEUE_UNDEF;
    private String queueUpdateName = QUEUE_UNDEF;
    private String queueRemoveName = QUEUE_UNDEF;

    protected abstract void setBestFarm(T entity) throws Exception;

    protected String getQueueCreateName() {
        return queueCreateName;
    }

    protected RoutableToEngine<T> setQueueCreateName(String queueCreateName) {
        this.queueCreateName = queueCreateName;
        return this;
    }

    protected String getQueueUpdateName() {
        return queueUpdateName;
    }

    protected RoutableToEngine<T> setQueueUpdateName(String queueUpdateName) {
        this.queueUpdateName = queueUpdateName;
        return this;
    }

    protected String getQueueRemoveName() {
        return queueRemoveName;
    }

    protected RoutableToEngine<T> setQueueRemoveName(String queueRemoveName) {
        this.queueRemoveName = queueRemoveName;
        return this;
    }

    protected void fixStatus(T entity, PagingAndSortingRepository<T, Long> repository) throws Exception {
        final EntityStatus status = entity.getStatus();
        if (status == null || (!DISABLED.equals(status) && !ENABLE.equals(status))) {
            final T targetPersisted = repository.findOne(entity.getId());
            if (targetPersisted != null && !targetPersisted.getStatus().equals(status)) {
                entity.setStatus(targetPersisted.getStatus());
            }
        }
    }

    protected void jmsSendByStatus(T entity, JmsTemplate jms) throws JmsException {
        final EntityStatus status = entity.getStatus();
        if (DISABLED.equals(status)) {
            jmsSend(jms, getQueueRemoveName(), entity);
        } else {
            if (ENABLE.equals(status)) {
                entity.setStatus(PENDING);
                jmsSend(jms, getQueueCreateName(), entity);
            } else {
                jmsSend(jms, getQueueUpdateName(), entity);
            }
        }
    }

    protected void jmsSend(JmsTemplate jms, String queue, T entity) throws JmsException {
        if (!QUEUE_UNDEF.equals(queue)) {
            jms.convertAndSend(queue, entity);
        }
    }

    public void beforeCreate(T entity, Log logger) throws Exception {
        logger.info(entity.getClass().getSimpleName()+": HandleBeforeCreate");
        setBestFarm(entity);
        entity.setStatus(PENDING);
    }

    public void afterCreate(T entity, JmsTemplate jms, Log logger) throws Exception {
        logger.info(entity.getClass().getSimpleName()+": HandleAfterCreate");
        try {
            jmsSend(jms, getQueueCreateName(), entity);
        } catch (JmsException e) {
            logger.error(e);
            throw e;
        }
    }

    public void beforeSave(T entity, PagingAndSortingRepository<T, Long> repository, Log logger) throws Exception {
        logger.info(entity.getClass().getSimpleName()+": HandleBeforeSave");
        setBestFarm(entity);
        try {
            fixStatus(entity, repository);
        } catch (Exception e) {
            logger.error(e);
            throw e;
        }
    }

    public void afterSave(T entity, JmsTemplate jms, Log logger) throws Exception {
        logger.info(entity.getClass().getSimpleName()+": HandleAfterSave");
        try {
            jmsSend(jms, getQueueUpdateName(), entity);
        } catch (JmsException e) {
            logger.error(e);
            throw e;
        }
    }

    public void beforeDelete(T entity, Log logger) {
        logger.info(entity.getClass().getSimpleName()+": HandleBeforeDelete");
    }

    public void afterDelete(T entity, JmsTemplate jms, Log logger) throws Exception {
        logger.info(entity.getClass().getSimpleName()+": HandleAfterDelete");
        try {
            jmsSend(jms, getQueueRemoveName(), entity);
        } catch (JmsException e) {
            logger.error(e);
            throw e;
        }
    }


}
