/*
 *   Galeb - Load Balance as a Service Plataform
 *
 *   Copyright (C) 2014-2016 Globo.com
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

package io.galeb.manager.httpclient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.galeb.core.json.JsonObject;
import io.galeb.core.model.Entity;
import io.galeb.core.util.Constants;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class FakeFarmClient implements CommonHttpRequester {

    private static final Log LOGGER = LogFactory.getLog(FakeFarmClient.class);

    private static final String RESULT_OK          = "Result: OK \n";
    private static final String RESULT_NOT_FOUND   = "Result: NOT FOUND";
    private static final String RESULT_BAD_REQUEST = "Result: BAD REQUEST";
    private static final String RESULT_ACCEPTED    = "Result: ACCEPTED";
    private static final String EMPTY_STR          = "";
    private static final String FIELD_PARENT_ID    = "parentId";
    private static final String FIELD_ID           = "id";

    private final Map<String, ConcurrentHashMap<String, String>> mapOfmaps = new HashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();

    public FakeFarmClient() {
        Constants.ENTITY_CLASSES.stream()
                                .map(c -> c.getSimpleName().toLowerCase())
                                .forEach(e -> mapOfmaps.put(e, new ConcurrentHashMap<>()));
    }

    @Override
    public ResponseEntity<String> get(String uriPath) throws URISyntaxException {
        LOGGER.info("GET " + uriPath);
        String[] paths = getPathsWithSlash(uriPath);
        String entityPath = getEntityPath(paths);
        if ("farm".equals(entityPath)) {
            String response = "{ \"info\" : \"'GET /farm' was removed\" }";
            LOGGER.info(RESULT_OK + response);
            return ResponseEntity.ok(response);
        }
        String entityId = getEntityId(paths);
        String result = getArrayOfEntities(entityPath, entityId);
        if ("[]".equals(result)) {
            LOGGER.info(RESULT_NOT_FOUND);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(EMPTY_STR);
        } else {
            LOGGER.info(RESULT_OK + result);
            return ResponseEntity.ok(result);
        }
    }

    @Override
    public ResponseEntity<String> post(String uriPath, String body) throws URISyntaxException {
        LOGGER.info("POST " + uriPath + " \n" + body);
        if (body == null || EMPTY_STR.equals(body)) {
            LOGGER.info(RESULT_BAD_REQUEST);
            return ResponseEntity.badRequest().body(EMPTY_STR);
        }
        String[] paths = getPathsWithSlash(uriPath);
        String entityPath = getEntityPath(paths);
        if ("farm".equals(entityPath)) {
            LOGGER.info(RESULT_BAD_REQUEST);
            return ResponseEntity.badRequest().body(EMPTY_STR);
        }
        putIfAbsentToMap(entityPath, body);
        LOGGER.info(RESULT_ACCEPTED);
        return ResponseEntity.accepted().body(EMPTY_STR);
    }

    @Override
    public ResponseEntity<String> put(String uriPath, String body) throws URISyntaxException {
        LOGGER.info("PUT " + uriPath + " \n" + body);
        if (body == null || EMPTY_STR.equals(body)) {
            LOGGER.info(RESULT_BAD_REQUEST);
            return ResponseEntity.badRequest().body(EMPTY_STR);
        }
        String[] paths = getPathsWithSlash(uriPath);
        String entityPath = getEntityPath(paths);
        if ("farm".equals(entityPath)) {
            // TODO: Galeb.API ignore update Farm.
            LOGGER.info(RESULT_BAD_REQUEST);
            return ResponseEntity.badRequest().body(EMPTY_STR);
        }
        String entityId = getEntityId(paths);
        replaceIntoMap(entityPath, entityId, body);
        LOGGER.info(RESULT_ACCEPTED);
        return ResponseEntity.accepted().body(EMPTY_STR);
    }

    @Override
    public ResponseEntity<String> delete(String uriPath, String body) throws URISyntaxException, IOException {
        LOGGER.info("DELETE " + uriPath + " \n" + body);
        String[] paths = getPathsWithSlash(uriPath);
        String entityPath = getEntityPath(paths);
        if ("farm".equals(entityPath)) {
            deleteAll();
            LOGGER.info(RESULT_ACCEPTED);
            return ResponseEntity.accepted().body(EMPTY_STR);
        }
        String entityId = getEntityId(paths);
        if (!EMPTY_STR.equals(entityId)) {
            if (body == null || EMPTY_STR.equals(body)) {
                LOGGER.info(RESULT_BAD_REQUEST);
                return ResponseEntity.badRequest().body(EMPTY_STR);
            }
        }
        removeFromMap(entityPath, entityId, body);
        LOGGER.info(RESULT_ACCEPTED);
        return ResponseEntity.accepted().body(EMPTY_STR);
    }

    @Override
    public boolean isStatusCodeEqualOrLessThan(ResponseEntity<String> response, int status) {
        return response.getStatusCode().value() <= status;
    }

    public void deleteAll() {
        Constants.ENTITY_CLASSES.stream()
                                .map(c -> c.getSimpleName().toLowerCase())
                                .forEach(e -> mapOfmaps.get(e).clear());
    }

    private String getEntityPath(final String[] paths) {
        return paths.length > 1 ? paths[1] : "UNDEF";
    }

    private String getEntityId(final String[] paths) {
        return paths.length > 2 ? paths[2] : EMPTY_STR;
    }

    private String[] getPathsWithSlash(String uriPath) throws URISyntaxException {
        URI uri = new URI(uriPath);
        return uri.getPath().split("/");
    }

    private Predicate<? super Map.Entry<String, String>> hasIdAndExistOrNotHasId(String entityId) {
        return entry -> (!EMPTY_STR.equals(entityId) && entry.getKey().startsWith(entityId + Entity.SEP_COMPOUND_ID)) ||
                (EMPTY_STR.equals(entityId));
    }

    private String getArrayOfEntities(String entityPath, String entityId) {
        String result = "[]";
        ConcurrentHashMap<String, String> map = mapOfmaps.get(entityPath);
        if (map != null) {
            result = "[" + map.entrySet().stream()
                    .filter(hasIdAndExistOrNotHasId(entityId))
                    .map(Map.Entry::getValue)
                    .collect(Collectors.joining(",")) + "]";
        }
        return result;
    }

    private void putIfAbsentToMap(String entityPath, String body) {
        ConcurrentHashMap<String, String> map = mapOfmaps.get(entityPath);
        if (map != null) {
            try {
                JsonNode jsonNode = mapper.readTree(body);
                String id = jsonNode.get(FIELD_ID).asText();
                JsonNode parentIdObj = jsonNode.get(FIELD_PARENT_ID);
                String parentId = EMPTY_STR;
                if (parentIdObj != null) {
                    parentId = parentIdObj.asText(EMPTY_STR);
                }
                Entity entity = (Entity) JsonObject.fromJson(body, Entity.class);
                entity.setEntityType(entityPath);
                body = JsonObject.toJsonString(entity);
                map.putIfAbsent(id + Entity.SEP_COMPOUND_ID + parentId, body);
            } catch (IOException e) {
                LOGGER.error(ExceptionUtils.getStackTrace(e));
            }
        }
    }

    private void replaceIntoMap(String entityPath, String entityId, String body) {
        ConcurrentHashMap<String, String> map = mapOfmaps.get(entityPath);
        if (map != null) {
            try {
                JsonNode jsonNode = mapper.readTree(body);
                String id = jsonNode.get(FIELD_ID).asText();
                if (entityId.equals(id)) {
                    JsonNode parentIdObj = jsonNode.get(FIELD_PARENT_ID);
                    String parentId = EMPTY_STR;
                    if (parentIdObj != null) {
                        parentId = parentIdObj.asText(EMPTY_STR);
                    }
                    Entity entity = (Entity) JsonObject.fromJson(body, Entity.class);
                    entity.setEntityType(entityPath);
                    body = JsonObject.toJsonString(entity);
                    map.replace(id + Entity.SEP_COMPOUND_ID + parentId, body);
                }
            } catch (IOException e) {
                LOGGER.error(ExceptionUtils.getStackTrace(e));
            }
        }
    }

    private void removeFromMap(String entityPath, String entityId, String body) {
        ConcurrentHashMap<String, String> map = mapOfmaps.get(entityPath);
        if (map != null) {
            if (EMPTY_STR.equals(entityId)) {
                map.clear();
            } else {
                try {
                    JsonNode jsonNode = mapper.readTree(body);
                    String id = jsonNode.get(FIELD_ID).asText();
                    if (entityId.equals(id)) {
                        JsonNode parentIdObj = jsonNode.get(FIELD_PARENT_ID);
                        String parentId = EMPTY_STR;
                        if (parentIdObj != null) {
                            parentId = parentIdObj.asText(EMPTY_STR);
                        }
                        map.remove(id + Entity.SEP_COMPOUND_ID + parentId);
                    }
                } catch (IOException e) {
                    LOGGER.error(ExceptionUtils.getStackTrace(e));
                }
            }
        }
    }

}
