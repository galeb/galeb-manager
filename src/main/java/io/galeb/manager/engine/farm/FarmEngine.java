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

import java.util.List;
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
import io.galeb.manager.entity.Farm;
import io.galeb.manager.entity.Rule;
import io.galeb.manager.entity.Target;
import io.galeb.manager.entity.VirtualHost;
import io.galeb.manager.entity.AbstractEntity.EntityStatus;
import io.galeb.manager.repository.FarmRepository;
import io.galeb.manager.repository.RuleRepository;
import io.galeb.manager.repository.TargetRepository;
import io.galeb.manager.repository.VirtualHostRepository;
import io.galeb.manager.security.CurrentUser;
import io.galeb.manager.security.SystemUserService;

@Component
public class FarmEngine extends AbstractEngine {

    public static final String QUEUE_CREATE = "queue-farm-create";
    public static final String QUEUE_UPDATE = "queue-farm-update";
    public static final String QUEUE_REMOVE = "queue-farm-remove";
    public static final String QUEUE_RELOAD = "queue-farm-reload";
    public static final String QUEUE_CALLBK = "queue-farm-callback";

    private static final Log LOGGER = LogFactory.getLog(FarmEngine.class);

    @Autowired
    FarmRepository farmRepository;

    @Autowired
    VirtualHostRepository virtualHostRepository;

    @Autowired
    RuleRepository ruleRepository;

    @Autowired
    TargetRepository targetRepository;

    @Autowired
    JmsTemplate jms;

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
        try {
            isOk = driver.reload(makeProperties(farm));
        } catch (Exception e) {
            LOGGER.error(e);
        } finally {
            farm.setStatus(isOk ? EntityStatus.OK : EntityStatus.ERROR);
            jms.convertAndSend(QUEUE_CALLBK, farm);
        }
        if (isOk) {
            long farmId = farm.getId();
            Authentication currentUser = CurrentUser.getCurrentAuth();
            SystemUserService.runAs();
            List<Target> targets = targetRepository.findByFarmId(farmId);
            List<Rule> rules = ruleRepository.findByFarmId(farmId);
            List<VirtualHost> virtualhosts = virtualHostRepository.findByFarmId(farmId);
            SystemUserService.runAs(currentUser);
            if (targets != null) {
                targets.stream().forEach(target -> {
                    jms.convertAndSend(TargetEngine.QUEUE_CREATE, target);
                });
            } else {
                LOGGER.warn("targets is null");
            }
            if (rules != null) {
                rules.stream().forEach(rule -> {
                    jms.convertAndSend(RuleEngine.QUEUE_CREATE, rule);
                });
            } else {
                LOGGER.warn("rules is null");
            }
            if (virtualhosts != null) {
                virtualhosts.stream().forEach(virtualhost -> {
                    jms.convertAndSend(VirtualHostEngine.QUEUE_CREATE, virtualhost);
                });
            } else {
                LOGGER.warn("virtualhosts is null");
            }
        }
    }

    @JmsListener(destination = QUEUE_CALLBK)
    public void callBack(Farm farm) {
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
        Properties properties = fromEntity(farm);
        return properties;
    }
}
