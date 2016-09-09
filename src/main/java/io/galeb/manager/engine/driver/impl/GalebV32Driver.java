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
import io.galeb.core.model.Backend;
import io.galeb.core.util.Constants;
import io.galeb.manager.common.Properties;
import io.galeb.manager.engine.driver.Driver;
import io.galeb.manager.engine.util.DiffProcessor;
import io.galeb.manager.httpclient.HttpClient;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static io.galeb.manager.engine.util.CounterDownLatch.decrementDiffCounter;

public class GalebV32Driver implements Driver {

    public static final String DRIVER_NAME = GalebV32Driver.class.getSimpleName().replaceAll(DRIVER_SUFFIX, "");

    private static final Log LOGGER = LogFactory.getLog(GalebV32Driver.class);

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String toString() {
        return DRIVER_NAME;
    }

    @Override
    public Driver addResource(Object resource) {
        return this;
    }

    @Override
    public boolean exist(Properties properties) {
        String api = properties.getOrDefault("api", "NULL").toString();
        api = !api.startsWith("http") ? "http://" + api : api;
        String json = properties.getOrDefault("json", "{}").toString();
        String path = properties.getOrDefault("path", "").toString() + "/" + getIdEncoded(json);
        String uriPath = api + "/" + path;

        boolean result = false;
        try {
            final JsonNode jsonNode = mapper.readTree(json);
            final JsonNode parentIdObj = jsonNode.get("parentId");
            final HttpClient httpClient = new HttpClient();
            final ResponseEntity<String> response = httpClient.get(uriPath);
            result = httpClient.isStatusCodeEqualOrLessThan(response, 399);
            result = result && hasExpectedParent(response, parentIdObj.asText());
        } catch (IOException|URISyntaxException e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        }
        return result;
    }

    @Override
    public boolean create(Properties properties) {
        String api = properties.getOrDefault("api", "NULL").toString();
        String keyInProgress = api;
        api = !api.startsWith("http") ? "http://" + api : api;
        String json = properties.getOrDefault("json", "{}").toString();
        String path = properties.getOrDefault("path", "").toString();
        String uriPath = api + "/" + path;

        boolean result = false;
        try {
            final HttpClient httpClient = new HttpClient();
            final ResponseEntity<String> response = httpClient.post(uriPath, json);
            result = httpClient.isStatusCodeEqualOrLessThan(response, 399);
        } catch (RuntimeException|URISyntaxException e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        } finally {
            decrementDiffCounter(keyInProgress);
        }
        return result;
    }

    @Override
    public boolean update(Properties properties) {
        String api = properties.getOrDefault("api", "NULL").toString();
        String keyInProgress = api;
        api = !api.startsWith("http") ? "http://" + api : api;
        String json = properties.getOrDefault("json", "{}").toString();
        String path = properties.getOrDefault("path", "").toString() + "/" +getIdEncoded(json);
        String uriPath = api + "/" + path;

        boolean result = false;
        try {
            final HttpClient httpClient = new HttpClient();
            final ResponseEntity<String> response = httpClient.put(uriPath, json);
            result = httpClient.isStatusCodeEqualOrLessThan(response, 399);
        } catch (RuntimeException|URISyntaxException e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        } finally {
            decrementDiffCounter(keyInProgress);
        }
        return result;
    }

    @Override
    public boolean remove(Properties properties) {
        String api = properties.getOrDefault("api", "NULL").toString();
        String keyInProgress = api;
        api = !api.startsWith("http") ? "http://" + api : api;
        String json = properties.getOrDefault("json", "{}").toString();
        String path = properties.getOrDefault("path", "").toString();
        String id = getIdEncoded(json);
        path = !"".equals(id) ? path + "/" + id : path;
        String uriPath = api + "/" + path;

        boolean result = false;
        try {
            final HttpClient httpClient = new HttpClient();
            final String body = !"".equals(id) ? json : "{\"id\":\"\",\"version\":0}";
            final ResponseEntity<String> response = httpClient.delete(uriPath, body);
            result = httpClient.isStatusCodeEqualOrLessThan(response, 399);
        } catch (Exception e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        } finally {
            decrementDiffCounter(keyInProgress);
        }
        return result;
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
        final String apiFromProperties = properties.getOrDefault("api", "NULL").toString();
        final String api = !apiFromProperties.startsWith("http") ? "http://" + apiFromProperties : apiFromProperties;
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

    private boolean hasExpectedParent(ResponseEntity<String> response, String expectedParent) {
        final AtomicBoolean parentFound = new AtomicBoolean(false);

        try {
            final JsonNode json = mapper.readTree(response.getBody());
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
        final Map<String, Map<String, String>> fullMap = new HashMap<>();
        String fullPath = api + "/" + path;

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
                fullMap.put(fullPath + "/" + id + "@" + parentId, entityProperties);
            });
        }

        return fullMap;
    }

    private JsonNode getJson(String path) throws URISyntaxException, IOException {
        final HttpClient httpClient = new HttpClient();
        ResponseEntity<String> response = httpClient.get(path);
        boolean result = httpClient.isStatusCodeEqualOrLessThan(response, 399);
        return result ? mapper.readTree(response.getBody()) : null;
    }
}
