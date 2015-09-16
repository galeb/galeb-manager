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

package io.galeb.manager.engine.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.galeb.core.model.Entity;
import io.galeb.manager.common.EmptyStream;
import io.galeb.manager.common.Properties;
import io.galeb.manager.engine.Driver;
import io.galeb.manager.entity.AbstractEntity;
import io.galeb.manager.entity.WithParent;
import io.galeb.manager.entity.WithParents;

public class GalebV3Driver implements Driver {

    public static final String DRIVER_NAME = GalebV3Driver.class.getSimpleName()
                                                                .replaceAll("Driver", "");

    private static final Log LOGGER = LogFactory.getLog(GalebV3Driver.class);

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String toString() {
        return DRIVER_NAME;
    }

    @Override
    public boolean create(Properties properties) {
        String api = properties.getOrDefault("api", "NULL").toString();
        String json = properties.getOrDefault("json", "{}").toString();
        String path = properties.getOrDefault("path", "").toString();
        String uriPath = "http://" + api + "/" + path;
        RestTemplate restTemplate = new RestTemplate();
        boolean result = false;

        try {
            URI uri = new URI(uriPath);
            RequestEntity<String> request = RequestEntity.post(uri).contentType(MediaType.APPLICATION_JSON).body(json);
            ResponseEntity<String> response = restTemplate.exchange(request, String.class);
            result = response.getStatusCode().value() < 400;
        } catch (RuntimeException|URISyntaxException e) {
            LOGGER.error("POST "+uriPath+" ("+e.getMessage()+")");
        }
        return result;
    }

    @Override
    public boolean update(Properties properties) {
        String api = properties.getOrDefault("api", "NULL").toString();
        String json = properties.getOrDefault("json", "{}").toString();
        String path = properties.getOrDefault("path", "").toString() + "/" +getIdEncoded(json);
        String uriPath = "http://" + api + "/" + path;
        RestTemplate restTemplate = new RestTemplate();
        boolean result = false;

        try {
            URI uri = new URI(uriPath);
            RequestEntity<String> request = RequestEntity.put(uri).contentType(MediaType.APPLICATION_JSON).body(json);
            ResponseEntity<String> response = restTemplate.exchange(request, String.class);
            result = response.getStatusCode().value() < 400;
        } catch (RuntimeException|URISyntaxException e) {
            LOGGER.error("PUT "+uriPath+" ("+e.getMessage()+")");
        }
        return result;
    }

    @Override
    public boolean remove(Properties properties) {
        boolean result = false;
        String api = properties.getOrDefault("api", "NULL").toString();
        String json = properties.getOrDefault("json", "{}").toString();
        String path = properties.getOrDefault("path", "").toString() + "/" +getIdEncoded(json);
        String uriPath = "http://" + api + "/" + path;
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        HttpEntityEnclosingRequest delete = new HttpDeleteWithBody("/"+path);

        try {
            delete.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
            String[] apiWithPort = api.split(":");
            String hostName = apiWithPort[0];
            int port =  apiWithPort.length > 1 ? Integer.valueOf(apiWithPort[1]) : 80;
            delete.setEntity(new StringEntity(json));
            HttpResponse response = httpClient.execute(new HttpHost(hostName, port), delete);
            httpClient.close();
            result = response.getStatusLine().getStatusCode() < 400;
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.error("DELETE "+uriPath+" ("+e.getMessage()+")");
        }
        return result;
    }

    @Override
    public boolean reload(Properties properties) throws IOException {
        boolean result = false;
        String api = properties.getOrDefault("api", "NULL").toString();
        String[] apiWithPort = api.split(":");
        String hostName = apiWithPort[0];
        int port =  apiWithPort.length > 1 ? Integer.valueOf(apiWithPort[1]) : 80;
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();

        HttpDelete delete = new HttpDelete("/farm");
        delete.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
        HttpResponse response = httpClient.execute(new HttpHost(hostName, port), delete);

        result = response.getStatusLine().getStatusCode() < 400;
        httpClient.close();

        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public StatusFarm status(Properties properties) {
        String api = properties.getOrDefault("api", "localhost:9090").toString();
        String path = properties.getOrDefault("path", "").toString();
        String name = properties.getOrDefault("name", "UNDEF").toString();
        Stream<? extends AbstractEntity<?>> parents =
                (Stream<? extends AbstractEntity<?>>) properties.getOrDefault("parents", EmptyStream.get());
        int expectedId = properties.getOrDefault("id", -1);
        long expectedNumElements = properties.getOrDefault("numElements", -1L);

        String basePath = "http://" + api + "/" + path;
        String nameEncoded = name;
        try {
            nameEncoded = URLEncoder.encode(name, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e1) {
            LOGGER.error(e1);
            return StatusFarm.FAIL;
        }
        String fullPath = basePath+"/"+nameEncoded;

        AtomicBoolean result = new AtomicBoolean(true);
        try {
            if (!isOkNumElements(basePath, expectedNumElements)) {
                return StatusFarm.FAIL;
            }
            List<AbstractEntity<?>> listOfParents = parents.collect(Collectors.toList());
            if (listOfParents.size()>0) {
                listOfParents.forEach(parent -> {
                    try {
                        if (result.get()) {
                            boolean isSyncronized = isSyncronized(fullPath, name, parent.getName(), expectedId);
                            result.set(isSyncronized);
                        }
                    } catch (Exception e) {
                        result.set(false);
                        LOGGER.error("STATUS FAIL: " + fullPath);
                        LOGGER.error(e);
                    }
                });
            } else {
                result.set(isSyncronized(fullPath, name, null, expectedId));
            }

            if (!result.get()) {
                LOGGER.warn("STATUS FAIL: "+fullPath);
            } else {
                LOGGER.debug("STATUS OK: "+fullPath);
            }
        } catch (RuntimeException | IOException | URISyntaxException e) {
            result.set(false);
            LOGGER.error("STATUS FAIL: "+fullPath);
            LOGGER.error(e);
        }
        return result.get() ? StatusFarm.OK : StatusFarm.FAIL;
    }

    private JsonNode getJson(String path) throws URISyntaxException, IOException, JsonProcessingException {
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

    private boolean isSyncronized(String fullPath, String name, String parent, int expectedId) throws URISyntaxException, JsonProcessingException, IOException {
        JsonNode json = getJson(fullPath);
        if (json == null) {
            return false;
        }

        boolean syncronized = false;
        final Entity entity = new Entity();

        entity.setVersion(-1);
        if (json.isArray()) {
            StreamSupport.stream(json.spliterator(), false)
                .filter(element -> element.isObject() &&
                        element.get("id") != null &&
                        element.get("id").asText("defaultTextIfAbsent").equals(name))
                .forEach(element -> {
                    if ((parent == null) ||
                                (!(parent != null) &&
                                 element.get("parentId") != null &&
                                 element.get("parentId").asText("defaultTextIfAbsent").equals(parent))) {

                        entity.setVersion(element.get("version").asInt(-1));
                    } else {
                        LOGGER.debug("CHECK FAIL >>>>  "+fullPath+" : "+" parent="+parent+" expectedId="+expectedId);
                        LOGGER.debug("           >>>>  "+fullPath+" : "+" parent(remote)="+element.get("parentId").asText()+" id(remote)="+element.get("version").asInt());
                    }
            });
        }
        syncronized = expectedId == entity.getVersion();
        if (!syncronized) {
            LOGGER.error(fullPath+" : VERSION NOT MATCH (manager:"+expectedId+" != farm:"+entity.getVersion()+")");
        }

        return syncronized;
    }

    private boolean isOkNumElements(String pathBase, long expectedNumElements) throws URISyntaxException, JsonProcessingException, IOException {
        JsonNode json = getJson(pathBase);
        if (json == null) {
            return false;
        }

        boolean resultCount = false;
        int numElements = 0;

        if (json.isArray()) {
            numElements = json.size();
        }
        resultCount = expectedNumElements == numElements;
        if (!resultCount) {
            LOGGER.error(pathBase+" : COUNT NOT MATCH (manager:"+expectedNumElements+" != farm:"+numElements+")");
        }

        return resultCount;
    }

    @NotThreadSafe
    private class HttpDeleteWithBody extends HttpEntityEnclosingRequestBase {

        @Override
        public String getMethod() {
            return "DELETE";
        }

        public HttpDeleteWithBody(String uri) {
            super();
            setURI(URI.create(uri));
        }
    }

    private String getIdEncoded(String json) {
        try {
            String id = new ObjectMapper().readTree(json).get("id").asText();
            if (id!=null) {
                id = URLEncoder.encode(id, StandardCharsets.UTF_8.toString());
            } else {
                id = "";
            }
            return id;
        } catch (IOException e) {
            return "";
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Properties> diff(Properties properties) {

        final String api = properties.getOrDefault("api", "localhost:9090").toString();
        final Set<AbstractEntity<?>> virtualhosts = (Set<AbstractEntity<?>>)
                            properties.getOrDefault("virtualhosts", Collections.emptySet());
        final Set<AbstractEntity<?>> backendpools = (Set<AbstractEntity<?>>)
                properties.getOrDefault("backendpools", Collections.emptySet());
        final Set<AbstractEntity<?>> backends = (Set<AbstractEntity<?>>)
                properties.getOrDefault("backends", Collections.emptySet());
        final Set<AbstractEntity<?>> rules = (Set<AbstractEntity<?>>)
                properties.getOrDefault("rules", Collections.emptySet());

        final Map<String, Set<AbstractEntity<?>>> entitiesMap = new HashMap<>();
        entitiesMap.put("virtualhost", virtualhosts);
        entitiesMap.put("backendpool", backendpools);
        entitiesMap.put("backend", backends);
        entitiesMap.put("rule", rules);

        final Map<String, Properties> fullMap = extractRemoteMap(api, properties);
        final Map<String, Properties> diffMap = makeDiffMap(api, entitiesMap, fullMap);

        return diffMap;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Properties> makeDiffMap(final String api,
                                                final Map<String, Set<AbstractEntity<?>>> entitiesMap,
                                                final Map<String, Properties> fullMap) {

        final Map<String, Properties> diffMap = new HashMap<>();
        final List<String> pathList = Arrays.asList("virtualhost","backendpool","backend","rule");

        pathList.stream().forEach(path ->
        {
            Set<AbstractEntity<?>> entities = entitiesMap.get(path);

            fullMap.entrySet().stream()
                              .filter(entry ->
                                  entry.getValue().getOrDefault("entity_type", "UNDEF").equals(path))
                              .forEach(entry ->
            {
                final String key = entry.getKey();
                final Properties entityProperties = entry.getValue();
                final String id = entityProperties.getOrDefault("id", "UNDEF").toString();
                final String parentId = entityProperties.getOrDefault("parentId", "UNDEF").toString();
                final String version = entityProperties.getOrDefault("version", "UNDEF").toString();
                final String pk = entityProperties.getOrDefault("pk", "UNDEF").toString();
                AtomicBoolean hasId = new AtomicBoolean(false);

                entities.stream().filter(entity -> entity.getName().equals(id))
                                 .filter(entity -> (!(entity instanceof WithParent) && !(entity instanceof WithParents)) ||
                                                   (entity instanceof WithParent) &&
                                                        ((WithParent<AbstractEntity<?>>) entity).getParent() != null &&
                                                        ((WithParent<AbstractEntity<?>>) entity).getParent().getName().equals(parentId) ||
                                                   (entity instanceof WithParents) &&
                                                        !((WithParents<AbstractEntity<?>>) entity).getParents().isEmpty() &&
                                                        ((WithParents<AbstractEntity<?>>) entity).getParents().stream()
                                                            .map(AbstractEntity::getName).collect(Collectors.toList()).contains(parentId))
                                 .forEach(entity ->
                {
                    hasId.set(true);
                    if (!version.equals(String.valueOf(entity.getId())) || !pk.equals(String.valueOf(entity.getId()))) {
                        changeAction(key, entityProperties, diffMap);
                    }
                });

                if (!hasId.get()) {
                    delAction(key, entityProperties, diffMap);
                }
            });

            entities.stream().forEach(entity -> {
                String id = entity.getName();
                if (!(entity instanceof WithParent) && !(entity instanceof WithParents)) {
                    addAction(api, path, id, "", fullMap, diffMap);
                }
                if (entity instanceof WithParent) {
                    String parentId = ((WithParent<AbstractEntity<?>>) entity).getParent().getName();
                    addAction(api, path, id, parentId, fullMap, diffMap);
                }
                if (entity instanceof WithParents) {
                    ((WithParents<AbstractEntity<?>>) entity).getParents().forEach(aParent ->
                    {
                        String parentId = aParent.getName();
                        addAction(api, path, id, parentId, fullMap, diffMap);
                    });
                }
            });
        });
        return diffMap;
    }

    private Map<String, Properties> extractRemoteMap(final String api,
            Properties properties) {
        final Map<String, Properties> fullMap = new HashMap<>();
        final List<String> pathList = Arrays.asList("virtualhost","backendpool","backend","rule");

        pathList.stream().map(path -> api + "/" + path).forEach(fullPath ->
        {
            try {
                JsonNode json = getJson(fullPath);
                if (json.isArray()) {
                    json.forEach(element -> {
                        Properties entityProperties = new Properties();
                        String id = element.get("id").asText();
                        JsonNode parentIdObj = element.get("parentId");
                        String parentId = parentIdObj != null ? parentIdObj.asText() : "";
                        String pk = element.get("pk").asText();
                        String version = element.get("version").asText();
                        String entityType = element.get("_entity_type").asText();
                        String etag = element.get("_etag").asText();

                        properties.put("pk", pk);
                        properties.put("version", version);
                        properties.put("entity_type", entityType);
                        properties.put("etag", etag);
                        fullMap.put(fullPath + "/" + id + "@" + parentId, entityProperties);
                    });
                }
            } catch (Exception e) {
                LOGGER.error(e);
            }
        });
        return fullMap;
    }

    private void addAction(final String api,
                           final String path,
                           final String id,
                           final String parentId,
                           final Map<String, Properties> fullMap,
                           final Map<String, Properties> diffMap) {
        String key = api + "/" + path + "/" + id + "@" + parentId;
        if (!fullMap.containsKey(key)) {
            Properties entityProperties = new Properties();
            entityProperties.put("action", "ADD");
            diffMap.put(key, entityProperties);
        }
    }

    private void changeAction(final String key,
                              final Properties entityProperties,
                              final Map<String, Properties> diffMap) {
        entityProperties.put("action", "CHANGE");
        diffMap.put(key, entityProperties);
    }

    private void delAction(final String key,
                           final Properties entityProperties,
                           final Map<String, Properties> diffMap) {
        entityProperties.put("action", "DELETE");
        diffMap.put(key, entityProperties);
    }

}
