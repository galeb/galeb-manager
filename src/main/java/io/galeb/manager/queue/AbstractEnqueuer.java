/*
 * Galeb - Load Balance as a Service Plataform
 *
 * Copyright (C) 2014-2015 Globo.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */

package io.galeb.manager.queue;

import io.galeb.manager.entity.AbstractEntity;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.JMSException;
import javax.jms.Message;

import java.util.Collections;
import java.util.Map;

public abstract class AbstractEnqueuer<T extends AbstractEntity<?>> {

    private static final String UNIQUE_ID_SEP = ".";

    private static final boolean DISABLE_QUEUE;
    static {
        DISABLE_QUEUE = Boolean.getBoolean(System.getProperty(
                JmsConfiguration.DISABLE_QUEUE, Boolean.toString(false)));
    }

    private static final String QUEUE_UNDEF = "UNDEF";

    private String queueCreateName   = QUEUE_UNDEF;
    private String queueUpdateName   = QUEUE_UNDEF;
    private String queueRemoveName   = QUEUE_UNDEF;
    private String queueSyncName     = QUEUE_UNDEF;

    private final Log logger;

    public AbstractEnqueuer(Log logger) {
        this.logger = logger;
    }

    protected static boolean isDisableQueue() {
        return DISABLE_QUEUE;
    }

    protected abstract JmsTemplate template();

    public String getQueueCreateName() {
        return queueCreateName;
    }

    protected Log logger() {
        return logger;
    }

    protected AbstractEnqueuer<T> setQueueCreateName(String queueCreateName) {
        this.queueCreateName = queueCreateName;
        return this;
    }

    public String getQueueUpdateName() {
        return queueUpdateName;
    }

    protected AbstractEnqueuer<T> setQueueUpdateName(String queueUpdateName) {
        this.queueUpdateName = queueUpdateName;
        return this;
    }

    @SuppressWarnings("unused")
    protected String getQueueRemoveName() {
        return queueRemoveName;
    }

    protected AbstractEnqueuer<T> setQueueRemoveName(String queueRemoveName) {
        this.queueRemoveName = queueRemoveName;
        return this;
    }

    @SuppressWarnings("unused")
    protected String getQueueSyncName() {
        return queueSyncName;
    }

    protected AbstractEnqueuer<T> setQueueSyncName(String queueSyncName) {
        this.queueSyncName = queueSyncName;
        return this;
    }

    public void sendToQueue(String queue, T entity) {
        sendToQueue(queue, entity, Collections.emptyMap());

    }

    public void sendToQueue(String queue, T entity, final Map<String, String> properties) {
        if (!QUEUE_UNDEF.equals(queue) && !isDisableQueue()) {
            template().send(queue, session -> {
                Message message = session.createObjectMessage(entity);
                String uniqueId = "ID:" + queue + UNIQUE_ID_SEP +
                        entity.getId() + UNIQUE_ID_SEP + entity.getLastModifiedAt().getTime();
                properties.entrySet().forEach(entry -> {
                    try {
                        if (entry.getValue() instanceof String) {
                            message.setStringProperty(entry.getKey(), entry.getValue());
                        }
                    } catch (JMSException e) {
                        logger.error(ExceptionUtils.getStackTrace(e));
                    }
                });
                message.setStringProperty("_HQ_DUPL_ID", uniqueId);
                message.setJMSMessageID(uniqueId);
                logger.info("JMSMessageID: " + uniqueId + " - Farm " + entity.getName());
                return message;
            });
        }
    }
}
