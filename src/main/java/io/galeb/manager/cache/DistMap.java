/*
 * Galeb - Load Balance as a Service Plataform
 *
 * Copyright (C) 2014-2015 Globo.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */

package io.galeb.manager.cache;

import io.galeb.core.cluster.ignite.IgniteCacheFactory;
import io.galeb.core.jcache.CacheFactory;
import io.galeb.core.model.Entity;
import io.galeb.manager.engine.util.ManagerToFarmConverter;
import io.galeb.manager.entity.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import javax.cache.Cache;

import java.io.Serializable;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static io.galeb.manager.engine.listeners.AbstractEngine.SEPARATOR;
import static io.galeb.manager.engine.util.ManagerToFarmConverter.FARM_TO_MANAGER_ENTITY_MAP;
import static java.util.stream.Collectors.toSet;

@Service
public class DistMap implements Serializable {

    public static final CacheFactory CACHE_FACTORY = IgniteCacheFactory.getInstance().start();

    public static final String DIST_MAP_FARM_ID_PROP = "DIST_MAP_FARM_ID_PROP";
    private static final Log LOGGER = LogFactory.getLog(DistMap.class);

    public String get(AbstractEntity<?> entity) {
        Cache<String, String> distMap = CACHE_FACTORY.getCache(entity.getClass().getSimpleName());
        String key = getKey(entity);
        return distMap.get(key);
    }

    public void put(Entity entity, String value) {
        final Class<?> internalEntityTypeClass = FARM_TO_MANAGER_ENTITY_MAP.get(entity.getEntityType());
        Cache<String, String> distMap = CACHE_FACTORY.getCache(internalEntityTypeClass.getSimpleName());
        String key = getKey(entity);
        distMap.put(key, value);
    }

    public void put(AbstractEntity<?> entity, String value) {
        Cache<String, String> distMap = CACHE_FACTORY.getCache(entity.getClass().getSimpleName());
        String key = getKey(entity);
        distMap.put(key, value);
    }

    public void remove(AbstractEntity<?> entity) {
        Cache<String, String> distMap = CACHE_FACTORY.getCache(entity.getClass().getSimpleName());
        String key = getKey(entity);
        distMap.remove(key);
    }

    public String getKey(AbstractEntity<?> entity) {
        if (entity instanceof Farm) {
            return Farm.class.getSimpleName() + entity.getId();
        }
        Long farmId = -1L;
        if (entity instanceof Rule) {
            farmId = ((Rule)entity).getPool().getFarmId();
        } else if (entity instanceof WithFarmID) {
            farmId = ((WithFarmID)entity).getFarmId();
        }
        String key = farmId.toString() + SEPARATOR;
        key += entity.getName() + SEPARATOR;
        if  (entity instanceof WithParent) {
            key += ((WithParent) entity).getParent().getName();
        }
        return key;
    }

    public String getKey(Entity entity) {
        Long farmId = (Long) entity.getProperties().get(DIST_MAP_FARM_ID_PROP);
        String key = "";
        if (farmId != null) {
            key += farmId.toString() + SEPARATOR;
        }
        key += entity.getId() + SEPARATOR;
        if (entity.getParentId() != null) {
            key += entity.getParentId();
        }
        return key;
    }

    public void resetFarm(Long farmId) {
        LOGGER.warn(this.getClass().getSimpleName() + ".resetFarm( " + farmId +" ) initialized");
        removeAll(s -> s.startsWith(farmId.toString() + SEPARATOR));
        LOGGER.warn(this.getClass().getSimpleName() +".resetFarm( " + farmId + " ) called.");
    }

    public void removeAll(Predicate<String> predicate) {
        FARM_TO_MANAGER_ENTITY_MAP.values().stream()
                .filter(WithFarmID.class::isAssignableFrom)
                .map(c -> c.getSimpleName().toLowerCase())
                .map(CACHE_FACTORY::getCache)
                .filter(Objects::nonNull)
                .forEach(cache -> {

            try {
                final Stream<String> keysToRemove = StreamSupport.stream(cache.spliterator(), false).map(Cache.Entry::getKey).filter(predicate);
                cache.removeAll(keysToRemove.collect(toSet()));
            } catch (Exception e) {
                LOGGER.error("DistMap.removeAll FAILED: " + e.getMessage());
            }

        });
    }
}
