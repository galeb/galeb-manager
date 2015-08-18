package io.galeb.manager;

import static com.jayway.restassured.RestAssured.with;
import static org.hamcrest.Matchers.hasToString;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationContextLoader;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.ValidatableResponse;
import com.jayway.restassured.specification.RequestSpecification;

import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import gherkin.deps.com.google.gson.Gson;
import gherkin.deps.com.google.gson.GsonBuilder;
import io.galeb.manager.Application;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        classes = { Application.class },
        loader = SpringApplicationContextLoader.class
)
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
@WebAppConfiguration
@IntegrationTest({"server.port=0"})
@Ignore
public class StepDefs {

    private static Log LOGGER = LogFactory.getLog(StepDefs.class);
    private static Object[] PARAMS = new Object[0];
    private static final Gson jsonParser = new GsonBuilder().setPrettyPrinting()
                                                            .create();

    @Value("${local.server.port}")
    private int port;



    private RequestSpecification request;
    private ValidatableResponse response;

    @Before
    public void setUp() {
        response = null;
        request = null;
    }

    @Given("^a REST client$")
    public void givenRestClient() throws Throwable {
        request = with().contentType("application/json").auth().basic("admin", "password");
        LOGGER.info("Using "+RestAssured.class.getName());
    }

    @Given("^a REST client unauthenticated$")
    public void givenRestClientUnauthenticated() throws Throwable {
        request = with().contentType("application/json");
        LOGGER.info("Using "+RestAssured.class.getName()+" unauthenticated");
    }

    @Given("^a REST client authenticated as (.*)$")
    public void givenRestClientAuthenticatedAs(String login) throws Throwable {
        request = with().contentType("application/json").auth().basic(login, "password");
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
                    jsonComponentsProcessed.put(entry.getKey(), arrayOfValues);
                } else {
                    jsonComponentsProcessed.put(entry.getKey(), oldValue);
                }
            });
            String json = jsonParser.toJson(jsonComponentsProcessed);
            request.body(json);
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
        final String fullUrl="http://127.0.0.1:"+port+path;
        switch (method) {
        case "GET":
            response = request.get(fullUrl, PARAMS).then();
            break;
        case "POST":
            response = request.post(fullUrl, PARAMS).then();
            break;
        case "PUT":
            response = request.put(fullUrl, PARAMS).then();
            break;
        case "PATCH":
            response = request.patch(fullUrl, PARAMS).then();
            break;
        case "DELETE":
            response = request.delete(fullUrl, PARAMS).then();
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
            response.body(property,  hasToString(value));
        }
    }

}
