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

import java.util.Set;
import java.util.stream.Collectors;

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

import io.galeb.manager.engine.farm.RuleEngine;
import io.galeb.manager.entity.Rule;
import io.galeb.manager.entity.Target;
import io.galeb.manager.entity.VirtualHost;
import io.galeb.manager.exceptions.BadRequestException;
import io.galeb.manager.repository.RuleRepository;
import io.galeb.manager.repository.TargetRepository;

@RepositoryEventHandler(Rule.class)
public class RuleHandler extends RoutableToEngine<Rule> {

    private static Log LOGGER = LogFactory.getLog(RuleHandler.class);

    @Autowired
    private JmsTemplate jms;

    @Autowired
    private RuleRepository ruleRepository;

    @Autowired
    private TargetRepository targetRepository;


    public RuleHandler() {
        setQueueCreateName(RuleEngine.QUEUE_CREATE);
        setQueueUpdateName(RuleEngine.QUEUE_UPDATE);
        setQueueRemoveName(RuleEngine.QUEUE_REMOVE);
    }

    @Override
    protected void setBestFarm(final Rule rule) throws Exception {
        long farmIdTarget = -1L;
        Set<Long> farmIds = rule.getParents().stream().collect(
                Collectors.groupingBy(VirtualHost::getFarmId)).keySet();
        long farmIdVirtualHost = farmIds.size() == 1 ? farmIds.iterator().next() : -1L;

        if (rule.getTarget() != null) {
            final Target target = rule.getTarget();
            farmIdTarget = target.getFarmId();
        }
        if (farmIdVirtualHost > -1L && farmIdTarget > -1L && farmIdVirtualHost != farmIdTarget) {
            String errorMsg = "VirtualHost.farmId is not equal Target.farmId";
            LOGGER.error(errorMsg);
            throw new BadRequestException(errorMsg);
        }
        rule.setFarmId(farmIdTarget);
    }

    @HandleBeforeCreate
    public void beforeCreate(Rule rule) throws Exception {
        beforeCreate(rule, LOGGER);
        setTargetGlobalIfNecessary(rule);

    }

    @HandleAfterCreate
    public void afterCreate(Rule rule) throws Exception {
        afterCreate(rule, jms, LOGGER);
    }

    @HandleBeforeSave
    public void beforeSave(Rule rule) throws Exception {
        beforeSave(rule, ruleRepository, LOGGER);
        setTargetGlobalIfNecessary(rule);
    }

    @HandleAfterSave
    public void afterSave(Rule rule) throws Exception {
        afterSave(rule, jms, LOGGER);
    }

    @HandleBeforeDelete
    public void beforeDelete(Rule rule) throws Exception {
        beforeDelete(rule, LOGGER);
    }

    @HandleAfterDelete
    public void afterDelete(Rule rule) throws Exception {
        afterDelete(rule, jms, LOGGER);
    }

    private void setTargetGlobalIfNecessary(Rule rule) {
        if (rule.isGlobal()) {
            Target target = rule.getTarget();
            target.setGlobal(true);
            target.setSaveOnly(true);
            targetRepository.save(target);
            target.setSaveOnly(false);
        }
    }

}
