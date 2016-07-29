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
import io.galeb.manager.entity.AbstractEntity;
import io.galeb.manager.entity.Farm;
import io.galeb.manager.entity.Rule;
import io.galeb.manager.entity.WithParent;
import org.springframework.stereotype.Service;

import javax.cache.Cache;

import static io.galeb.manager.engine.listeners.AbstractEngine.SEPARATOR;

@Service
public class DistMap {

    public static final CacheFactory CACHE_FACTORY = IgniteCacheFactory.getInstance().start();

    public static final String DIST_MAP_FARM_ID_PROP = "DIST_MAP_FARM_ID_PROP";

    public String get(AbstractEntity<?> entity) {
        Cache<String, String> distMap = CACHE_FACTORY.getCache(entity.getClass().getSimpleName());
        return distMap.get(getKey(entity));
    }

    public void put(Entity entity, String value) {
        final Class<?> internalEntityTypeClass = ManagerToFarmConverter.FARM_TO_MANAGER_ENTITY_MAP.get(entity.getEntityType());
        Cache<String, String> distMap = CACHE_FACTORY.getCache(internalEntityTypeClass.getClass().getSimpleName());
        distMap.put(getKey(entity), value);
    }

    public void put(AbstractEntity<?> entity, String value) {
        Cache<String, String> distMap = CACHE_FACTORY.getCache(entity.getClass().getSimpleName());
        distMap.put(getKey(entity), value);
    }

    public void remove(AbstractEntity<?> entity) {
        Cache<String, String> distMap = CACHE_FACTORY.getCache(entity.getClass().getSimpleName());
        distMap.remove(getKey(entity));
    }

    public String getKey(AbstractEntity<?> entity) {
        if (entity instanceof Farm) {
            return Farm.class.getSimpleName() + entity.getId();
        }
        Long farmId = -1L;
        if (entity instanceof Rule) {
            farmId = ((Rule)entity).getPool().getFarmId();
        }
        String key = farmId.toString() + SEPARATOR;
        key += entity.getName() + SEPARATOR;
        if  (entity instanceof WithParent) {
            key += ((WithParent) entity).getParent();
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
}
