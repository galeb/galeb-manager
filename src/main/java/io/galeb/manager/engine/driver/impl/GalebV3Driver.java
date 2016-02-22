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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.*;
import java.util.stream.Collectors;

import io.galeb.core.util.*;
import io.galeb.manager.common.LoggerUtils;
import io.galeb.manager.engine.driver.Driver;
import io.galeb.manager.engine.util.DiffProcessor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.boot.logging.LogLevel;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.galeb.manager.common.Properties;

import static io.galeb.manager.engine.util.CounterDownLatch.decrementDiffCounter;

public class GalebV3Driver implements Driver {

    public static final String DRIVER_NAME = GalebV3Driver.class.getSimpleName()
                                                                .replaceAll("Driver", "");

    private static final Log LOGGER = LogFactory.getLog(GalebV3Driver.class);

    private static final String NO_DECREMENT = "NO_DECREMENT";

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
        String path = properties.getOrDefault("path", "").toString() + "/" +getIdEncoded(json);
        String uriPath = api + "/" + path;

        JsonNode jsonNode = null;
        try {
            jsonNode = mapper.readTree(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
        JsonNode parentIdObj = jsonNode != null ? jsonNode.get("parentId") : null;
        RestTemplate restTemplate = new RestTemplate();

        boolean result = false;

        try {
            URI uri = new URI(uriPath);
            RequestEntity<Void> request = RequestEntity.get(uri).build();
            ResponseEntity<String> response = restTemplate.exchange(request, String.class);
            result = getResultFromStatusCode(request, response);
            if (parentIdObj != null) {
                result = result && getResultFromParent(response, parentIdObj.asText());
            }
        } catch (RuntimeException|URISyntaxException e) {
            LOGGER.error("POST "+uriPath+" ("+e.getMessage()+")");
        }

        return result;
    }

    private boolean getResultFromParent(ResponseEntity<String> response, String expectedParent) {
        AtomicBoolean parentFound = new AtomicBoolean(false);

        try {
            JsonNode json = mapper.readTree(response.getBody());
            if (json.isArray()) {
                json.forEach(jsonNode -> {
                    JsonNode parentIdObj = jsonNode.get("parentId");
                    if (parentIdObj != null && parentIdObj.isTextual() && parentIdObj.asText().equals(expectedParent)) {
                        parentFound.set(true);
                    }
                });
            }
        } catch (IOException e) {
            LOGGER.error(e);
            return false;
        }

        return parentFound.get();
    }

    private void removeBeforeCreate(final Properties properties) {
        properties.put(NO_DECREMENT, true);
        remove(properties);
        properties.remove(NO_DECREMENT);
    }

    @Override
    public boolean create(Properties properties) {
        removeBeforeCreate(properties);
        String api = properties.getOrDefault("api", "NULL").toString();
        String keyInProgress = api;
        api = !api.startsWith("http") ? "http://" + api : api;
        String json = properties.getOrDefault("json", "{}").toString();
        String path = properties.getOrDefault("path", "").toString();
        String uriPath = api + "/" + path;
        RestTemplate restTemplate = new RestTemplate();
        boolean result = false;

        try {
            URI uri = new URI(uriPath);
            RequestEntity<String> request = RequestEntity.post(uri).contentType(MediaType.APPLICATION_JSON).body(json);
            ResponseEntity<String> response = restTemplate.exchange(request, String.class);
            result = getResultFromStatusCode(request, response);
        } catch (RuntimeException|URISyntaxException e) {
            LOGGER.error("POST "+uriPath+" ("+e.getMessage()+")");
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
        RestTemplate restTemplate = new RestTemplate();
        boolean result = false;

        try {
            URI uri = new URI(uriPath);
            RequestEntity<String> request = RequestEntity.put(uri).contentType(MediaType.APPLICATION_JSON).body(json);
            ResponseEntity<String> response = restTemplate.exchange(request, String.class);
            result = getResultFromStatusCode(request, response);
        } catch (RuntimeException|URISyntaxException e) {
            LOGGER.error("PUT "+uriPath+" ("+e.getMessage()+")");
        } finally {
            decrementDiffCounter(keyInProgress);
        }
        return result;
    }

    @Override
    public boolean remove(Properties properties) {
        boolean result = false;
        Boolean noDecrement = (Boolean) properties.getOrDefault(NO_DECREMENT, false);
        String api = properties.getOrDefault("api", "NULL").toString();
        String keyInProgress = api;
        api = !api.startsWith("http") ? "http://" + api : api;
        String json = properties.getOrDefault("json", "{}").toString();
        String path = properties.getOrDefault("path", "").toString();
        String id = getIdEncoded(json);
        path = !"".equals(id) ? path + "/" + id : path;
        String uriPath = api + "/" + path;
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        HttpEntityEnclosingRequest delete = new HttpDeleteWithBody("/"+path);

        try {
            delete.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
            api = api.startsWith("http") ? api.replaceAll("http.?://", "") : api;
            String[] apiWithPort = api.split(":");
            String hostName = apiWithPort[0];
            int port =  apiWithPort.length > 1 ? Integer.valueOf(apiWithPort[1]) : 80;
            delete.setEntity(new StringEntity(!"".equals(id) ? json : "{\"id\":\"\",\"version\":0}"));
            HttpResponse response = httpClient.execute(new HttpHost(hostName, port), delete);
            httpClient.close();
            result = getResultFromStatusCode(delete, response);
        } catch (Exception e) {
            LOGGER.error("DELETE "+uriPath+" ("+e.getMessage()+")");
        } finally {
            if (!noDecrement) {
                decrementDiffCounter(keyInProgress);
            }
        }
        return result;
    }

    @NotThreadSafe
    private static class HttpDeleteWithBody extends HttpEntityEnclosingRequestBase {

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
        if (json == null) {
            return "";
        }
        try {
            JsonNode idObj = mapper.readTree(json).get("id");
            if (idObj == null) {
                return "";
            }
            String id = idObj.asText();
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
    public Map<String, Map<String, Object>> diff(Properties properties,
                 Map<String,Map<String,Map<String,String>>> getAll) throws Exception {
        return new DiffProcessor().setProperties(properties).getDiffMap(getAll);
    }

    @Override
    public Map<String,Map<String,Map<String,String>>> getAll(Properties properties) throws Exception {
        Map<String,Map<String,Map<String,String>>> remoteMultiMap = new HashMap<>();
        AtomicReference<Exception> exception = new AtomicReference<>(null);
        String apiFromProperties = properties.getOrDefault("api", "NULL").toString();
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

    private boolean getResultFromStatusCode(HttpEntityEnclosingRequest request, HttpResponse response) {
        InputStream content = null;
        try {
            content = request.getEntity().getContent();
        } catch (IOException e) {
            LOGGER.error(e);
        }
        String body = inputStreamToString(content);

        MultiValueMap<String, String> headers = new org.springframework.http.HttpHeaders();
        Map<String, List<String>> newMapOfHeaders =
                Arrays.asList(request.getAllHeaders()).stream().collect(
                        Collectors.toMap(Header::getName, header -> Arrays.asList(header.getValue().split(","))));
        headers.putAll(newMapOfHeaders);
        HttpMethod httpMethod = EnumSet.allOf(HttpMethod.class).stream()
                .filter(method -> method.toString().equals(request.getRequestLine().getMethod())).findFirst().get();

        RequestEntity<String> newRequest = new RequestEntity<>(body, headers, httpMethod, URI.create(request.getRequestLine().getUri()));
        InputStream responseContent = null;
        try {
            responseContent = response.getEntity().getContent();
        } catch (IOException e) {
            LOGGER.error(e);
        }
        String responseBody = inputStreamToString(responseContent);

        MultiValueMap<String, String> responseHeaders = new org.springframework.http.HttpHeaders();
        Map<String, List<String>> newResponseMapOfHeaders =
                Arrays.asList(request.getAllHeaders()).stream().collect(
                        Collectors.toMap(Header::getName, header -> Arrays.asList(header.getValue().split(","))));
        responseHeaders.putAll(newResponseMapOfHeaders);
        HttpStatus responseStatusCode = EnumSet.allOf(HttpStatus.class).stream()
                .filter(status -> status.value() == response.getStatusLine().getStatusCode()).findFirst().get();

        ResponseEntity<String> newResponse = new ResponseEntity<>(responseBody,
                responseHeaders,
                responseStatusCode);

        return getResultFromStatusCode(newRequest, newResponse);
    }

    private String inputStreamToString(InputStream content) {
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = null;
        String line;
        try {
            if (content != null) {
                bufferedReader = new BufferedReader(new InputStreamReader(content, StandardCharsets.UTF_8.toString()));
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line);
                }
            } else {
                LOGGER.warn("Content is null.");
            }
        } catch (IOException e) {
            LOGGER.error(e);
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    LOGGER.error(e);
                }
            }
        }
        return stringBuilder.toString();
    }

    private boolean getResultFromStatusCode(RequestEntity<?> request, ResponseEntity<String> response) {
        if (response.getStatusCode().value() < 400) {
            logRequestResponse(request, response, LogLevel.INFO);
            return true;
        } else {
            logRequestResponse(request, response, LogLevel.ERROR);
            return false;
        }
    }

    private void logRequestResponse(RequestEntity<?> request, ResponseEntity<String> response, LogLevel logLevel) {
        HttpStatus statusCode = response.getStatusCode();
        String status = "HTTP/1.? " + statusCode.value()+" " + statusCode.getReasonPhrase();

        LoggerUtils.logger(LOGGER, logLevel, request.getMethod().toString() + " " + request.getUrl().toString());
        request.getHeaders().entrySet().forEach(entry ->
                LoggerUtils.logger(LOGGER, logLevel, entry.getKey() + ": " + entry.getValue().stream().collect(Collectors.joining(","))));
        Object requestBody = request.getBody();
        if (requestBody instanceof String) {
            LoggerUtils.logger(LOGGER, logLevel, requestBody);
        }
        LoggerUtils.logger(LOGGER, logLevel, "---");
        LoggerUtils.logger(LOGGER, logLevel, status);
        response.getHeaders().entrySet().forEach(entry ->
                LoggerUtils.logger(LOGGER, logLevel, entry.getKey() + ": " + entry.getValue().stream().collect(Collectors.joining(","))));
        String responseBody = response.getBody();
        if (responseBody != null) {
            LoggerUtils.logger(LOGGER, logLevel, response.getBody());
        }
    }

    private Map<String, Map<String, String>> extractRemoteMap(String path, String api) throws Exception {
        final Map<String, Map<String, String>> fullMap = new HashMap<>();
        String fullPath = api + "/" + path;

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
                entityProperties.put("_entity_type", entityType);
                entityProperties.put("_etag", etag);
                fullMap.put(fullPath + "/" + id + "@" + parentId, entityProperties);
            });
        }

        return fullMap;
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
