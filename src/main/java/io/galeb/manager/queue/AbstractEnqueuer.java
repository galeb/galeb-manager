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

import io.galeb.manager.common.ErrorLogger;
import io.galeb.manager.entity.AbstractEntity;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import javax.jms.JMSException;
import javax.jms.Message;

import java.util.Collections;
import java.util.Map;

import static io.galeb.manager.engine.listeners.AbstractEngine.API_PROP;
import static org.apache.activemq.artemis.api.core.Message.HDR_DUPLICATE_DETECTION_ID;

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

    public void sendToQueue(String queue, T entity, String uniqueId) {
        sendToQueue(queue, entity, Collections.emptyMap(), uniqueId);
    }

    public void sendToQueue(String queue, T entity, final Map<String, String> properties) {
        String api = getApiEncoded(properties.get(API_PROP));
        String apiWithSep = !"".equals(api) ? UNIQUE_ID_SEP + api : "";
        String uniqueId = "ID:" + queue + UNIQUE_ID_SEP + entity.getId() + apiWithSep + UNIQUE_ID_SEP + entity.getLastModifiedAt().getTime();
        sendToQueue(queue, entity, properties, uniqueId);
    }

    private String getApiEncoded(String api) {
        if (api != null) {
            return  api.replace('/', '_').replace(':', '_').replace('?', '_');
        }
        return "";
    }

    public void sendToQueue(String queue, T entity, final Map<String, String> properties, String uniqueId) {
        if (!QUEUE_UNDEF.equals(queue) && !isDisableQueue()) {
            MessageCreator messageCreator = session -> {
                Message message = session.createObjectMessage(entity);
                properties.forEach((key, value) -> {
                    try {
                        if (value != null) message.setStringProperty(key, value);
                    } catch (JMSException e) {
                        logger.error(ExceptionUtils.getStackTrace(e));
                    }
                });
                defineUniqueId(message, uniqueId);

                logger.info("JMSMessageID: " + uniqueId + " - " + entity.getClass().getSimpleName() + " " + entity.getName());
                return message;
            };
            try {
                template().sendAndReceive(queue, messageCreator);
            } catch (Exception e) {
                ErrorLogger.logError(e, this.getClass());
            }
        }
    }

    private void defineUniqueId(Message message, String uniqueId) throws JMSException {
        message.setStringProperty("_HQ_DUPL_ID", uniqueId);
        message.setJMSMessageID(uniqueId);
        message.setStringProperty(HDR_DUPLICATE_DETECTION_ID.toString(), uniqueId);
    }
}
