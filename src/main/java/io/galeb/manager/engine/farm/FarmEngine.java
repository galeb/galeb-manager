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

package io.galeb.manager.engine.farm;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import io.galeb.manager.common.Properties;
import io.galeb.manager.engine.Driver;
import io.galeb.manager.engine.DriverBuilder;
import io.galeb.manager.engine.Provisioning;
import io.galeb.manager.entity.AbstractEntity.EntityStatus;
import io.galeb.manager.entity.Farm;
import io.galeb.manager.entity.Rule;
import io.galeb.manager.entity.Target;
import io.galeb.manager.entity.VirtualHost;
import io.galeb.manager.repository.FarmRepository;
import io.galeb.manager.repository.RuleRepository;
import io.galeb.manager.repository.TargetRepository;
import io.galeb.manager.repository.VirtualHostRepository;
import io.galeb.manager.security.CurrentUser;
import io.galeb.manager.security.SystemUserService;
import io.galeb.manager.service.GenericEntityService;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Component
public class FarmEngine extends AbstractEngine {

    public static final String QUEUE_CREATE = "queue-farm-create";
    public static final String QUEUE_UPDATE = "queue-farm-update";
    public static final String QUEUE_REMOVE = "queue-farm-remove";
    public static final String QUEUE_RELOAD = "queue-farm-reload";
    public static final String QUEUE_CALLBK = "queue-farm-callback";

    private static final Log LOGGER = LogFactory.getLog(FarmEngine.class);

    @Autowired
    private FarmRepository farmRepository;

    @Autowired
    private GenericEntityService genericEntityService;

    @Autowired
    private VirtualHostRepository virtualHostRepository;

    @Autowired
    private RuleRepository ruleRepository;

    @Autowired
    private TargetRepository targetRepository;

    @Autowired
    private JmsTemplate jms;

    @JmsListener(destination = QUEUE_CREATE)
    public void create(Farm farm) {
        LOGGER.info("Creating "+farm.getClass().getSimpleName()+" "+farm.getName());
        Provisioning provisioning = getProvisioning(farm);
        boolean isOk = false;
        try {
            isOk = provisioning.create(fromEntity(farm));
        } catch (Exception e) {
            LOGGER.error(e);
        } finally {
            farm.setStatus(isOk ? EntityStatus.OK : EntityStatus.ERROR);
            jms.convertAndSend(QUEUE_CALLBK, farm);
        }
    }

    @JmsListener(destination = QUEUE_REMOVE)
    public void remove(Farm farm) {
        LOGGER.info("Removing "+farm.getClass().getSimpleName()+" "+farm.getName());
        Provisioning provisioning = getProvisioning(farm);
        boolean isOk = false;
        try {
            isOk = provisioning.create(fromEntity(farm));
        } catch (Exception e) {
            LOGGER.error(e);
        } finally {
            farm.setStatus(isOk ? EntityStatus.OK : EntityStatus.ERROR);
            jms.convertAndSend(QUEUE_CALLBK, farm);
        }
    }

    @JmsListener(destination = QUEUE_RELOAD)
    public void reload(Farm farm) {
        LOGGER.warn("Reloading "+farm.getClass().getSimpleName()+" "+farm.getName());
        Driver driver = DriverBuilder.getDriver(farm);
        boolean isOk = false;
        Map<String, Object> properties = new HashMap<>();
        SystemUserService.runAs();
        properties.put("api", farm.getApi());
        properties.put("virtualhosts", getVirtualhosts(farm).collect(Collectors.toSet()));
        properties.put("backendpools", getTargets(farm)
                .filter(target -> target.getTargetType().getName().equals("BackendPool"))
                .collect(Collectors.toSet()));
        properties.put("backends", getTargets(farm)
                .filter(target -> target.getTargetType().getName().equals("Backend"))
                .collect(Collectors.toSet()));
        properties.put("rules", getRules(farm).collect(Collectors.toSet()));
        try {
            isOk = driver.reload(makeProperties(farm, driver.diff(properties)));
        } catch (Exception e) {
            LOGGER.error(e);
        } finally {
            farm.setStatus(isOk ? EntityStatus.OK : EntityStatus.ERROR);
            jms.convertAndSend(QUEUE_CALLBK, farm);
            SystemUserService.clearContext();
        }
    }

    @JmsListener(destination = QUEUE_CALLBK)
    public void callBack(Farm farm) {
        if (genericEntityService.isNew(farm)) {
            // farm removed?
            return;
        }
        Authentication currentUser = CurrentUser.getCurrentAuth();
        SystemUserService.runAs();
        farm.setSaveOnly(true);
        farmRepository.save(farm);
        farm.setSaveOnly(false);
        SystemUserService.runAs(currentUser);
    }

    @Override
    protected FarmRepository getFarmRepository() {
        return farmRepository;
    }

    @Override
    protected JmsTemplate getJmsTemplate() {
        return jms;
    }

    private Properties makeProperties(Farm farm) {
        return makeProperties(farm, null);
    }

    private Properties makeProperties(Farm farm, Map<String, Map<String, String>> diff) {
        Properties properties = fromEntity(farm);
        properties.put("virtualhosts", getVirtualhosts(farm).collect(Collectors.toSet()));
        properties.put("targets", getTargets(farm).collect(Collectors.toSet()));
        properties.put("rules", getRules(farm).collect(Collectors.toSet()));
        properties.put("diff", diff);
        return properties;
    }

    private Stream<Target> getTargets(Farm farm) {
        return StreamSupport.stream(
                targetRepository.findByFarmId(farm.getId()).spliterator(), false);
    }

    private Stream<Rule> getRules(Farm farm) {
        return StreamSupport.stream(
                ruleRepository.findByFarmId(farm.getId()).spliterator(), false)
                .filter(rule -> !rule.getParents().isEmpty());
    }

    private Stream<VirtualHost> getVirtualhosts(Farm farm) {
        return StreamSupport.stream(
                virtualHostRepository.findByFarmId(farm.getId()).spliterator(), false);
    }

}
