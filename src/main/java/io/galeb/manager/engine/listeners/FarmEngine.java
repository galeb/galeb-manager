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

package io.galeb.manager.engine.listeners;

import io.galeb.core.model.Backend;
import io.galeb.core.model.BackendPool;
import io.galeb.manager.entity.*;
import io.galeb.manager.queue.*;
import io.galeb.manager.redis.*;
import io.galeb.manager.repository.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import io.galeb.manager.common.Properties;
import io.galeb.manager.engine.driver.Driver;
import io.galeb.manager.engine.driver.DriverBuilder;
import io.galeb.manager.engine.provisioning.Provisioning;
import io.galeb.manager.entity.AbstractEntity.EntityStatus;
import io.galeb.manager.security.user.CurrentUser;
import io.galeb.manager.security.services.SystemUserService;
import io.galeb.manager.engine.listeners.services.GenericEntityService;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.atomic.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Component
public class FarmEngine extends AbstractEngine<Farm> {

    private static final Log LOGGER = LogFactory.getLog(FarmEngine.class);

    @Autowired private GenericEntityService genericEntityService;
    @Autowired private FarmRepository farmRepository;
    @Autowired private VirtualHostRepository virtualHostRepository;
    @Autowired private RuleRepository ruleRepository;
    @Autowired private TargetRepository targetRepository;
    @Autowired private PoolRepository poolRepository;
    @Autowired private FarmQueue farmQueue;
    @Autowired private VirtualHostQueue virtualHostQueue;
    @Autowired private TargetQueue targetQueue;
    @Autowired private RuleQueue ruleQueue;
    @Autowired private PoolQueue poolQueue;
    @Autowired private DistributedLocker distributedLocker;

    private Map<String, JpaRepository> repositories = new HashMap<>();
    private Map<String, AbstractEnqueuer> queues = new HashMap<>();

    private AtomicBoolean isRead = new AtomicBoolean(false);
    private final Pageable pageable = new PageRequest(0, 99999);

    @PostConstruct
    public void init() {
        repositories.put(VirtualHost.class.getSimpleName().toLowerCase(), virtualHostRepository);
        repositories.put(Target.class.getSimpleName().toLowerCase(), targetRepository);
        repositories.put(Rule.class.getSimpleName().toLowerCase(), ruleRepository);
        repositories.put(Pool.class.getSimpleName().toLowerCase(), poolRepository);

        queues.put(VirtualHost.class.getSimpleName().toLowerCase(), virtualHostQueue);
        queues.put(Target.class.getSimpleName().toLowerCase(), targetQueue);
        queues.put(Rule.class.getSimpleName().toLowerCase(), ruleQueue);
        queues.put(Pool.class.getSimpleName().toLowerCase(), poolQueue);

        isRead.set(true);
    }

    @RabbitListener(queues = FarmQueue.QUEUE_CREATE)
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

    @RabbitListener(queues = FarmQueue.QUEUE_REMOVE)
    public void remove(Farm farm) {
        LOGGER.info("Removing " + farm.getClass().getSimpleName() + " " + farm.getName());
        Provisioning provisioning = getProvisioning(farm);
        boolean isOk = false;
        try {
            isOk = provisioning.remove(fromEntity(farm));
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

    @RabbitListener(containerFactory = "simpleListenerContainerFactory", queues = FarmQueue.QUEUE_SYNC)
    public void sync(Map.Entry<Farm, Map<String, Object>> entrySet) {
        Farm farm = entrySet.getKey();
        Map<String, Object> diff = entrySet.getValue();
        Driver driver = DriverBuilder.getDriver(farm);
        Properties properties = new Properties();
        SystemUserService.runAs();
        properties.put("api", farm.getApi());

        if (diff == null) {
            executeFullReload(farm, driver, properties);
            return;
        }

        LOGGER.warn("Syncing " + farm.getClass().getSimpleName() + " " + farm.getName());

        AtomicReference<Iterator<AbstractEntity<?>>> iteratorAtomicReference = new AtomicReference<>(null);
        AtomicReference<String> lastEntityType = new AtomicReference<>("");

        diff.entrySet().stream().forEach(diffEntrySet -> {

            final Map<String, String> attributes = (Map<String, String>) diffEntrySet.getValue();

            final String action = attributes.get("ACTION");
            final String id = attributes.get("ID");
            final String parentId = attributes.get("PARENT_ID");
            final String entityType = attributes.get("ENTITY_TYPE");

            final String internalEntityType = getInternalEntityType(entityType);

            JpaRepository repository = repositories.get(internalEntityType);
            AbstractEnqueuer<AbstractEntity<?>> queue = queues.get(internalEntityType);

            if (iteratorAtomicReference.get() == null || !lastEntityType.get().equals(entityType)) {
                iteratorAtomicReference.set(repository.findAll().iterator());
                lastEntityType.set(entityType);
            }

            Optional<AbstractEntity<?>> entityFromRepositoryOptional = getEntityIfExist(id, parentId, iteratorAtomicReference.get());
            AbstractEntity<?> entityFromRepository = entityFromRepositoryOptional.orElse(null);

            switch (action.toUpperCase()) {
                case "CREATE":
                    if (entityFromRepository != null) {
                        createEntityOnFarm(queue, entityFromRepository);
                    }
                    break;
                case "UPDATE":
                    if (entityFromRepository != null) {
                        updateEntityOnFarm(queue, entityFromRepository);
                    }
                    break;
                case "REMOVE":
                    removeEntityFromFarm(driver, makeBaseProperty(farm.getApi(), id, parentId, entityType));
                    break;
                default:
                    LOGGER.error("ACTION " + action + "(entityType: " + entityType + " - id: " + id + " - parentId: " + parentId + ") NOT EXIST");
            }
        });
    }

    private void executeFullReload(Farm farm, Driver driver, Properties properties) {
        String farmClassName = farm.getClass().getSimpleName().toLowerCase();
        LOGGER.warn("Full Reloading " + farmClassName + " " + farm.getName());
        properties.put("path", farmClassName);
        driver.remove(properties);
        return;
    }

    private void removeEntityFromFarm(Driver driver, Properties properties) {
        driver.remove(properties);
    }

    private Properties makeBaseProperty(String apiFarm, String id, String parentId, String entityType) {
        Properties properties = new Properties();
        properties.put("api", apiFarm);
        properties.put("json", "{\"id\":\"" + id + "\"" +
                (parentId != null && !"".equals(parentId) ? ",\"parentId\":\"" + parentId + "\"" : "") +
                ",\"version\":0}");
        properties.put("path", entityType);
        return properties;
    }

    private void updateEntityOnFarm(AbstractEnqueuer<AbstractEntity<?>> queue, AbstractEntity<?> entityFromRepository) {
        queue.sendToQueue(queue.getQueueUpdateName(), entityFromRepository);
    }

    private void createEntityOnFarm(AbstractEnqueuer<AbstractEntity<?>> queue, AbstractEntity<?> entity) {
        queue.sendToQueue(queue.getQueueCreateName(), entity);
    }

    private Optional<AbstractEntity<?>> getEntityIfExist(String id, String parentId, Iterator<AbstractEntity<?>> iterator) {
        while (iterator.hasNext()) {
            AbstractEntity<?> entity = iterator.next();
            if ((entity.getName().equals(id)) &&
                    ((!(entity instanceof WithParent) && !(entity instanceof WithParents)) ||
                            (entity instanceof WithParent && (
                                    ((WithParent<AbstractEntity<?>>) entity).getParent() != null &&
                                            ((WithParent<AbstractEntity<?>>) entity).getParent().getName().equals(parentId))) ||
                            (entity instanceof WithParents &&
                                    !((WithParents<AbstractEntity<?>>) entity).getParents().isEmpty() &&
                                    ((WithParents<AbstractEntity<?>>) entity).getParents().stream()
                                            .map(AbstractEntity::getName).collect(Collectors.toList()).contains(parentId)))) {
                return Optional.of(entity);
            }
        }
        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    private Predicate<AbstractEntity<?>> entityExistPredicate(String id, String parentId) {
        return entity -> (entity.getName().equals(id)) &&
                ((!(entity instanceof WithParent) && !(entity instanceof WithParents)) ||
                (entity instanceof WithParent && (
                        ((WithParent<AbstractEntity<?>>) entity).getParent() != null &&
                        ((WithParent<AbstractEntity<?>>) entity).getParent().getName().equals(parentId))) ||
                (entity instanceof WithParents &&
                        !((WithParents<AbstractEntity<?>>) entity).getParents().isEmpty() &&
                        ((WithParents<AbstractEntity<?>>) entity).getParents().stream()
                                .map(AbstractEntity::getName).collect(Collectors.toList()).contains(parentId)));
    }

    @SuppressWarnings("unchecked")
    private Stream<AbstractEntity<?>> convertToStream(JpaRepository repository) {
        return StreamSupport.stream(repository.findAll(new PageRequest(0, 999999)).spliterator(), false);
    }

    private String getInternalEntityType(String entityType) {
        return entityType.toLowerCase().equals(BackendPool.class.getSimpleName().toLowerCase()) ?
                    Pool.class.getSimpleName().toLowerCase() :
                entityType.toLowerCase().equals(Backend.class.getSimpleName().toLowerCase()) ?
                    Target.class.getSimpleName().toLowerCase() : entityType;
    }

    @RabbitListener(queues = FarmQueue.QUEUE_CALLBK)
    public void callBack(Farm farm) {
        if (genericEntityService.isNew(farm)) {
            // farm removed?
            return;
        }
        Authentication currentUser = CurrentUser.getCurrentAuth();
        SystemUserService.runAs();
        try {
            farmRepository.save(farm);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        } finally {
            SystemUserService.runAs(currentUser);
        }
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
        properties.put(VirtualHost.class.getSimpleName().toLowerCase(), getVirtualhosts(farm).collect(Collectors.toSet()));
        properties.put(Target.class.getSimpleName().toLowerCase(), getTargets(farm).collect(Collectors.toSet()));
        properties.put(Rule.class.getSimpleName().toLowerCase(), getRules(farm).collect(Collectors.toSet()));
        properties.put(Pool.class.getSimpleName().toLowerCase(), getPools(farm).collect(Collectors.toSet()));
        properties.put("diff", diff);
        return properties;
    }

    private Stream<Target> getTargets(Farm farm) {
        return StreamSupport.stream(
                targetRepository.findByFarmId(farm.getId(), pageable).spliterator(), false);
    }

    private Stream<Rule> getRules(Farm farm) {
        return StreamSupport.stream(
                ruleRepository.findByFarmId(farm.getId(), pageable).spliterator(), false)
                .filter(rule -> !rule.getParents().isEmpty());
    }

    private Stream<VirtualHost> getVirtualhosts(Farm farm) {
        return StreamSupport.stream(
                virtualHostRepository.findByFarmId(farm.getId(), pageable).spliterator(), false);
    }

    private Stream<Pool> getPools(Farm farm) {
        return StreamSupport.stream(
                poolRepository.findByFarmId(farm.getId(), pageable).spliterator(), false);
    }

}
