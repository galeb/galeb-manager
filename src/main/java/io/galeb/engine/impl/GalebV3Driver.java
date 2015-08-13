package io.galeb.engine.impl;

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
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.galeb.engine.Driver;
import io.galeb.handler.VirtualHostHandler;
import io.galeb.manager.common.Properties;

public class GalebV3Driver implements Driver {

    public static final String DRIVER_NAME = GalebV3Driver.class.getSimpleName()
                                                                .replaceAll("Driver", "");

    private static final Log LOGGER = LogFactory.getLog(VirtualHostHandler.class);

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
        String path = properties.getOrDefault("path", "").toString();
        String uriPath = "http://" + api + "/" + path + "/" +getIdEncoded(json);
        RestTemplate restTemplate = new RestTemplate();
        boolean result = false;

        try {
            URI uri = new URI(uriPath);
            RequestEntity<String> request = RequestEntity.put(uri).contentType(MediaType.APPLICATION_JSON).body(json);
            ResponseEntity<String> response = restTemplate.exchange(request, String.class);
            result = response.getStatusCode().value() < 400;
        } catch (RuntimeException|URISyntaxException e) {
            LOGGER.error("POST "+uriPath+" ("+e.getMessage()+")");
        }
        return result;
    }

    @Override
    public boolean remove(Properties properties) {
        boolean result = false;
        String api = properties.getOrDefault("api", "NULL").toString();
        String json = properties.getOrDefault("json", "{}").toString();
        String path = properties.getOrDefault("path", "").toString();
        String uriPath = "http://" + api + "/" + path + "/" +getIdEncoded(json);
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
