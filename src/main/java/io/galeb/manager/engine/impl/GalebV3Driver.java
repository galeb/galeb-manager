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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.galeb.core.model.Entity;
import io.galeb.manager.common.Properties;
import io.galeb.manager.engine.Driver;
import io.galeb.manager.handler.VirtualHostHandler;

public class GalebV3Driver implements Driver {

    public static final String DRIVER_NAME = GalebV3Driver.class.getSimpleName()
                                                                .replaceAll("Driver", "");

    private static final Log LOGGER = LogFactory.getLog(VirtualHostHandler.class);

    private ObjectMapper mapper = new ObjectMapper();

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

    @Override
    public StatusFarm status(Properties properties) {
        String api = properties.getOrDefault("api", "NULL").toString();
        String path = properties.getOrDefault("path", "").toString();
        String name = properties.getOrDefault("name", "").toString();
        int expectedId = properties.getOrDefault("id", -1);
        long expectedNumElements = properties.getOrDefault("numElements", -1L);
        String uriPath = "http://" + api + "/" + path;
        String fullPath = uriPath+"/"+name;
        RestTemplate restTemplate = new RestTemplate();
        boolean result = false;

        try {
            URI uri = new URI(uriPath);
            RequestEntity<Void> request = RequestEntity.get(uri).build();
            ResponseEntity<String> response = restTemplate.exchange(request, String.class);
            result = response.getStatusCode().value() < 400;
            if (result) {
                JsonNode json = mapper.readTree(response.getBody());
                final Entity entity = new Entity();
                entity.setVersion(-1);

                int numElements = 0;
                if (json != null && json.isArray()) {
                    numElements = json.size();
                    json.forEach(element -> {
                        if (element != null &&
                                element.isObject() &&
                                element.get("id") != null &&
                                element.get("id").asText("defaultTextIfAbsent").equals(name)) {
                            entity.setVersion(element.get("version").asInt(-1));
                        }
                    });
                }
                int entityVersion = entity.getVersion();
                boolean resultVersion = expectedId == entityVersion;
                if (!resultVersion) {
                    LOGGER.error(fullPath+" : VERSION NOT MATCH (manager:"+expectedId+" != farm:"+entityVersion+")");
                }
                boolean resultCount = expectedNumElements == numElements;
                if (!resultCount) {
                    LOGGER.error(fullPath+" : COUNT NOT MATCH (manager:"+expectedNumElements+" != farm:"+numElements+")");
                }
                result = resultVersion && resultCount;
            }
            if (!result) {
                LOGGER.warn("STATUS FAIL: "+fullPath);
            } else {
                LOGGER.debug("STATUS OK: "+fullPath);
            }
        } catch (RuntimeException | IOException | URISyntaxException e) {
            result = false;
            LOGGER.error("STATUS FAIL: "+fullPath);
            LOGGER.error(e);
        }
        return result ? StatusFarm.OK : StatusFarm.FAIL;
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

}
