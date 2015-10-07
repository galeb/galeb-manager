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
import org.springframework.jms.core.*;

import static io.galeb.manager.entity.AbstractEntity.EntityStatus.DISABLED;
import static io.galeb.manager.entity.AbstractEntity.EntityStatus.ENABLE;
import static io.galeb.manager.entity.AbstractEntity.EntityStatus.PENDING;

public abstract class AbstractEnqueuer<T> {

    private static final boolean DISABLE_QUEUE;
    static {
        DISABLE_QUEUE = Boolean.getBoolean(System.getProperty(
                JmsConfiguration.DISABLE_QUEUE, Boolean.toString(false)));
    }

    private static final String QUEUE_UNDEF = "UNDEF";

    private String queueCreateName   = QUEUE_UNDEF;
    private String queueUpdateName   = QUEUE_UNDEF;
    private String queueRemoveName   = QUEUE_UNDEF;
    private String queueCallBackName = QUEUE_UNDEF;
    private String queueSyncName = QUEUE_UNDEF;

    protected static boolean isDisableQueue() {
        return DISABLE_QUEUE;
    }

    protected abstract JmsTemplate template();

    public String getQueueCreateName() {
        return queueCreateName;
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

    public String getQueueRemoveName() {
        return queueRemoveName;
    }

    protected AbstractEnqueuer<T> setQueueRemoveName(String queueRemoveName) {
        this.queueRemoveName = queueRemoveName;
        return this;
    }

    public String getQueueCallBackName() {
        return queueCallBackName;
    }

    protected AbstractEnqueuer<T> setQueueCallBackName(String queueCallBackName) {
        this.queueCallBackName = queueCallBackName;
        return this;
    }

    public String getQueueSyncName() {
        return queueSyncName;
    }

    protected AbstractEnqueuer<T> setQueueSyncName(String queueSyncName) {
        this.queueSyncName = queueSyncName;
        return this;
    }

    public void sendByStatus(AbstractEntity<?> entity) {
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

    public void sendToQueue(String queue, T entity) {
        if (!QUEUE_UNDEF.equals(queue) && !isDisableQueue()) {
            template().convertAndSend(queue, entity);
        }
    }
}
