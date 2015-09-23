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

import io.galeb.manager.entity.Farm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class FarmQueue extends AbstractJmsEnqueuer<Farm> {

    public static final String QUEUE_CREATE = "queue-farm-create";
    public static final String QUEUE_UPDATE = "queue-farm-update";
    public static final String QUEUE_REMOVE = "queue-farm-remove";
    public static final String QUEUE_SYNC   = "queue-farm-sync";
    public static final String QUEUE_CALLBK = "queue-farm-callback";

    @Autowired
    private JmsTemplate jms;

    public FarmQueue() {
        setQueueCreateName(QUEUE_CREATE);
        setQueueUpdateName(QUEUE_UPDATE);
        setQueueRemoveName(QUEUE_REMOVE);
        setQueueCallBackName(QUEUE_CALLBK);
        setQueueSyncName(QUEUE_SYNC);
    }

    public void sendToQueue(String queue, Map.Entry<Farm, Map<String, Object>> entry) throws JmsException {
        if (!disableJms) {
            jms.convertAndSend(queue, entry);
        }
    }

    @Override
    protected JmsTemplate jms() {
        return jms;
    }
}
