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

import io.galeb.manager.common.LoggerUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.boot.logging.LogLevel;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class FarmClient implements CommonHttpRequester {

    private static final Log LOGGER = LogFactory.getLog(FarmClient.class);
    private static final int DRIVER_READ_TIMEOUT = Integer.parseInt(System.getProperty("io.galeb.read.timeout", "60000"));
    private static final int DRIVER_CONNECT_TIMEOUT = Integer.parseInt(System.getProperty("io.galeb.connect.timeout", "5000"));

    private final RestTemplate restTemplate;
    private final RequestConfig defaultRequestConfig;

    public FarmClient() {
        SimpleClientHttpRequestFactory clientHttp = new SimpleClientHttpRequestFactory();
        clientHttp.setReadTimeout(DRIVER_READ_TIMEOUT);
        clientHttp.setConnectTimeout(DRIVER_CONNECT_TIMEOUT);
        this.restTemplate = new RestTemplate(clientHttp);
        this.defaultRequestConfig = RequestConfig.custom().setSocketTimeout(DRIVER_READ_TIMEOUT)
                                                          .setConnectTimeout(DRIVER_CONNECT_TIMEOUT)
                                                          .setConnectionRequestTimeout(DRIVER_CONNECT_TIMEOUT)
                                                          .build();
    }

    @Override
    public ResponseEntity<String> get(String uriPath) throws URISyntaxException {
        final URI uri = new URI(uriWithProto(uriPath));
        final RequestEntity<Void> request = RequestEntity.get(uri).build();
        final ResponseEntity<String> response = restTemplate.exchange(request, String.class);
        logFormatted(request, response);
        return response;
    }

    @Override
    public ResponseEntity<String> post(String uriPath, String body) throws URISyntaxException {
        final URI uri = new URI(uriWithProto(uriPath));
        RequestEntity<String> request = RequestEntity.post(uri).contentType(MediaType.APPLICATION_JSON).body(body);
        ResponseEntity<String> response = restTemplate.exchange(request, String.class);
        logFormatted(request, response);
        return response;
    }

    @Override
    public ResponseEntity<String> put(String uriPath, String body) throws URISyntaxException {
        final URI uri = new URI(uriWithProto(uriPath));
        RequestEntity<String> request = RequestEntity.put(uri).contentType(MediaType.APPLICATION_JSON).body(body);
        ResponseEntity<String> response = restTemplate.exchange(request, String.class);
        logFormatted(request, response);
        return response;
    }

    @Override
    public ResponseEntity<String> delete(String uriPath, String body) throws URISyntaxException, IOException {
        final URI uri = new URI(uriWithProto(uriPath));
        final HttpEntityEnclosingRequest httpRequest = httpDeleteRequestWithBodyFactory(uri, new StringEntity(body));
        final CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(defaultRequestConfig).build();
        final HttpResponse httpResponse = httpClient.execute(new HttpHost(getHost(uri), getPort(uri)), httpRequest);
        httpClient.close();

        final ResponseEntity<String> response = convertResponse(httpResponse);
        logFormatted(convertRequest(httpRequest), response);
        return response;
    }

    @Override
    public boolean isStatusCodeEqualOrLessThan(final ResponseEntity<String> response, int status) {
        return response.getStatusCode().value() <= status;
    }

    private String uriWithProto(String uriPath) {
        return !uriPath.startsWith("http") ? "http://" + uriPath : uriPath;
    }

    private HttpEntityEnclosingRequest httpDeleteRequestWithBodyFactory(final URI uri, StringEntity entity) {
        final HttpEntityEnclosingRequest httpRequest = new HttpDeleteWithBody(uri);
        httpRequest.setEntity(entity);
        httpRequest.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
        return httpRequest;
    }

    private String getHost(final URI uri) {
        String host = uri.getHost();
        return host != null ? host : "";
    }

    private int getPort(final URI uri) {
        int port = uri.getPort();
        return port > 0 ? port : 80;
    }

    private ResponseEntity<String> convertResponse(final HttpResponse response) throws IOException {
        final InputStream content = response.getEntity().getContent();
        final String body = extractContent(content);
        final MultiValueMap<String, String> headers = getHeaders(response);
        final HttpStatus statusCode = EnumSet.allOf(HttpStatus.class)
                                             .stream()
                                             .filter(status -> status.value() == response.getStatusLine().getStatusCode())
                                             .findFirst().orElse(HttpStatus.INTERNAL_SERVER_ERROR);

        return new ResponseEntity<>(body, headers, statusCode);
    }

    private RequestEntity<String> convertRequest(final HttpEntityEnclosingRequest request) throws IOException {
        final InputStream content = request.getEntity().getContent();
        final String body = extractContent(content);
        final MultiValueMap<String, String> headers = getHeaders(request);
        final HttpMethod httpMethod = EnumSet.allOf(HttpMethod.class)
                                             .stream()
                                             .filter(method -> method.toString().equals(request.getRequestLine().getMethod()))
                                             .findFirst().orElse(HttpMethod.TRACE);

        return new RequestEntity<>(body, headers, httpMethod, URI.create(request.getRequestLine().getUri()));
    }

    private MultiValueMap<String, String> getHeaders(final Header[] arrayOfHeaders) {
        final MultiValueMap<String, String> headers = new org.springframework.http.HttpHeaders();
        final Function<Header, List<String>> headerListFunction = header -> Arrays.asList(header.getValue().split(","));
        final Collector<Header, ?, Map<String, List<String>>> headerMapCollector = Collectors.toMap(Header::getName, headerListFunction);
        final Map<String, List<String>> mapOfHeaders = Arrays.stream(arrayOfHeaders).collect(headerMapCollector);
        headers.putAll(mapOfHeaders);
        return headers;
    }

    private MultiValueMap<String, String> getHeaders(final HttpEntityEnclosingRequest request) {
        return getHeaders(request.getAllHeaders());
    }

    private MultiValueMap<String, String> getHeaders(final HttpResponse response) {
        return getHeaders(response.getAllHeaders());
    }

    private void logFormatted(final RequestEntity<?> request, final ResponseEntity<String> response) {
        final LogLevel logLevel = response.getStatusCode().value() >= 400 ? LogLevel.ERROR : LogLevel.INFO;
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
        if (StringUtils.countOccurrencesOf(responseBody, "},{") == 0) {
            LoggerUtils.logger(LOGGER, logLevel, responseBody);
        }
    }

    private String extractContent(InputStream content) {
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

    @NotThreadSafe
    private static class HttpDeleteWithBody extends HttpEntityEnclosingRequestBase {

        @Override
        public String getMethod() {
            return "DELETE";
        }

        HttpDeleteWithBody(final URI uri) {
            super();
            setURI(uri);
        }
    }
}
