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

package io.galeb.manager.engine.util;

import io.galeb.manager.entity.AbstractEntity;
import io.galeb.manager.entity.Rule;
import io.galeb.manager.entity.VirtualHost;
import io.galeb.manager.repository.VirtualHostRepository;
import io.galeb.manager.security.services.SystemUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class VirtualHostAliasBuilder {

    @Autowired private VirtualHostRepository virtualHostRepository;

    public VirtualHost buildVirtualHostAlias(String virtualHostName, final VirtualHost virtualHost) {
        VirtualHost virtualHostAlias = new VirtualHost(virtualHostName, virtualHost.getEnvironment(), virtualHost.getProject());

        SystemUserService.runAs();
        Set<Rule> rulesOfVirtualhost = virtualHostRepository.getRulesFromVirtualHostName(virtualHost.getName())
                .stream().collect(Collectors.toSet());
        SystemUserService.clearContext();
        virtualHostAlias.setRules(rulesOfVirtualhost);
        virtualHostAlias.setId(virtualHost.getId());
        virtualHostAlias.setFarmId(virtualHost.getFarmId());
        virtualHostAlias.setRuleDefault(virtualHost.getRuleDefault());
        virtualHostAlias.setProperties(virtualHost.getProperties());
        virtualHostAlias.setRuleDefault(virtualHost.getRuleDefault());
        virtualHostAlias.setRulesOrdered(virtualHost.getRulesOrdered());
        return virtualHostAlias;
    }
}
