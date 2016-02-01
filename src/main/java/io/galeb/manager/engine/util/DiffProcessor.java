/*
 * Galeb - Load Balance as a Service Plataform
 *
 * Copyright (C) 2014-2016 Globo.com
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

package io.galeb.manager.engine.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.galeb.core.util.Constants;
import io.galeb.manager.common.Properties;
import io.galeb.manager.entity.AbstractEntity;
import io.galeb.manager.entity.WithAliases;
import io.galeb.manager.entity.WithParent;
import io.galeb.manager.entity.WithParents;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static io.galeb.manager.engine.driver.Driver.ActionOnDiff.CALLBACK;
import static io.galeb.manager.engine.driver.Driver.ActionOnDiff.UPDATE;
import static io.galeb.manager.engine.driver.Driver.ActionOnDiff.CREATE;
import static io.galeb.manager.engine.driver.Driver.ActionOnDiff.REMOVE;

import static io.galeb.manager.entity.AbstractEntity.EntityStatus.DISABLED;
import static io.galeb.manager.entity.AbstractEntity.EntityStatus.PENDING;
import static io.galeb.manager.entity.AbstractEntity.EntityStatus.ERROR;
import static java.util.stream.Collectors.toList;

public class DiffProcessor {

    private static final Log LOGGER = LogFactory.getLog(DiffProcessor.class);

    private final ObjectMapper mapper = new ObjectMapper();

    private Properties properties;
    private String api = "";
    private final Map<String, Map<String, Object>> diffMap = new HashMap<>();
    private Map<String, List<?>> entitiesMap = new HashMap<>();

    public DiffProcessor setProperties(Properties properties) {
        this.properties = properties;
        return this;
    }

    public Map<String, Map<String, Object>> getDiffMap() throws Exception {
        final AtomicReference<String> error = new AtomicReference<>(null);
        getEntitiesMap().keySet().stream().forEach(path -> {
            try {
                makeDiffMap(path);
            } catch (Exception e) {
                error.set(e.getMessage());
            }
        });
        if (error.get() != null) {
            throw new RuntimeException(error.get());
        }
        return diffMap;
    }

    private String getApi() {
        if ("".equals(api) && properties != null) {
            api = properties.getOrDefault("api", "localhost:9090").toString();
            api = !api.startsWith("http") ? "http://" + api : api;
        }
        return api;
    }

    @SuppressWarnings("unchecked")
    private Map<String, List<?>> getEntitiesMap() {
        if (entitiesMap.isEmpty() && properties != null) {
            entitiesMap = (Map <String, List<?>>)properties.getOrDefault("entitiesMap", Collections.emptyMap());
        }
        return entitiesMap;
    }

    @SuppressWarnings("unchecked")
    private void makeDiffMap(String path) throws Exception {
        final Map<String, Map<String, String>> fullMap = extractRemoteMap(path);
        List<?> entities = getEntitiesMap().get(path);

        fullMap.entrySet().stream()
                .filter(entry ->
                        entry.getValue().getOrDefault("entity_type", "UNDEF").equals(path))
                .forEach(entry ->
                {
                    final Map<String, String> entityProperties = entry.getValue();
                    final String id = entityProperties.getOrDefault("id", "UNDEF");
                    final String parentId = entityProperties.getOrDefault("parentId", "UNDEF");
                    AtomicBoolean hasId = new AtomicBoolean(false);

                    entities.stream().filter(entity -> entity instanceof AbstractEntity<?>)
                            .map(entity -> ((AbstractEntity<?>) entity))
                            .filter(entity -> entity.getName().equals(id))
                            .filter(getAbstractEntityPredicate(parentId))
                            .forEach(entity ->
                            {
                                hasId.set(true);
                                updateIfNecessary(path, id, parentId, entity, entityProperties);
                            });
                    entities.stream().filter(entity -> !hasId.get() && entity instanceof WithAliases).forEach(entity ->
                    {
                        WithAliases<?> withAliases = (WithAliases) entity;
                        if (withAliases.getAliases().contains(id)) {
                            hasId.set(true);
                        }
                    });
                    entities.stream().filter(entity -> !hasId.get() && entity instanceof WithParents).forEach(entity -> {
                        WithParents<?> withParents = (WithParents) entity;
                        Set<WithAliases> withAliases = new HashSet<>((Collection<? extends WithAliases>) withParents.getParents());
                        boolean isAlias = withAliases.stream().map(WithAliases::getAliases)
                                .flatMap(Collection::stream).collect(Collectors.toSet()).contains(parentId);
                        if (isAlias) {
                            hasId.set(true);
                        }
                    });
                    removeEntityIfNecessary(path, id, parentId, hasId);
                });

        entities.stream().filter(entity -> entity instanceof AbstractEntity<?> &&
                ((AbstractEntity<?>)entity).getStatus() != DISABLED)
                .map(entity -> ((AbstractEntity<?>) entity))
                .forEach(entity -> createEntityIfNecessary(path, entity, fullMap));
    }

    private void updateIfNecessary(String path,
                                   String id,
                                   String parentId,
                                   final AbstractEntity<?> entity,
                                   final Map<String, String> entityProperties) {
        final String version = entityProperties.getOrDefault("version", "UNDEF");
        final String pk = entityProperties.getOrDefault("pk", "UNDEF");
        LOGGER.debug("Check if is necessary UPDATE");
        if (!version.equals(String.valueOf(entity.getHash())) || !pk.equals(String.valueOf(entity.getId()))) {
            changeAction(path, id, parentId);
        } else {
            if (entity.getStatus() == PENDING || entity.getStatus() == ERROR ) {
                callbackStatusOkAction(path, id, parentId);
            }
        }
    }

    private void removeEntityIfNecessary(String path,
                                         String id,
                                         String parentId,
                                         final AtomicBoolean hasId) {
        LOGGER.debug("Check if is necessary REMOVE");
        if (!hasId.get()) {
            delAction(path, id, parentId);
        }
    }

    @SuppressWarnings("unchecked")
    private void createEntityIfNecessary(String path,
                                         final AbstractEntity<?> entity,
                                         final Map<String, Map<String, String>> fullMap) {
        String id = entity.getName();
        LOGGER.debug("Check if is necessary CREATE");
        if (!(entity instanceof WithParent) && !(entity instanceof WithParents)) {
            addAction(path, id, "", fullMap.keySet());
        }
        if (entity instanceof WithParent) {
            AbstractEntity<?> parentInstance = ((WithParent<AbstractEntity<?>>) entity).getParent();
            String parentId = parentInstance != null ? parentInstance.getName() : "";
            addAction(path, id, parentId, fullMap.keySet());
        }
        if (entity instanceof WithParents) {
            ((WithParents<AbstractEntity<?>>) entity).getParents().forEach(aParent ->
            {
                String parentId = aParent.getName();
                addAction(path, id, parentId, fullMap.keySet());
            });
        }
    }

    @SuppressWarnings("unchecked")
    private Predicate<AbstractEntity<?>> getAbstractEntityPredicate(String parentId) {
        return entity -> (!(entity instanceof WithParent) && !(entity instanceof WithParents)) ||
                (entity instanceof WithParent) && (
                        ((WithParent<AbstractEntity<?>>) entity).getParent() != null &&
                                ((WithParent<AbstractEntity<?>>) entity).getParent().getName().equals(parentId)) ||
                (entity instanceof WithParents) &&
                        !((WithParents<AbstractEntity<?>>) entity).getParents().isEmpty() &&
                        ((WithParents<AbstractEntity<?>>) entity).getParents().stream()
                                .map(AbstractEntity::getName).collect(toList()).contains(parentId);
    }

    private Map<String, Map<String, String>> extractRemoteMap(String path) throws Exception {
        final Map<String, Map<String, String>> fullMap = new HashMap<>();
        final AtomicReference<String> error = new AtomicReference<>(null);
        String fullPath = getApi() + "/" + path;

        JsonNode json = getJson(fullPath);
        if (json.isArray()) {
            json.forEach(element -> {
                Map<String, String> entityProperties = new HashMap<>();
                String id = element.get("id").asText();
                JsonNode parentIdObj = element.get("parentId");
                String parentId = parentIdObj != null ? parentIdObj.asText() : "";
                String pk = element.get("pk").asText();
                String version = element.get("version").asText();
                String entityType = element.get("_entity_type").asText();
                String etag = element.get("_etag").asText();

                entityProperties.put("id", id);
                entityProperties.put("pk", pk);
                entityProperties.put("version", version);
                entityProperties.put("parentId", parentId);
                entityProperties.put("entity_type", entityType);
                entityProperties.put("etag", etag);
                fullMap.put(fullPath + "/" + id + "@" + parentId, entityProperties);
            });
        }

        return fullMap;
    }

    private void addAction(final String path,
                           final String id,
                           final String parentId,
                           final Set<String> setOfKeys) {
        String key = getApi() + "/" + path + "/" + id + "@" + parentId;
        if (!setOfKeys.contains(key)) {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("ACTION", CREATE);
            attributes.put("ID", id);
            attributes.put("PARENT_ID", parentId);
            attributes.put("ENTITY_TYPE", path);
            diffMap.put(key, attributes);
        }
    }

    private void changeAction(final String path,
                              final String id,
                              final String parentId) {
        Map<String, Object> attributes = new HashMap<>();
        String key = getApi() + "/" + path + "/" + id + "@" + parentId;
        attributes.put("ACTION", UPDATE);
        attributes.put("ID", id);
        attributes.put("PARENT_ID", parentId);
        attributes.put("ENTITY_TYPE", path);
        diffMap.put(key, attributes);
    }

    private void delAction(final String path,
                           final String id,
                           final String parentId) {
        Map<String, Object> attributes = new HashMap<>();
        String key = getApi() + "/" + path + "/" + id + "@" + parentId;
        attributes.put("ACTION", REMOVE);
        attributes.put("ID", id);
        attributes.put("PARENT_ID", parentId);
        attributes.put("ENTITY_TYPE", path);
        diffMap.put(key, attributes);
    }


    private void callbackStatusOkAction(final String path,
                                        final String id,
                                        final String parentId) {
        Map<String, Object> attributes = new HashMap<>();
        String key = getApi() + "/" + path + "/" + id + "@" + parentId;
        attributes.put("ACTION", CALLBACK);
        attributes.put("ID", id);
        attributes.put("PARENT_ID", parentId);
        attributes.put("ENTITY_TYPE", path);
        diffMap.put(key, attributes);
    }

    private JsonNode getJson(String path) throws URISyntaxException, IOException {
        JsonNode json = null;
        RestTemplate restTemplate = new RestTemplate();
        URI uri = new URI(path);
        RequestEntity<Void> request = RequestEntity.get(uri).build();
        ResponseEntity<String> response = restTemplate.exchange(request, String.class);
        boolean result = response.getStatusCode().value() < 400;

        if (result) {
            json = mapper.readTree(response.getBody());
        }
        return json;
    }

}
