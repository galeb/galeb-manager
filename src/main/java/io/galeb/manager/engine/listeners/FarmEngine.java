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

import static io.galeb.manager.cache.DistMap.DIST_MAP_FARM_ID_PROP;
import static io.galeb.manager.engine.driver.Driver.ActionOnDiff.REMOVE;
import static io.galeb.manager.engine.driver.DriverBuilder.getDriver;
import static io.galeb.manager.entity.AbstractEntity.EntityStatus.*;
import static java.lang.System.currentTimeMillis;

import io.galeb.core.json.JsonObject;
import io.galeb.core.model.Backend;
import io.galeb.core.model.BackendPool;
import io.galeb.core.model.Entity;
import io.galeb.core.model.Rule;
import io.galeb.manager.cache.DistMap;
import io.galeb.manager.engine.driver.Driver;
import io.galeb.manager.engine.driver.Driver.ActionOnDiff;
import io.galeb.manager.engine.listeners.services.QueueLocator;
import io.galeb.manager.engine.service.LockerManager;
import io.galeb.manager.engine.util.CounterDownLatch;
import io.galeb.manager.entity.*;
import io.galeb.manager.entity.AbstractEntity.EntityStatus;
import io.galeb.manager.queue.AbstractEnqueuer;
import io.galeb.manager.queue.FarmQueue;
import io.galeb.manager.repository.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import io.galeb.manager.common.Properties;
import io.galeb.manager.engine.provisioning.Provisioning;
import io.galeb.manager.security.user.CurrentUser;
import io.galeb.manager.security.services.SystemUserService;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Component
public class FarmEngine extends AbstractEngine<Farm> {

    private static final Log LOGGER = LogFactory.getLog(FarmEngine.class);

    private final Pageable pageable = new PageRequest(0, Integer.MAX_VALUE);

    private LockerManager lockerManager = null;

    @Autowired private DistMap distMap;

    @Override
    protected Log getLogger() {
        return LOGGER;
    }

    @Autowired private FarmRepository farmRepository;
    @Autowired private VirtualHostRepository virtualHostRepository;
    @Autowired private RuleRepository ruleRepository;
    @Autowired private TargetRepository targetRepository;
    @Autowired private PoolRepository poolRepository;
    @Autowired private QueueLocator queueLocator;

    private AtomicBoolean isReady = new AtomicBoolean(false);

    @PostConstruct
    public void init() {
        isReady.set(true);
    }

    private JpaRepositoryWithFindByName getRepository(String entityClass) {
        switch (entityClass) {
            case "virtualhost":
                return virtualHostRepository;
            case "rule":
                return ruleRepository;
            case "pool":
                return poolRepository;
            case "target":
                return targetRepository;
            default:
                LOGGER.error("Entity Class " + entityClass + " NOT FOUND");
                return null;
        }
    }

    @JmsListener(destination = FarmQueue.QUEUE_RELOAD)
    public void reload(Farm farm) {
        distMap.resetFarm(farm.getId());
        String apiWithSeparator = farm.getApi();
        Arrays.stream(apiWithSeparator.split(",")).forEach(api -> {
            executeFullReload(farm, getDriver(farm), getPropertiesWithEntities(farm, api));
        });
    }

    @JmsListener(destination = FarmQueue.QUEUE_CREATE)
    public void create(Farm farm, @Headers final Map<String, String> jmsHeaders) {
        LOGGER.info("Creating " + farm.getClass().getSimpleName() + " " + farm.getName());
        Provisioning provisioning = getProvisioning(farm);
        try {
            provisioning.create(fromEntity(farm, jmsHeaders));
        } catch (Exception e) {
            LOGGER.error(e);
        }
    }

    @JmsListener(destination = FarmQueue.QUEUE_REMOVE)
    public void remove(Farm farm, @Headers final Map<String, String> jmsHeaders) {
        LOGGER.info("Removing " + farm.getClass().getSimpleName() + " " + farm.getName());
        Provisioning provisioning = getProvisioning(farm);
        try {
            provisioning.remove(fromEntity(farm, jmsHeaders));
        } catch (Exception e) {
            LOGGER.error(e);
        }
    }

    @Override
    protected void update(Farm entity, @Headers final Map<String, String> jmsHeaders) {
        //
    }

    @SuppressWarnings({ "unchecked", "unused" })
    @JmsListener(destination = FarmQueue.QUEUE_SYNC)
    public void sync(Farm farm) {
        long farmId = farm.getId();
        String farmName = farm.getName();
        String farmStatusMsgPrefix = "FARM STATUS - ";

        if (lockerManager == null) {
            lockerManager = new LockerManager();
        }
        if (lockerManager.lock(farm.idName())) {

            String apiWithSeparator = farm.getApi();
            Arrays.stream(apiWithSeparator.split(",")).forEach(api -> {
                CounterDownLatch.put(api, -1);
            });

            final Driver driver = getDriver(farm);

            final Map<String, EntityStatus> statusMap = new HashMap<>();

            Arrays.stream(apiWithSeparator.split(",")).forEach(api -> {
                long start = currentTimeMillis();

                String farmFull = farmName + " (" + farmId + ") [ " + api + " ]";
                LOGGER.info(farmStatusMsgPrefix + "Retrieving entities from database - " + farmFull);
                final Properties properties = getPropertiesWithEntities(farm, api);

                LOGGER.info(farmStatusMsgPrefix + "Starting Check & Sync task - " + farmFull);
                Map<String, Map<String, Object>> diff = new HashMap<>();
                Map<String, Map<String, Map<String, String>>> remoteMultiMap;

                try {
                    long diffStart = currentTimeMillis();
                    remoteMultiMap = driver.getAll(properties);
                    diff.putAll(driver.diff(properties, remoteMultiMap));
                    int diffSize = diff.size();

                    String diffDurationMsg = farmStatusMsgPrefix + "diff from " + farmFull + " finished ("
                            + (currentTimeMillis() - diffStart) + " ms)";
                    LOGGER.info(diffDurationMsg);

                    CounterDownLatch.put(api, diffSize);

                    updateStatus(remoteMultiMap, farmId);
                    if (diffSize == 0) {
                        LOGGER.info(farmStatusMsgPrefix + "OK: " + farmFull + " ("
                                + (currentTimeMillis() - start) + " ms)");

                        statusMap.put(api, OK);
                    } else {
                        LOGGER.warn(farmStatusMsgPrefix + "INCONSISTENT (" + diffSize + " fix(es)): " + farmFull
                                + " (" + (currentTimeMillis() - start) + " ms). Calling fixFarm task.");
                        fixFarm(farm, diff, driver, api);
                        statusMap.put(api, PENDING);
                    }

                } catch (Exception e) {
                    CounterDownLatch.reset(api);
                    LOGGER.error(e);
                    LOGGER.error(farmStatusMsgPrefix + farmFull + " FAILED");
                    statusMap.put(api, ERROR);
                }
            });
            EntityStatus statusConsolidated = getStatusConsolidated(statusMap);
            distMap.put(farm, statusConsolidated.toString());
            statusMap.clear();
        } else {
            LOGGER.info(farmStatusMsgPrefix + "Farm " + farmName + " locked by an other process/node. Aborting Check & Sync Task.");
        }
    }

    private EntityStatus getStatusConsolidated(final Map<String, EntityStatus> statusMap) {
        if (statusMap.containsValue(PENDING)) {
            return PENDING;
        }
        if (statusMap.entrySet().stream().filter(e -> e.getValue().equals(OK)).count() == statusMap.size()) {
            return OK;
        }
        if (statusMap.containsValue(ERROR)) {
            return ERROR;
        }
        return UNKNOWN;
    }

    private void updateStatus(final Map<String, Map<String, Map<String, String>>> remoteMultiMap, final Long farmId) {
        remoteMultiMap.entrySet().forEach(entryWithPath -> {
            entryWithPath.getValue().entrySet().forEach(entryWithEntity -> {
                Entity entity = new Entity();
                Map<String, String> entityMap = entryWithEntity.getValue();
                String id = entityMap.get("id");
                String version = entityMap.get("version");
                version = version != null ? version : "0";
                String entityType = entityMap.get("_entity_type");
                String parentId = entityMap.get("parentId");
                parentId = parentId != null && !Rule.class.getSimpleName().toLowerCase().equals(entityType) ? parentId : "";
                entity.setId(id);
                entity.setParentId(parentId);
                entity.setVersion(Integer.parseInt(version));
                entity.setEntityType(entityType);
                entity.getProperties().put(DIST_MAP_FARM_ID_PROP, farmId);
                distMap.put(entity, JsonObject.toJsonString(entity));
            });
        });
    }

    @SuppressWarnings("unchecked")
    private void fixFarm(final Farm farm,
                         final Map<String, Map<String, Object>> diff,
                         final Driver driver,
                         final String api) {

        LOGGER.warn("FARM STATUS - Synchronizing Farm " + farm.getName());

        final AtomicReference<Set<String>> entityTypes = new AtomicReference<>(new HashSet<>());

        diff.entrySet().forEach(diffEntrySet -> {

            try {

                final Map<String, Object> attributes = diffEntrySet.getValue();

                final ActionOnDiff action = (ActionOnDiff) attributes.get("ACTION");
                final String id = String.valueOf(attributes.get("ID"));
                final String parentId = String.valueOf(attributes.get("PARENT_ID"));
                final String entityType = String.valueOf(attributes.get("ENTITY_TYPE"));

                LOGGER.info("Processing DIFF (Farm " + farm.getId() + ") => " +
                        "{ ACTION: '" + action.toString()
                        + "', ID: '" + id
                        + "', PARENT_ID: '" + parentId
                        + "', ENTITY_TYPE: '"
                        + entityType + "' }");

                entityTypes.get().add(entityType);

                final String managerEntityType = getManagerEntityType(entityType);

                JpaRepositoryWithFindByName repository = getRepository(managerEntityType);
                if (repository != null) {
                    AbstractEntity<?> entityFromRepository = null;

                    SystemUserService.runAs();
                    Page<?> elements = repository.findByName(id, new PageRequest(0, Integer.MAX_VALUE));
                    Stream<AbstractEntity<?>> elementsStream = (Stream<AbstractEntity<?>>) StreamSupport.stream(elements.spliterator(), false);
                    entityFromRepository = getEntityIfExist(id, parentId, elementsStream).orElse(null);
                    SystemUserService.clearContext();

                    if (entityFromRepository == null && action != REMOVE) {
                        LOGGER.error("Entity " + id + " (parent: " + parentId + ") NOT FOUND [" + managerEntityType + "]");
                        CounterDownLatch.decrementDiffCounter(api);
                    } else {
                        AbstractEnqueuer queue = queueLocator.getQueue(managerEntityType);
                        if (action == REMOVE) {
                            LOGGER.debug("Sending " + id + " to " + queue + " queue [action: " + action + "]");
                            removeEntityFromFarm(driver, makeBaseProperty(api, id, parentId, entityType));
                        } else {
                            LOGGER.debug("Sending " + entityFromRepository.getName() + " to " + queue + " queue [action: " + action + "]");
                            final Map<String, String> jmsHeaders = new HashMap<>();
                            jmsHeaders.put("api", api);
                            switch (action) {
                                case CREATE:
                                    createEntityOnFarm(queue, entityFromRepository, jmsHeaders);
                                    break;
                                case UPDATE:
                                    updateEntityOnFarm(queue, entityFromRepository, jmsHeaders);
                                    break;
                                default:
                                    LOGGER.error("ACTION " + action + "(entityType: " + entityType + " - id: " + id + " - parentId: " + parentId + ") NOT EXIST");
                            }
                            LOGGER.debug("Send " + entityFromRepository.getName() + " to " + queue + " queue [action: " + action + "] finish");
                        }
                    }
                } else {
                    LOGGER.error("Repository is NULL: " + managerEntityType);
                }
            } catch (Exception e) {
                LOGGER.error(e);
                CounterDownLatch.decrementDiffCounter(api);
            }
        });
    }

    private void executeFullReload(Farm farm, Driver driver, Properties properties) {
        String farmClassName = farm.getClass().getSimpleName().toLowerCase();
        LOGGER.warn("Full Reloading " + farmClassName + " " + farm.getName());
        properties.put("path", farmClassName);
        driver.remove(properties);
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

    private void updateEntityOnFarm(AbstractEnqueuer<AbstractEntity<?>> queue, AbstractEntity<?> entityFromRepository, final Map<String, String> jmsHeaders) {
        queue.sendToQueue(queue.getQueueUpdateName(), entityFromRepository, jmsHeaders);
    }

    private void createEntityOnFarm(AbstractEnqueuer<AbstractEntity<?>> queue, AbstractEntity<?> entity, final Map<String, String> jmsHeaders) {
        queue.sendToQueue(queue.getQueueCreateName(), entity, jmsHeaders);
    }

    @SuppressWarnings("unchecked")
    private Optional<AbstractEntity<?>> getEntityIfExist(String id, String parentId, final Stream<AbstractEntity<?>> stream) {
        return stream
                .filter(entity -> entity.getName().equals(id))
                .filter(entity ->
                    (!(entity instanceof WithParent) && !(entity instanceof WithParents)) ||
                    (entity instanceof WithParent && (
                            ((WithParent<AbstractEntity<?>>) entity).getParent() != null &&
                                    ((WithParent<AbstractEntity<?>>) entity).getParent().getName().equals(parentId))) ||
                    (entity instanceof WithParents &&
                            !((WithParents<AbstractEntity<?>>) entity).getParents().isEmpty() &&
                            ((WithParents<AbstractEntity<?>>) entity).getParents().stream()
                                    .map(AbstractEntity::getName).collect(Collectors.toList()).contains(parentId))
        ).findAny();
    }

    @Override
    protected FarmRepository getFarmRepository() {
        return farmRepository;
    }

    @Override
    protected FarmQueue farmQueue() {
        return (FarmQueue)queueLocator.getQueue(Farm.class);
    }

    public Properties getPropertiesWithEntities(Farm farm, String api) {
        return getPropertiesWithEntities(farm, api, null);
    }

    public Properties getPropertiesWithEntities(Farm farm, String api, Map<String, List<?>> anEntitiesMap) {
        final Map<String, List<?>> entitiesMap = anEntitiesMap == null ? getEntitiesMap(farm) : anEntitiesMap;
        final Properties properties = new Properties();
        properties.put("api", api);
        properties.put("path", Farm.class.getSimpleName().toLowerCase());
        properties.put("entitiesMap", entitiesMap);
        properties.put("lockName", "lock_" + farm.getId());
        return properties;
    }

    private Map<String, List<?>> getEntitiesMap(Farm farm) {
        final Map<String, List<?>> entitiesMap = new HashMap<>();
        Authentication currentUser = CurrentUser.getCurrentAuth();
        SystemUserService.runAs();
        entitiesMap.put(VirtualHost.class.getSimpleName().toLowerCase(),
                virtualHostRepository.findByFarmId(farm.getId(), pageable).getContent());
        entitiesMap.put(BackendPool.class.getSimpleName().toLowerCase(),
                poolRepository.findByFarmId(farm.getId(), pageable).getContent());
        entitiesMap.put(Backend.class.getSimpleName().toLowerCase(),
                targetRepository.findByFarmId(farm.getId(), pageable).getContent());
        entitiesMap.put(Rule.class.getSimpleName().toLowerCase(),
                ruleRepository.findByFarmId(farm.getId(), pageable).getContent());
        SystemUserService.runAs(currentUser);
        return entitiesMap;
    }

}
