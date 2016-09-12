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
import java.util.stream.Collectors;

public class FakeFarmClient implements CommonHttpRequester {

    private static final Log LOGGER = LogFactory.getLog(FakeFarmClient.class);

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
        URI uri = new URI(uriPath);
        String result = null;
        String[] paths = uri.getPath().split("/");
        String entityPath = paths.length > 1 ? paths[1] : "UNDEF";
        if ("farm".equals(entityPath)) {
            String response = "{ \"info\" : \"'GET /farm' was removed\" }";
            LOGGER.info("Result: OK \n" + response);
            return ResponseEntity.ok(response);
        }
        String entityId = paths.length > 2 ? paths[2] : "";
        ConcurrentHashMap<String, String> map = mapOfmaps.get(entityPath);
        if (map != null) {
            result = "[" + map.entrySet().stream()
                    .filter(entry -> (!"".equals(entityId) && entry.getKey().startsWith(entityId + Entity.SEP_COMPOUND_ID)) || ("".equals(entityId)))
                    .map(Map.Entry::getValue)
                    .collect(Collectors.joining(",")) + "]";
        }
        if (result == null || "[]".equals(result)) {
            LOGGER.info("Result: NOT FOUND");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("");
        } else {
            LOGGER.info("Result: OK \n" + result);
            return ResponseEntity.ok(result);
        }
    }

    @Override
    public ResponseEntity<String> post(String uriPath, String body) throws URISyntaxException {
        LOGGER.info("POST " + uriPath + " \n" + body);
        if (body == null || "".equals(body)) {
            LOGGER.info("Result: BAD REQUEST");
            return ResponseEntity.badRequest().body("");
        }
        URI uri = new URI(uriPath);
        String[] paths = uri.getPath().split("/");
        String entityPath = paths.length > 1 ? paths[1] : "UNDEF";
        if ("farm".equals(entityPath)) {
            LOGGER.info("Result: BAD REQUEST");
            return ResponseEntity.badRequest().body("");
        }
        ConcurrentHashMap<String, String> map = mapOfmaps.get(entityPath);
        if (map != null) {
            try {
                JsonNode jsonNode = mapper.readTree(body);
                String id = jsonNode.get("id").asText();
                JsonNode parentIdObj = jsonNode.get("parentId");
                String parentId = "";
                if (parentIdObj != null) {
                    parentId = parentIdObj.asText("");
                }
                Entity entity = (Entity) JsonObject.fromJson(body, Entity.class);
                entity.setEntityType(entityPath);
                body = JsonObject.toJsonString(entity);
                map.putIfAbsent(id + Entity.SEP_COMPOUND_ID + parentId, body);
            } catch (IOException e) {
                LOGGER.error(ExceptionUtils.getStackTrace(e));
            }
        }
        LOGGER.info("Result: ACCEPTED");
        return ResponseEntity.accepted().body("");
    }

    @Override
    public ResponseEntity<String> put(String uriPath, String body) throws URISyntaxException {
        LOGGER.info("PUT " + uriPath + " \n" + body);
        if (body == null || "".equals(body)) {
            LOGGER.info("Result: BAD REQUEST");
            return ResponseEntity.badRequest().body("");
        }
        URI uri = new URI(uriPath);
        String[] paths = uri.getPath().split("/");
        String entityPath = paths.length > 1 ? paths[1] : "UNDEF";
        if ("farm".equals(entityPath)) {
            // TODO: Galeb.API ignore update Farm.
            LOGGER.info("Result: BAD REQUEST");
            return ResponseEntity.badRequest().body("");
        }
        String entityId = paths.length > 2 ? paths[2] : "";
        ConcurrentHashMap<String, String> map = mapOfmaps.get(entityPath);
        if (map != null) {
            try {
                JsonNode jsonNode = mapper.readTree(body);
                String id = jsonNode.get("id").asText();
                if (entityId.equals(id)) {
                    JsonNode parentIdObj = jsonNode.get("parentId");
                    String parentId = "";
                    if (parentIdObj != null) {
                        parentId = parentIdObj.asText("");
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
        LOGGER.info("Result: ACCEPTED");
        return ResponseEntity.accepted().body("");
    }

    @Override
    public ResponseEntity<String> delete(String uriPath, String body) throws URISyntaxException, IOException {
        LOGGER.info("DELETE " + uriPath + " \n" + body);
        URI uri = new URI(uriPath);
        String[] paths = uri.getPath().split("/");
        String entityPath = paths.length > 1 ? paths[1] : "UNDEF";
        if ("farm".equals(entityPath)) {
            deleteAll();
            LOGGER.info("Result: ACCEPTED");
            return ResponseEntity.accepted().body("");
        }
        String entityId = paths.length > 2 ? paths[2] : "";
        if (!"".equals(entityId)) {
            if (body == null || "".equals(body)) {
                LOGGER.info("Result: BAD REQUEST");
                return ResponseEntity.badRequest().body("");
            }
        }
        ConcurrentHashMap<String, String> map = mapOfmaps.get(entityPath);
        if (map != null) {
            if ("".equals(entityId)) {
                map.clear();
            } else {
                try {
                    JsonNode jsonNode = mapper.readTree(body);
                    String id = jsonNode.get("id").asText();
                    if (entityId.equals(id)) {
                        JsonNode parentIdObj = jsonNode.get("parentId");
                        String parentId = "";
                        if (parentIdObj != null) {
                            parentId = parentIdObj.asText("");
                        }
                        map.remove(id + Entity.SEP_COMPOUND_ID + parentId);
                    }
                } catch (IOException e) {
                    LOGGER.error(ExceptionUtils.getStackTrace(e));
                }
            }
        }
        LOGGER.info("Result: ACCEPTED");
        return ResponseEntity.accepted().body("");
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
}
