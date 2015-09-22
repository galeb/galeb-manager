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

package io.galeb.manager.jms;

import io.galeb.manager.entity.AbstractEntity;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;

import static io.galeb.manager.entity.AbstractEntity.EntityStatus.DISABLED;
import static io.galeb.manager.entity.AbstractEntity.EntityStatus.ENABLE;
import static io.galeb.manager.entity.AbstractEntity.EntityStatus.PENDING;

public abstract class AbstractJmsEnqueuer<T> {

    private static boolean disableJms;

    static {
        disableJms = Boolean.getBoolean(System.getProperty(
                JmsConfiguration.DISABLE_JMS, Boolean.toString(false)));
    }

    private static final String QUEUE_UNDEF = "UNDEF";

    private String queueCreateName   = QUEUE_UNDEF;
    private String queueUpdateName   = QUEUE_UNDEF;
    private String queueRemoveName   = QUEUE_UNDEF;
    private String queueCallBackName = QUEUE_UNDEF;
    private String queueReloadName   = QUEUE_UNDEF;

    protected abstract JmsTemplate jms();

    public String getQueueCreateName() {
        return queueCreateName;
    }

    protected AbstractJmsEnqueuer<T> setQueueCreateName(String queueCreateName) {
        this.queueCreateName = queueCreateName;
        return this;
    }

    public String getQueueUpdateName() {
        return queueUpdateName;
    }

    protected AbstractJmsEnqueuer<T> setQueueUpdateName(String queueUpdateName) {
        this.queueUpdateName = queueUpdateName;
        return this;
    }

    public String getQueueRemoveName() {
        return queueRemoveName;
    }

    protected AbstractJmsEnqueuer<T> setQueueRemoveName(String queueRemoveName) {
        this.queueRemoveName = queueRemoveName;
        return this;
    }

    public String getQueueCallBackName() {
        return queueCallBackName;
    }

    protected AbstractJmsEnqueuer<T> setQueueCallBackName(String queueCallBackName) {
        this.queueCallBackName = queueCallBackName;
        return this;
    }

    public String getQueueReloadName() {
        return queueReloadName;
    }

    protected AbstractJmsEnqueuer<T> setQueueReloadName(String queueReloadName) {
        this.queueReloadName = queueReloadName;
        return this;
    }

    public void jmsSendByStatus(AbstractEntity<?> entity) throws JmsException {
        final AbstractEntity.EntityStatus status = entity.getStatus();
        if (DISABLED.equals(status)) {
            sendToQueue(getQueueRemoveName(), (T) entity);
        } else {
            if (ENABLE.equals(status)) {
                entity.setStatus(PENDING);
                sendToQueue(getQueueCreateName(), (T) entity);
            } else {
                sendToQueue(getQueueUpdateName(), (T) entity);
            }
        }
    }

    public void sendToQueue(String queue, T entity) throws JmsException {
        if (!QUEUE_UNDEF.equals(queue) && !disableJms) {
            jms().convertAndSend(queue, entity);
        }
    }
}
