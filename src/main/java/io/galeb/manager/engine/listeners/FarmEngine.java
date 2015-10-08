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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jms.annotation.*;
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
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

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

    private AtomicBoolean isRead = new AtomicBoolean(false);
    private final Pageable pageable = new PageRequest(0, Integer.MAX_VALUE);

    @PostConstruct
    public void init() {
        isRead.set(true);
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

    private AbstractEnqueuer getQueue(String entityClass) {
        switch (entityClass) {
            case "virtualhost":
                return virtualHostQueue;
            case "rule":
                return ruleQueue;
            case "pool":
                return poolQueue;
            case "target":
                return targetQueue;
            default:
                LOGGER.error("Entity Class " + entityClass + " NOT FOUND");
                return null;
        }
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

    @JmsListener(destination = FarmQueue.QUEUE_SYNC)
    public void sync(Map.Entry<Farm, Map<String, Object>> entrySet) {
        Farm farm = entrySet.getKey();
        String farmLock = farm.getName() + ".lock";

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

        diff.entrySet().stream().forEach(diffEntrySet -> {

            final Map<String, String> attributes = (Map<String, String>) diffEntrySet.getValue();

            final String action = attributes.get("ACTION");
            final String id = attributes.get("ID");
            final String parentId = attributes.get("PARENT_ID");
            final String entityType = attributes.get("ENTITY_TYPE");

            final String internalEntityType = getInternalEntityType(entityType);

            JpaRepositoryWithFindByName repository = getRepository(internalEntityType);
            if (repository != null) {
                long totalElements = repository.findByName(id, pageable).getTotalElements();
                long pageSize = totalElements > 100 ? 100 : totalElements;
                int page = 0;
                long numPages = totalElements / pageSize;
                AbstractEntity<?> entityFromRepository = null;

                while (entityFromRepository == null && page < numPages + 1) {
                    Iterator<AbstractEntity<?>> iter = repository.findByName(id, new PageRequest(page, (int) pageSize)).iterator();
                    entityFromRepository = getEntityIfExist(id, parentId, iter).orElse(null);
                    page++;
                }

                if (entityFromRepository == null) {
                    LOGGER.error("Entity " + id + " (parent: " + parentId + ") NOT FOUND [repository: " + repository + "]");
                } else {
                    AbstractEnqueuer queue = getQueue(internalEntityType);
                    LOGGER.debug("Sending " + entityFromRepository.getName() + " to " + queue + " queue [action: " + action + "]");
                    switch (action.toUpperCase()) {
                        case "CREATE":
                            createEntityOnFarm(queue, entityFromRepository);
                            break;
                        case "UPDATE":
                            updateEntityOnFarm(queue, entityFromRepository);
                            break;
                        case "REMOVE":
                            removeEntityFromFarm(driver, makeBaseProperty(farm.getApi(), id, parentId, entityType));
                            break;
                        default:
                            LOGGER.error("ACTION " + action + "(entityType: " + entityType + " - id: " + id + " - parentId: " + parentId + ") NOT EXIST");
                    }
                    LOGGER.debug("Send " + entityFromRepository.getName() + " to " + queue + " queue [action: " + action + "] finish");
                }
            }
        });
        distributedLocker.release(farmLock);
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

    private void updateEntityOnFarm(AbstractEnqueuer<AbstractEntity<?>> queue, AbstractEntity<?> entityFromRepository) {
        queue.sendToQueue(queue.getQueueUpdateName(), entityFromRepository);
    }

    private void createEntityOnFarm(AbstractEnqueuer<AbstractEntity<?>> queue, AbstractEntity<?> entity) {
        queue.sendToQueue(queue.getQueueCreateName(), entity);
    }

    private Optional<AbstractEntity<?>> getEntityIfExist(String id, String parentId, final Iterator<AbstractEntity<?>> iterator) {
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

    private String getInternalEntityType(String entityType) {
        return entityType.toLowerCase().equals(BackendPool.class.getSimpleName().toLowerCase()) ?
                    Pool.class.getSimpleName().toLowerCase() :
                entityType.toLowerCase().equals(Backend.class.getSimpleName().toLowerCase()) ?
                    Target.class.getSimpleName().toLowerCase() : entityType;
    }

    @JmsListener(destination = FarmQueue.QUEUE_CALLBK)
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

}
