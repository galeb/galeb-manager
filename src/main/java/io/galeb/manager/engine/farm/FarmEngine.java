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

import io.galeb.manager.entity.*;
import io.galeb.manager.jms.FarmQueue;
import io.galeb.manager.repository.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import io.galeb.manager.common.Properties;
import io.galeb.manager.engine.Driver;
import io.galeb.manager.engine.DriverBuilder;
import io.galeb.manager.engine.Provisioning;
import io.galeb.manager.entity.AbstractEntity.EntityStatus;
import io.galeb.manager.security.CurrentUser;
import io.galeb.manager.security.SystemUserService;
import io.galeb.manager.service.GenericEntityService;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Component
public class FarmEngine extends AbstractEngine<Farm> {

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
    private FarmQueue farmQueue;

    @Autowired
    private VirtualHostEngine virtualHostEngine;

    @Autowired
    private TargetEngine targetEngine;

    @Autowired
    private RuleEngine ruleEngine;

    private Map<String, AbstractEngine> engines = new HashMap<>();

    private Map<String, PagingAndSortingRepository> repositories = new HashMap<>();

    private AtomicBoolean isRead = new AtomicBoolean(false);

    @PostConstruct
    public void init() {
        engines.put(VirtualHost.class.getSimpleName().toLowerCase(), virtualHostEngine);
        engines.put(Target.class.getSimpleName().toLowerCase(), targetEngine);
        engines.put(Rule.class.getSimpleName().toLowerCase(), ruleEngine);
        repositories.put(VirtualHost.class.getSimpleName().toLowerCase(), virtualHostRepository);
        repositories.put(Target.class.getSimpleName().toLowerCase(), targetRepository);
        repositories.put(Rule.class.getSimpleName().toLowerCase(), ruleRepository);
        isRead.set(true);
    }

    @JmsListener(destination = FarmQueue.QUEUE_CREATE)
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
            farmQueue.sendToQueue(FarmQueue.QUEUE_CALLBK, farm);
        }
    }

    @JmsListener(destination = FarmQueue.QUEUE_REMOVE)
    public void remove(Farm farm) {
        LOGGER.info("Removing " + farm.getClass().getSimpleName() + " " + farm.getName());
        Provisioning provisioning = getProvisioning(farm);
        boolean isOk = false;
        try {
            isOk = provisioning.create(fromEntity(farm));
        } catch (Exception e) {
            LOGGER.error(e);
        } finally {
            farm.setStatus(isOk ? EntityStatus.OK : EntityStatus.ERROR);
            farmQueue.sendToQueue(FarmQueue.QUEUE_CALLBK, farm);
        }
    }

    @Override
    protected void update(Farm entity) {
        //
    }

    @JmsListener(destination = FarmQueue.QUEUE_RELOAD)
    public void reload(Map.Entry<Farm, Map<String, Object>> entrySet) {
        if (!isRead.get()) {
            return;
        }
        Farm farm = entrySet.getKey();
        Map<String, Object> diff = entrySet.getValue();
        LOGGER.warn("Syncing " + farm.getClass().getSimpleName() + " " + farm.getName());
        Driver driver = DriverBuilder.getDriver(farm);
        Properties properties = new Properties();
        SystemUserService.runAs();
        properties.put("api", farm.getApi());

        if (diff == null) {
            driver.remove(properties);
            return;
        }

        diff.entrySet().stream().forEach(diffEntrySet -> {
            final Map<String, String> attributes = (Map<String, String>) diffEntrySet.getValue();

            final String action = attributes.get("ACTION");
            final String id = attributes.get("ID");
            final String parentId = attributes.get("PARENT_ID");
            final String entityType = attributes.get("ENTITY_TYPE");
            final String internalEntityType = entityType.toLowerCase().equals("backendpool") ||
                    entityType.toLowerCase().equals("backend") ? Target.class.getSimpleName().toLowerCase() : entityType;
            final AbstractEngine engine = engines.get(internalEntityType);
            PagingAndSortingRepository repository = repositories.get(internalEntityType);
            Stream<AbstractEntity> stream = StreamSupport.stream(repository.findAll().spliterator(), false);
            Optional<AbstractEntity> entityFromRepositoryOptional = stream.filter(entity -> entity.getName().equals(id))
                    .filter(entity -> (!(entity instanceof WithParent) && !(entity instanceof WithParents)) ||
                            (entity instanceof WithParent) && (
                                    (((WithParent<AbstractEntity<?>>) entity).getParent() != null &&
                                            ((WithParent<AbstractEntity<?>>) entity).getParent().getName().equals(parentId)) ||
                                            !((WithParent<AbstractEntity<?>>) entity).getChildren().isEmpty()) ||
                            (entity instanceof WithParents) &&
                                    !((WithParents<AbstractEntity<?>>) entity).getParents().isEmpty() &&
                                    ((WithParents<AbstractEntity<?>>) entity).getParents().stream()
                                            .map(AbstractEntity::getName).collect(Collectors.toList()).contains(parentId))
                    .findAny();
            AbstractEntity<?> entityFromRepository = entityFromRepositoryOptional.orElse(null);
            switch (action.toUpperCase()) {
                case "CREATE":
                    engine.create(entityFromRepository);
                    break;
                case "UPDATE":
                    engine.update(entityFromRepository);
                    break;
                case "REMOVE":
                    properties.put("json", "{\"id\":\"" + id + "\"" +
                            (parentId != null && !"".equals(parentId) ? ",\"parentId\":\"" + parentId + "\"" : "") +
                            ",\"version\":0}");
                    properties.put("path", entityType);
                    driver.remove(properties);
                    break;
                default:
                    LOGGER.error("ACTION " + action + "(entityType: " + entityType + " - id: " + id + " - parentId: " + parentId + ") NOT EXIST");
            }

        });
    }

    @JmsListener(destination = FarmQueue.QUEUE_CALLBK)
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
    protected FarmQueue farmQueue() {
        return farmQueue;
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
