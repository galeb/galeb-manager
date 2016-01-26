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

import static io.galeb.core.util.Constants.ENTITY_MAP;
import static io.galeb.manager.engine.driver.Driver.ActionOnDiff.REMOVE;
import static io.galeb.manager.engine.driver.DriverBuilder.addResource;
import static io.galeb.manager.engine.driver.DriverBuilder.getDriver;
import static io.galeb.manager.entity.AbstractEntity.EntityStatus.PENDING;
import static io.galeb.manager.entity.AbstractEntity.EntityStatus.ERROR;
import static io.galeb.manager.entity.AbstractEntity.EntityStatus.OK;
import static io.galeb.manager.scheduler.tasks.SyncFarms.LOCK_PREFIX;

import io.galeb.manager.engine.driver.Driver.ActionOnDiff;
import io.galeb.manager.entity.AbstractEntity;
import io.galeb.manager.entity.Farm;
import io.galeb.manager.entity.WithParent;
import io.galeb.manager.entity.WithParents;
import io.galeb.manager.queue.AbstractEnqueuer;
import io.galeb.manager.queue.FarmQueue;
import io.galeb.manager.queue.PoolQueue;
import io.galeb.manager.queue.RuleQueue;
import io.galeb.manager.queue.TargetQueue;
import io.galeb.manager.queue.VirtualHostQueue;
import io.galeb.manager.repository.FarmRepository;
import io.galeb.manager.repository.JpaRepositoryWithFindByName;
import io.galeb.manager.repository.PoolRepository;
import io.galeb.manager.repository.RuleRepository;
import io.galeb.manager.repository.TargetRepository;
import io.galeb.manager.repository.VirtualHostRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import io.galeb.manager.common.Properties;
import io.galeb.manager.engine.driver.Driver;
import io.galeb.manager.engine.provisioning.Provisioning;
import io.galeb.manager.security.user.CurrentUser;
import io.galeb.manager.security.services.SystemUserService;
import io.galeb.manager.engine.listeners.services.GenericEntityService;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Component
public class FarmEngine extends AbstractEngine<Farm> {

    private static final Log LOGGER = LogFactory.getLog(FarmEngine.class);

    @Override
    protected Log getLogger() {
        return LOGGER;
    }

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

    private AtomicBoolean isRead = new AtomicBoolean(false);

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
        LOGGER.info("Creating " + farm.getClass().getSimpleName() + " " + farm.getName());
        Provisioning provisioning = getProvisioning(farm);
        boolean isOk = false;
        try {
            isOk = provisioning.create(fromEntity(farm));
        } catch (Exception e) {
            LOGGER.error(e);
        } finally {
            farm.setStatus(isOk ? OK : ERROR);
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
            farm.setStatus(isOk ? OK : ERROR);
            farmQueue.sendToQueue(FarmQueue.QUEUE_CALLBK, farm);
        }
    }

    @Override
    protected void update(Farm entity) {
        //
    }

    @SuppressWarnings("unchecked")
    @JmsListener(destination = FarmQueue.QUEUE_SYNC)
    public void sync(Map.Entry<Farm, Map<String, Object>> entrySet) {
        Farm farm = entrySet.getKey();
        String farmLock = LOCK_PREFIX + String.valueOf(farm.getId());

        Map<String, Object> diff = entrySet.getValue();
        Driver driver = addResource(getDriver(farm), cacheFactory);
        Properties properties = new Properties();
        SystemUserService.runAs();
        properties.put("api", farm.getApi());
        SystemUserService.clearContext();

        if (diff == null) {
            executeFullReload(farm, driver, properties);
            return;
        }

        LOGGER.warn("Syncing " + farm.getClass().getSimpleName() + " " + farm.getName());

        final Set<String> entityTypes = new HashSet<>();

        diff.entrySet().stream().forEach(diffEntrySet -> {

            final Map<String, Object> attributes = (Map<String, Object>) diffEntrySet.getValue();

            final ActionOnDiff action = (ActionOnDiff) attributes.get("ACTION");
            final String id = String.valueOf(attributes.get("ID"));
            final String parentId = String.valueOf(attributes.get("PARENT_ID"));
            final String entityType = String.valueOf(attributes.get("ENTITY_TYPE"));

            if (lockWithId(farmLock, entityType, id + SEPARATOR + parentId)) {

                entityTypes.add(entityType);

                final String managerEntityType = getManagerEntityType(entityType);

                JpaRepositoryWithFindByName repository = getRepository(managerEntityType);
                if (repository != null) {
                    AbstractEntity<?> entityFromRepository = null;
                    int pageSize = 100;
                    int page = 0;
                    SystemUserService.runAs();
                    Page<?> elements = repository.findByName(id, new PageRequest(page, pageSize));

                    while (elements.hasContent() && entityFromRepository == null) {
                        entityFromRepository = getEntityIfExist(id, parentId, (Iterator<AbstractEntity<?>>) elements.iterator()).orElse(null);
                        if (!elements.isLast()) {
                            elements = repository.findByName(id, new PageRequest(++page, pageSize));
                        } else {
                            break;
                        }
                    }
                    SystemUserService.clearContext();

                    if (entityFromRepository == null && action != REMOVE) {
                        LOGGER.error("Entity " + id + " (parent: " + parentId + ") NOT FOUND [" + managerEntityType + "]");
                    } else {
                        AbstractEnqueuer queue = getQueue(managerEntityType);
                        if (action == REMOVE) {
                            LOGGER.debug("Sending " + id + " to " + queue + " queue [action: " + action + "]");
                            removeEntityFromFarm(driver, makeBaseProperty(farm.getApi(), id, parentId, entityType));
                            Class<?> entityTypeClass = ENTITY_MAP.get(entityType);
                            if (entityTypeClass != null) {
                                String lockPrefix = farmLock + SEPARATOR + entityTypeClass.getSimpleName();
                                releaseLockWithId(id, parentId, lockPrefix);
                            }
                        } else {
                            LOGGER.debug("Sending " + entityFromRepository.getName() + " to " + queue + " queue [action: " + action + "]");
                            switch (action) {
                                case CREATE:
                                    createEntityOnFarm(queue, entityFromRepository);
                                    break;
                                case UPDATE:
                                    updateEntityOnFarm(queue, entityFromRepository);
                                    break;
                                case CALLBACK:
                                    resendCallBackWithOK(queue, entityFromRepository);
                                    break;
                                default:
                                    LOGGER.error("ACTION " + action + "(entityType: " + entityType + " - id: " + id + " - parentId: " + parentId + ") NOT EXIST");
                            }
                            LOGGER.debug("Send " + entityFromRepository.getName() + " to " + queue + " queue [action: " + action + "] finish");
                        }
                    }
                }
            }
        });
    }

    private void resendCallBackWithOK(final AbstractEnqueuer<AbstractEntity<?>> queue, final AbstractEntity<?> entityFromRepository) {
        if (entityFromRepository.getStatus() == PENDING || entityFromRepository.getStatus() == ERROR ) {
            entityFromRepository.setStatus(OK);
            queue.sendToQueue(queue.getQueueCallBackName(), entityFromRepository);
        }
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

    @SuppressWarnings("unchecked")
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
