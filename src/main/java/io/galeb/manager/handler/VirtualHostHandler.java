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

import io.galeb.manager.cache.DistMap;
import io.galeb.manager.entity.Farm;
import io.galeb.manager.entity.Rule;
import io.galeb.manager.entity.RuleOrder;
import io.galeb.manager.entity.VirtualHost;
import io.galeb.manager.exceptions.BadRequestException;
import io.galeb.manager.exceptions.ConflictException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.HandleAfterDelete;
import org.springframework.data.rest.core.annotation.HandleAfterSave;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeDelete;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.security.core.Authentication;

import io.galeb.manager.repository.FarmRepository;
import io.galeb.manager.repository.VirtualHostRepository;
import io.galeb.manager.security.user.CurrentUser;
import io.galeb.manager.security.services.SystemUserService;

import java.util.*;
import java.util.stream.Collectors;

@RepositoryEventHandler(VirtualHost.class)
public class VirtualHostHandler extends AbstractHandler<VirtualHost> {

    private static final Log LOGGER = LogFactory.getLog(VirtualHostHandler.class);

    @Autowired private FarmRepository farmRepository;
    @Autowired private VirtualHostRepository virtualHostRepository;
    @Autowired private DistMap distMap;
    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired private StringRedisTemplate template;

    @Override
    protected void setBestFarm(final VirtualHost virtualhost) {
        Authentication currentUser = CurrentUser.getCurrentAuth();
        SystemUserService.runAs();
        final Iterable<Farm> farmIterable = farmRepository.findByEnvironment(
                virtualhost.getEnvironment());
        final Farm farm = farmIterable.iterator().hasNext() ? farmIterable.iterator().next() : null;
        SystemUserService.runAs(currentUser);
        if (farm!=null) {
            virtualhost.setFarmId(farm.getId());
        }
    }

    @HandleBeforeCreate
    public void beforeCreate(VirtualHost virtualhost) throws Exception {
        virtualhost.setFarmId(-1L);
        updateRuleOrder(virtualhost);
        beforeCreate(virtualhost, LOGGER);
        checkDupOnAliases(virtualhost, true);
    }

    @HandleAfterCreate
    public void afterCreate(VirtualHost virtualhost) throws Exception {
        afterCreate(virtualhost, LOGGER);
    }

    @HandleBeforeSave
    public void beforeSave(VirtualHost virtualhost) throws Exception {
        distMap.remove(virtualhost);
        updateRuleOrder(virtualhost);
        beforeSave(virtualhost, getVirtualHostRepository(), LOGGER);
        checkDupOnAliases(virtualhost, false);
    }

    @HandleAfterSave
    public void afterSave(VirtualHost virtualhost) throws Exception {
        afterSave(virtualhost, LOGGER);
    }

    @HandleBeforeDelete
    public void beforeDelete(VirtualHost virtualhost) {
        distMap.remove(virtualhost);
        beforeDelete(virtualhost, LOGGER);
    }

    @HandleAfterDelete
    public void afterDelete(VirtualHost virtualhost) throws Exception {
        afterDelete(virtualhost, LOGGER);
    }

    public void checkDupOnAliases(final VirtualHost virtualHost, final boolean isCreation) {
        final long farmId = virtualHost.getFarmId();
        if (farmId == -1L) return;
        VirtualHost virtualHostExistent = getVirtualHostRepository().findOne(virtualHost.getId());
        if (isCreation && virtualHostExistent != null) {
            throw new ConflictException("Virtual Host already exists");
        }
        final Set<String> allNames = getVirtualHostRepository().getAllNamesExcept(virtualHost);
        if (virtualHost.getAliases().stream().anyMatch(allNames::contains) ||
            allNames.contains(virtualHost.getName()) ||
            virtualHost.getAliases().contains(virtualHost.getName())) {
            throw new BadRequestException("Name already exists");
        }
    }

    protected VirtualHostRepository getVirtualHostRepository() {
        return virtualHostRepository;
    }

    private void updateRuleOrder(VirtualHost virtualhost) {
        if (!virtualhost.getRules().isEmpty()) {
            final Set<Long> ruleIds = new HashSet<>(virtualhost.getRules().stream().map(Rule::getId).collect(Collectors.toSet()));
            final Set<Long> rulesIdsOrdered = virtualhost.getRulesOrdered().stream()
                                                .map(RuleOrder::getRuleId).collect(Collectors.toSet());
            if (!ruleIds.containsAll(rulesIdsOrdered)){
                throw new BadRequestException("Any rule not contains in rules ordered.");
            }
            if (!rulesIdsOrdered.isEmpty()) {
                ruleIds.removeAll(rulesIdsOrdered);
            }
            ruleIds.forEach(ruleId -> {
                virtualhost.getRulesOrdered().add(new RuleOrder(ruleId, Integer.MAX_VALUE));
            });
        }
    }

    @Override
    protected boolean canRegisterChanges() {
        return true;
    }

}
