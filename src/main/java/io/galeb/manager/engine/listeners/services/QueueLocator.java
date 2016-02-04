/*
 * Galeb - Load Balance as a Service Plataform
 *
 * Copyright (C) 2014-2016 Globo.com
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

package io.galeb.manager.engine.listeners.services;

import io.galeb.manager.entity.*;
import io.galeb.manager.queue.*;
import org.apache.commons.logging.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;

@Service
public class QueueLocator {

    private static final Log LOGGER = LogFactory.getLog(QueueLocator.class);

    @Autowired private FarmQueue farmQueue;
    @Autowired private VirtualHostQueue virtualHostQueue;
    @Autowired private TargetQueue targetQueue;
    @Autowired private RuleQueue ruleQueue;
    @Autowired private PoolQueue poolQueue;

    public AbstractEnqueuer<? extends AbstractEntity<?>> getQueue(String entityClass) {
        switch (entityClass) {
            case "virtualhost":
                return virtualHostQueue;
            case "rule":
                return ruleQueue;
            case "pool":
                return poolQueue;
            case "target":
                return targetQueue;
            case "farm":
                return farmQueue;
            default:
                LOGGER.error("Entity Class " + entityClass + " NOT FOUND");
                return null;
        }
    }

    public AbstractEnqueuer<? extends AbstractEntity<?>> getQueue(Class<? extends AbstractEntity> clazz) {
        String classNameLowercase = clazz.getSimpleName().toLowerCase();
        return getQueue(classNameLowercase);
    }
}
