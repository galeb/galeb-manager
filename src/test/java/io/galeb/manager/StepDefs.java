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

package io.galeb.manager;

import static com.jayway.restassured.RestAssured.with;
import static org.hamcrest.Matchers.hasToString;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import io.galeb.manager.queue.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.flywaydb.core.Flyway;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationContextLoader;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.config.RedirectConfig;
import com.jayway.restassured.config.RestAssuredConfig;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.response.ValidatableResponse;
import com.jayway.restassured.specification.RequestSpecification;

import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import gherkin.deps.com.google.gson.Gson;
import gherkin.deps.com.google.gson.GsonBuilder;
import io.galeb.manager.entity.AbstractEntity;
import io.galeb.manager.repository.DatabaseConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        classes = { Application.class },
        loader = SpringApplicationContextLoader.class
)
@WebAppConfiguration
@IntegrationTest({"server.port=0"})
@Ignore
public class StepDefs {

    private static final Flyway FLYWAY     = new Flyway();
    private static final Log    LOGGER     = LogFactory.getLog(StepDefs.class);
    private static final Gson   jsonParser = new GsonBuilder().setPrettyPrinting().create();

    @Value("${local.server.port}")
    private int port;

    @PersistenceContext
    private EntityManager em;

    private RequestSpecification request;

    private ValidatableResponse response;

    private String token;

    private RedirectConfig redirectConfig = RestAssuredConfig.config().getRedirectConfig()
                                                                      .followRedirects(false);

    private RestAssuredConfig restAssuredConfig = RestAssuredConfig.config().redirect(redirectConfig);

    @PostConstruct
    public void init() {
        FLYWAY.setDataSource(DatabaseConfiguration.getUrl(),
                             DatabaseConfiguration.getUsername(),
                             DatabaseConfiguration.getPassword());
    }

    @Before
    public void setUp() {
        response = null;
        request = null;
        token = null;
        FLYWAY.migrate();
        System.setProperty(AmqpConfigurator.DISABLE_QUEUE, Boolean.toString(true));
    }

    @After
    public void cleanUp() {
        final URI logoutUrl = URI.create("http://127.0.0.1:"+port+"/logout");
        try {
            with().header("x-auth-token", token).get(logoutUrl).andReturn();
            token = "";
        } catch (Exception e) {
            LOGGER.warn(e);
        }
        FLYWAY.clean();
        System.setProperty(AmqpConfigurator.DISABLE_QUEUE, Boolean.toString(false));
    }

    private String processFullUrl(String data) {
        String key = "=";
        if (data.contains(key)) {
            int indexOf = data.indexOf(key);
            String entityClass = data.substring(0, indexOf);
            return "http://localhost/" + entityClass.toLowerCase() + "/" + getIdFromData(data, indexOf);
        }
        return data;
    }

    private String getIdFromData(String dataWithTypeAndName, int keyPos) {
        String id = "0";
        String entityClass = dataWithTypeAndName.substring(0, keyPos);
        String entityName = dataWithTypeAndName.substring(keyPos + 1, dataWithTypeAndName.length());
        String jpqlFindByName ="SELECT e FROM " + entityClass + " e WHERE e.name = '" + entityName + "'";
        Query query = em.createQuery(jpqlFindByName);
        AbstractEntity<?> entity = null;

        try {
            entity = (AbstractEntity<?>) query.getSingleResult();
        } catch (NoResultException e) {
            LOGGER.warn("CUCUMBER: " + dataWithTypeAndName + "NOT FOUND (" + e.getMessage() + ")");
        } finally {
            if (entity != null) {
               id = String.valueOf(entity.getId());
            } else {
                LOGGER.warn("CUCUMBER: " + dataWithTypeAndName + "NOT FOUND");
            }
        }
        return id;
    }

    @Given("^a REST client unauthenticated$")
    public void givenRestClientUnauthenticated() throws Throwable {
        request = with().config(restAssuredConfig).contentType("application/json");
        LOGGER.info("Using "+RestAssured.class.getName()+" unauthenticated");
    }

    @Given("^a REST client authenticated as (.*)$")
    public void givenRestClientAuthenticatedAs(String login) throws Throwable {
        final URI loginUrl = URI.create("http://127.0.0.1:"+port+"/");
        Response result = with().auth().basic(login, "password")
                                .get(loginUrl).thenReturn();

        if (result.getStatusCode() == 200) {
            final URI tokenURI = URI.create("http://127.0.0.1:"+port+"/token");
            token = with().auth().basic(login, "password").get(tokenURI)
                                 .thenReturn().body().jsonPath().getString("token");
        } else {
            token="NULL";
        }

        try {
            request = with().config(restAssuredConfig).contentType("application/json")
                                                      .header("x-auth-token", token);
        } catch (Exception e) {
            request = with().config(restAssuredConfig).contentType("application/json");
            LOGGER.warn(e);
        }
        LOGGER.info("Using "+RestAssured.class.getName()+" authenticated as "+login);
    }

    @When("^request json body has:$")
    public void requestJsonBodyHas(Map<String, String> jsonComponents) throws Throwable {
        if (!jsonComponents.isEmpty() && !jsonComponents.keySet().contains("")) {
            final Map<String, Object> jsonComponentsProcessed = new HashMap<>();
            jsonComponents.entrySet().stream().forEach(entry -> {
                String oldValue = entry.getValue();
                if (oldValue.contains("[")) {
                    String[] arrayOfValues = oldValue.replaceAll("\\[|\\]| ", "").split(",");
                    for (int x = 0; x < arrayOfValues.length; x++) {
                        arrayOfValues[x] = processFullUrl(arrayOfValues[x]);
                    }
                    jsonComponentsProcessed.put(entry.getKey(), arrayOfValues);
                } else {
                    oldValue = processFullUrl(oldValue);
                    jsonComponentsProcessed.put(entry.getKey(), oldValue);
                }
            });
            String json = jsonParser.toJson(jsonComponentsProcessed);
            request.body(json);
        }
    }

    @When("^request uri-list body has:$")
    public void requestUriListBodyHas(List<String> uriList) throws Throwable {
        request.contentType("text/uri-list");
        if (!uriList.isEmpty()) {
            String body = uriList.stream().map(this::processFullUrl)
                                          .collect(Collectors.joining("\n"));
            request.body(body);
        }
    }

    @When("^request body is (.*)")
    public void requesthBodyIs(String body) {
        if (body !=null && !"".equals(body)) {
            request.body(body);
        }
    }

    @And("^send (.+) (.+)$")
    public void sendMethodPath(String method, String path) throws Throwable {
        URI fullUrl = URI.create(processFullUrl(path));
        switch (method) {
        case "GET":
            response = request.get(fullUrl).then();
            break;
        case "POST":
            final String fullUrlStr="http://127.0.0.1:"+port+path;
            response = request.post(URI.create(fullUrlStr)).then();
            break;
        case "PUT":
            response = request.put(fullUrl).then();
            break;
        case "PATCH":
            response = request.patch(fullUrl).then();
            break;
        case "DELETE":
            response = request.delete(fullUrl).then();
            break;
        default:
            break;
        }
    }

    @Then("^the response status is (\\d+)$")
    public void ThenTheResponseStatusIs(int status) throws Throwable {
        response.statusCode(status);
    }

    @And("^property (.*) contains (.*)$")
    public void andPropertyContains(String property, String value) throws Throwable {
        if (property!=null && !"".equals(property)) {
            response.body(property, hasToString(value));
        }
    }

}
