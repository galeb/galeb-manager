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

import io.galeb.manager.entity.VirtualHost;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Service
public class VirtualHostQueue extends AbstractEnqueuer<VirtualHost> {

    public static final String QUEUE_CREATE = "queue-virtualhost-create";
    public static final String QUEUE_REMOVE = "queue-virtualhost-remove";
    public static final String QUEUE_UPDATE = "queue-virtualhost-update";
    public static final String QUEUE_RELOAD = "queue-virtualhost-reload";

    private static final Log LOGGER = LogFactory.getLog(VirtualHost.class);

    @Autowired
    private JmsTemplate template;

    public VirtualHostQueue() {
        super(LOGGER);
        setQueueCreateName(QUEUE_CREATE);
        setQueueUpdateName(QUEUE_UPDATE);
        setQueueRemoveName(QUEUE_REMOVE);
        setQueueSyncName(QUEUE_RELOAD);
    }

    @Override
    protected JmsTemplate template() {
        return template;
    }
}
