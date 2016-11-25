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

package io.galeb.manager.engine.driver.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.galeb.core.cluster.ignite.IgniteCacheFactory;
import io.galeb.core.jcache.CacheFactory;
import io.galeb.core.model.Backend;
import io.galeb.core.util.Constants;
import io.galeb.manager.common.Properties;
import io.galeb.manager.engine.driver.Driver;
import io.galeb.manager.engine.listeners.AbstractEngine;
import io.galeb.manager.engine.util.DiffProcessor;
import io.galeb.manager.httpclient.CommonHttpRequester;
import io.galeb.manager.httpclient.FarmClient;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static io.galeb.manager.engine.listeners.AbstractEngine.*;
import static io.galeb.manager.engine.util.CounterDownLatch.decrementDiffCounter;

public class GalebV32Driver implements Driver {

    public static final String DRIVER_NAME = GalebV32Driver.class.getSimpleName().replaceAll(DRIVER_SUFFIX, "");

    private static final Log LOGGER = LogFactory.getLog(GalebV32Driver.class);

    private static final CacheFactory CACHE_FACTORY = IgniteCacheFactory.getInstance().start();

    private final ObjectMapper mapper = new ObjectMapper();

    private Object resource = null;

    @Override
    public String toString() {
        return DRIVER_NAME;
    }

    @Override
    public Driver addResource(Object resource) {
        this.resource = resource;
        return this;
    }

    @Override
    public boolean exist(Properties properties) {
        String api = extractApiFromProperties(properties);
        String json = extractBodyFromProperties(properties);
        String path = pathWithId(extractPathFromProperties(properties), json);
        String uriPath = fullUriPath(api, path);

        boolean result = false;
        try {
            final JsonNode jsonNode = mapper.readTree(json);
            final JsonNode parentIdObj = jsonNode.get("parentId");
            final String parentId = parentIdObj != null ? parentIdObj.asText() : "";
            final CommonHttpRequester httpClient = getHttpClient();
            final ResponseEntity<String> response = httpClient.get(uriPath);
            result = httpClient.isStatusCodeEqualOrLessThan(response, HttpStatus.OK.value());
            result = result && !httpClient.bodyIsEmptyOrEmptyArray(response);
            result = result && hasExpectedParent(response.getBody(), parentId);
        } catch (IOException|URISyntaxException e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        }
        return result;
    }

    @Override
    public boolean create(Properties properties) {
        String api = extractApiFromProperties(properties);
        String json = extractBodyFromProperties(properties);
        String path = extractPathFromProperties(properties);
        String uriPath = fullUriPath(api, path);

        boolean result = false;
        try {
            final CommonHttpRequester httpClient = getHttpClient();
            final ResponseEntity<String> response = httpClient.post(uriPath, json);
            result = httpClient.isStatusCodeEqualOrLessThan(response, HttpStatus.ACCEPTED.value());
        } catch (RuntimeException|URISyntaxException e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        } finally {
            decrementDiffCounter(api);
        }
        return result;
    }

    @Override
    public boolean update(Properties properties) {
        String api = extractApiFromProperties(properties);
        String json = extractBodyFromProperties(properties);
        String path = pathWithId(extractPathFromProperties(properties),json);
        String uriPath = fullUriPath(api, path);

        boolean result = false;
        try {
            final CommonHttpRequester httpClient = getHttpClient();
            final ResponseEntity<String> response = httpClient.put(uriPath, json);
            result = httpClient.isStatusCodeEqualOrLessThan(response, HttpStatus.ACCEPTED.value());
        } catch (RuntimeException|URISyntaxException e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        } finally {
            decrementDiffCounter(api);
        }
        return result;
    }

    @Override
    public boolean remove(Properties properties) {
        String api = extractApiFromProperties(properties);
        String json = extractBodyFromProperties(properties);
        String path = pathWithId(extractPathFromProperties(properties), json);
        String uriPath = fullUriPath(api, path);

        boolean result = false;
        try {
            final CommonHttpRequester httpClient = getHttpClient();
            final String body = path.endsWith("/") ? "{\"id\":\"\",\"version\":0}" : json;
            final ResponseEntity<String> response = httpClient.delete(uriPath, body);
            result = httpClient.isStatusCodeEqualOrLessThan(response, HttpStatus.ACCEPTED.value());
            removeFromDistMap(json, path);
        } catch (Exception e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        } finally {
            decrementDiffCounter(api);
        }
        return result;
    }

    private void removeFromDistMap(String json, String path) {
        String id = getJsonElement(json, "id", false);
        String parentId = getJsonElement(json, "parentId", false);
        final Class<?> internalEntityTypeClass = ManagerToFarmConverter.FARM_TO_MANAGER_ENTITY_MAP.get(path);
        if (internalEntityTypeClass != null) {
            final Cache<String, String> distMap = CACHE_FACTORY.getCache(internalEntityTypeClass.getSimpleName());
            if (distMap != null) {
                distMap.remove(id + AbstractEngine.SEPARATOR + parentId);
            }
        }
    }

    @Override
    public Map<String, Map<String, Object>> diff(Properties properties,
                Map<String,Map<String,Map<String,String>>> getAll) throws Exception {
        return new DiffProcessor().setProperties(properties).getDiffMap(getAll);
    }

    @Override
    public Map<String,Map<String,Map<String,String>>> getAll(Properties properties) throws Exception {
        final Map<String,Map<String,Map<String,String>>> remoteMultiMap = new HashMap<>();
        final AtomicReference<Exception> exception = new AtomicReference<>(null);
        final String api = extractApiFromProperties(properties);
        Constants.ENTITY_CLASSES.stream().map(clazz -> clazz.getSimpleName().toLowerCase())
                .forEach(path -> {
                    try {
                        if (exception.get() == null) {
                            remoteMultiMap.put(path, extractRemoteMap(path, api));
                        }
                    } catch (Exception e) {
                        exception.set(e);
                    }
                });
        if (exception.get() != null) {
            throw exception.get();
        }
        return remoteMultiMap;
    }

    private String fullUriPath(String api, String path) {
        return api + "/" + path;
    }

    private String extractPathFromProperties(Properties properties) {
        return properties.getOrDefault(PATH_PROP, "").toString();
    }

    private String pathWithId(String path, String json) {
        String id = getIdEncoded(json);
        return path + "/" + id;
    }

    private String extractBodyFromProperties(final Properties properties) {
        return properties.getOrDefault(JSON_PROP, "{}").toString();
    }

    private String extractApiFromProperties(final Properties properties) {
        return properties.getOrDefault(API_PROP, "NULL").toString();
    }

    private boolean hasExpectedParent(String body, String expectedParent) {
        if (expectedParent == null || "".equals(expectedParent)) {
            return true;
        }

        final AtomicBoolean parentFound = new AtomicBoolean(false);
        try {
            final JsonNode json = mapper.readTree(body);
            if (json.isArray()) {
                json.forEach(jsonNode -> {
                    JsonNode parentIdObj = jsonNode.get("parentId");
                    if (parentIdObj != null && parentIdObj.isTextual() && parentIdObj.asText().equals(expectedParent)) {
                        parentFound.set(true);
                    }
                });
            }
        } catch (IOException e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        }

        return parentFound.get();
    }

    private String getIdEncoded(String json) {
        String id = "";
        if (json == null) {
            return "";
        }
        try {
            JsonNode idObj = mapper.readTree(json).get("id");
            if (idObj == null) {
                return "";
            }
            id = idObj.asText();
            if (id != null) {
                id = URLEncoder.encode(id, StandardCharsets.UTF_8.toString());
            } else {
                id = "";
            }
        } catch (IOException e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        }
        return id;
    }

    private Map<String, Map<String, String>> extractRemoteMap(String path, String api) throws Exception {
        final Map<String, Map<String, String>> remoteMap = new HashMap<>();
        String fullPath = fullUriPath(api, path);

        JsonNode json = getJson(fullPath);
        if (json != null && json.isArray()) {
            json.forEach(element -> {
                Map<String, String> entityProperties = new HashMap<>();
                String id = element.get("id").asText();
                JsonNode parentIdObj = element.get("parentId");
                String parentId = parentIdObj != null ? parentIdObj.asText() : "";
                String pk = element.get("pk").asText();
                String version = element.get("version").asText();
                String entityType = element.get("_entity_type").asText();
                String etag = element.get("_etag").asText();
                JsonNode healthObj = element.get("health");
                String health = healthObj != null ? healthObj.asText() : Backend.Health.UNKNOWN.toString();

                entityProperties.put("id", id);
                entityProperties.put("pk", pk);
                entityProperties.put("version", version);
                entityProperties.put("parentId", parentId);
                entityProperties.put("_entity_type", entityType);
                entityProperties.put("_etag", etag);
                entityProperties.put("health", health);
                remoteMap.put(fullPath + "/" + id + "@" + parentId, entityProperties);
            });
        }

        return remoteMap;
    }

    private JsonNode getJson(String fullPath) throws URISyntaxException, IOException {
        final CommonHttpRequester httpClient = getHttpClient();
        ResponseEntity<String> response = httpClient.get(fullPath);
        boolean result = httpClient.isStatusCodeEqualOrLessThan(response, 200);
        return result ? mapper.readTree(response.getBody()) : null;
    }

    private CommonHttpRequester getHttpClient() {
        return (resource instanceof CommonHttpRequester) ? (CommonHttpRequester)resource : new FarmClient();
    }

}
