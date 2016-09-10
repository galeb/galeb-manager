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
import io.galeb.core.model.Entity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class FakeHttpClient implements CommonHttpRequester {

    private final Map<String, ConcurrentHashMap<String, String>> mapOfmaps = new HashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();

    public FakeHttpClient() {
        mapOfmaps.put("virtualhost", new ConcurrentHashMap<>());
        mapOfmaps.put("rule", new ConcurrentHashMap<>());
        mapOfmaps.put("backendpool", new ConcurrentHashMap<>());
        mapOfmaps.put("backend", new ConcurrentHashMap<>());
    }

    @Override
    public ResponseEntity<String> get(String uriPath) throws URISyntaxException {
        URI uri = new URI(uriPath);
        String result = null;
        String[] paths = uri.getPath().split("/");
        String entityPath = paths.length > 1 ? paths[1] : "UNDEF";
        String entityId = paths.length > 2 ? paths[2] : "";
        ConcurrentHashMap<String, String> map = mapOfmaps.get(entityPath);
        if (map != null) {
            result = "[" + map.entrySet().stream()
                    .filter(entry -> entry.getKey().startsWith(entityId + Entity.SEP_COMPOUND_ID))
                    .map(Map.Entry::getValue)
                    .collect(Collectors.joining(",")) + "]";
        }
        if (result == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("[]");
        } else {
            return ResponseEntity.ok(result);
        }
    }

    @Override
    public ResponseEntity<String> post(String uriPath, String body) throws URISyntaxException {
        URI uri = new URI(uriPath);
        String[] paths = uri.getPath().split("/");
        String entityPath = paths.length > 1 ? paths[1] : "UNDEF";
        ConcurrentHashMap<String, String> map = mapOfmaps.get(entityPath);
        if (map != null) {
            try {
                JsonNode jsonNode = mapper.readTree(body);
                String id = jsonNode.get("id").asText();
                String parentId = jsonNode.get("parentId").asText("");
                map.putIfAbsent(id + Entity.SEP_COMPOUND_ID + parentId, body);
            } catch (IOException e) {
                // ignore
            }
        }
        return ResponseEntity.accepted().body("");
    }

    @Override
    public ResponseEntity<String> put(String uriPath, String body) throws URISyntaxException {
        URI uri = new URI(uriPath);
        String[] paths = uri.getPath().split("/");
        String entityPath = paths.length > 1 ? paths[1] : "UNDEF";
        String entityId = paths.length > 2 ? paths[2] : "";
        ConcurrentHashMap<String, String> map = mapOfmaps.get(entityPath);
        if (map != null) {
            try {
                JsonNode jsonNode = mapper.readTree(body);
                String id = jsonNode.get("id").asText();
                if (entityId.equals(id)) {
                    String parentId = jsonNode.get("parentId").asText("");
                    map.replace(id + Entity.SEP_COMPOUND_ID + parentId, body);
                }
            } catch (IOException e) {
                // ignore
            }
        }
        return ResponseEntity.accepted().body("");
    }

    @Override
    public ResponseEntity<String> delete(String uriPath, String body) throws URISyntaxException, IOException {
        URI uri = new URI(uriPath);
        String[] paths = uri.getPath().split("/");
        String entityPath = paths.length > 1 ? paths[1] : "UNDEF";
        String entityId = paths.length > 2 ? paths[2] : "";
        ConcurrentHashMap<String, String> map = mapOfmaps.get(entityPath);
        if (map != null) {
            try {
                JsonNode jsonNode = mapper.readTree(body);
                String id = jsonNode.get("id").asText();
                if (entityId.equals(id)) {
                    String parentId = jsonNode.get("parentId").asText("");
                    map.remove(id + Entity.SEP_COMPOUND_ID + parentId, body);
                }
            } catch (IOException e) {
                // ignore
            }
        }
        return ResponseEntity.accepted().body("");
    }

    @Override
    public boolean isStatusCodeEqualOrLessThan(ResponseEntity<String> response, int status) {
        return response.getStatusCode().value() <= status;
    }
}
