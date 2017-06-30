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
import io.galeb.manager.entity.*;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.cache.Cache;
import java.io.Serializable;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static io.galeb.manager.engine.listeners.AbstractEngine.SEPARATOR;
import static io.galeb.manager.engine.util.ManagerToFarmConverter.FARM_TO_MANAGER_ENTITY_MAP;
import static java.util.stream.Collectors.toSet;

public class DistMap implements Serializable {

    private static final DistMap INSTANCE = new DistMap();

    public static final CacheFactory CACHE_FACTORY = IgniteCacheFactory.getInstance().start();

    public static final String DIST_MAP_FARM_ID_PROP = "DIST_MAP_FARM_ID_PROP";
    private static final Log LOGGER = LogFactory.getLog(DistMap.class);

    public static DistMap getInstance() {
        return INSTANCE;
    }

    private DistMap() {
        //
    }

    /**
     * Returns all values by cache name of distributed map
     * @param cacheName cache name simple
     * @return all values
     */
    public Cache<String, String> getAll(String cacheName) {
        return CACHE_FACTORY.getCache(cacheName);
    }

    /**
     * Returns value of the distributed map.
     * The cache name and key are simple values.
     * @param cacheName
     * @param key
     * @return value of the distributed map
     */
    public String get(String cacheName, String key) {
        Cache<String, String> distMap = CACHE_FACTORY.getCache(cacheName);
        return distMap.get(key);
    }

    /**
     * Puts the value in the distributed map.
     * The cache name and key are simple values.
     *
     * @param cacheName
     * @param key
     * @param value
     */
    public void put(String cacheName, String key, String value) {
        Cache<String, String> distMap = CACHE_FACTORY.getCache(cacheName);
        distMap.put(key, value);
    }

    /**
     * Returns value of the distributed map.
     * The cache name is Simple Name in Lower Case of AbstractEntity.
     * The key is generated by method #getKey
     *
     * @param entity an AbstractEntity instance
     * @return value of the distributed map
     */
    public String get(AbstractEntity<?> entity) {
        Cache<String, String> distMap = CACHE_FACTORY.getCache(entity.getClass().getSimpleName().toLowerCase());
        String key = getKey(entity);
        return distMap.get(key);
    }

    /**
     * Puts the value in the distributed map.
     * The cache name is the Simples Name in Lower Case of Entity converted by type.
     * The key is generated by method #getKey
     *
     * @param entity an Entity to convert by type
     * @param value the value to put
     */
    public void put(Entity entity, String value) {
        final Class<?> internalEntityTypeClass = FARM_TO_MANAGER_ENTITY_MAP.get(entity.getEntityType());
        Cache<String, String> distMap = CACHE_FACTORY.getCache(internalEntityTypeClass.getSimpleName().toLowerCase());
        String key = getKey(entity);
        distMap.put(key, value);
    }

    /**
     * Puts the value in the distributed map.
     * The cache name is the Simples Name in Lower Case of AbstractEntity.
     * The key is generated by method #getKey
     *
     * @param entity an AbstractEntity instance
     * @param value the value to put
     */
    public void put(AbstractEntity<?> entity, String value) {
        Cache<String, String> distMap = CACHE_FACTORY.getCache(entity.getClass().getSimpleName().toLowerCase());
        String key = getKey(entity);
        distMap.put(key, value);
    }

    /**
     * Remove the value in the distributed map.
     * The cache name is the Simples Name in Lower Case of AbstractEntity.
     * The key is generated by method #getKey
     *
     * @param entity an AbstractEntity instance
     */
    public void remove(AbstractEntity<?> entity) {
        Cache<String, String> distMap = CACHE_FACTORY.getCache(entity.getClass().getSimpleName().toLowerCase());
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
                Set<String> keys = keysToRemove.collect(toSet());
                cache.removeAll(keys);
            } catch (Exception e) {
                LOGGER.error("DistMap.removeAll FAILED");
                LOGGER.error(ExceptionUtils.getStackTrace(e));
            }

        });
    }

}
