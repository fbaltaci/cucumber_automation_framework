package com.example.bookstore.stepdefs;

import com.example.bookstore.util.ApiHelper;
import com.example.bookstore.util.LoggerUtil;
import com.example.bookstore.util.UserUtil;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.slf4j.Logger;

import static org.junit.jupiter.api.Assertions.*;

public class LoginSteps {

    private static final Logger logger = LoggerUtil.getLogger(LoginSteps.class);
    private final ScenarioState state;

    public LoginSteps(ScenarioState scenarioState) {
        this.state = scenarioState;
    }

    @Given("I generate a unique test username and password")
    public void generateUniqueUser() {
        state.username = UserUtil.generateUniqueUsername();
        state.password = UserUtil.generateDefaultPassword();
        logger.info("Generated username: {}, password: {}", state.username, state.password);
    }

    @When("I send a POST request to create the user")
    public void createUser() {
        state.response = ApiHelper.createUser(state.username, state.password);
        logger.debug("Create user response: {}", state.response.getBody().asString());

        if (state.response.statusCode() == 201) {
            state.userId = state.response.jsonPath().getString("userID");
            logger.info("User created successfully with userId: {}", state.userId);
        } else {
            logger.warn("User creation failed: Status {}, Body: {}", state.response.statusCode(), state.response
                    .getBody().asString());
        }
    }

    @When("I send a POST request to generate a token for the user")
    public void generateToken() {
        state.response = ApiHelper.generateToken(state.username, state.password);
        logger.debug("Generate token response: {}", state.response.getBody().asString());

        state.token = state.response.jsonPath().getString("token");
        logger.info("Received token: {}", state.token);
    }

    @Then("the response status code should be {int}")
    public void validateStatusCode(int expected) {
        int actual = state.response.statusCode();
        logger.info("Validating response status: expected {}, actual {}", expected, actual);
        assertEquals(expected, actual, state.response.body().asPrettyString());
    }

    @Then("the response status code should be 200 and contain a token")
    public void validateTokenResponse() {
        logger.info("Validating token response...");
        assertEquals(200, state.response.statusCode());
        assertNotNull(state.token, "Token should not be null");
    }

    @Then("the user should be retrievable by GET request")
    public void retrieveUser() {
        logger.info("Retrieving user with userId: {}", state.userId);
        state.response = ApiHelper.retrieveUser(state.userId, state.token);
        logger.debug("Retrieve user response: {}", state.response.getBody().asString());

        assertEquals(200, state.response.statusCode());
        assertEquals(state.username, state.response.jsonPath().getString("username"));
    }

    @Given("I generate a unique test username and an empty password")
    public void generateUsernameWithEmptyPassword() {
        state.username = UserUtil.generateUniqueUsername();
        state.password = "";
        logger.info("Generated user with empty password - username: {}", state.username);
    }

    @Then("the response should contain {string}")
    public void responseShouldContain(String expectedMessage) {
        String body = state.response.getBody().asString();
        logger.info("Validating response contains '{}'", expectedMessage);
        assertTrue(body.contains(expectedMessage), "Expected response to contain: " + expectedMessage + "\nActual: " + body);
    }

    @When("I send a POST request to generate a token with invalid password")
    public void generateTokenWithInvalidPassword() {
        logger.info("Sending token request with invalid password for user: {}", state.username);
        state.response = ApiHelper.generateToken(state.username, "WrongPassword123!");
        logger.debug("Token response: {}", state.response.getBody().asString());
        state.token = state.response.jsonPath().getString("token");
    }

    @Given("I set an invalid token")
    public void setInvalidToken() {
        state.token = "invalid_token_123";
        logger.info("Set invalid token manually");
    }

    @When("I send a GET request to retrieve the user")
    public void retrieveUserWithToken() {
        logger.info("Sending GET request with token: {}", state.token);
        state.response = ApiHelper.retrieveUser(state.userId, state.token);
        logger.debug("Response: {}", state.response.getBody().asString());
    }

    @Given("I generate a unique test username and invalid password")
    public void generateUsernameWithInvalidPassword() {
        state.username = UserUtil.generateUniqueUsername();
        state.password = "Password123"; // missing non-alphanumeric
        logger.info("Generated user with invalid password (missing special char): {}", state.username);
    }

    @When("I send another POST request to create the same user again")
    public void sendDuplicateUserRequest() {
        logger.info("Sending second user creation attempt for username: {}", state.username);
        state.response = ApiHelper.createUser(state.username, state.password);
        logger.debug("Duplicate user response: {}", state.response.getBody().asString());
    }

    @When("I attempt to generate a token with an empty password")
    public void generateTokenWithEmptyPassword() {
        logger.info("Attempting to generate token with empty password for user: {}", state.username);
        state.response = ApiHelper.generateToken(state.username, "");
        logger.debug("Empty password response: {}", state.response.getBody().asString());
    }

    @Given("I generate a valid password only")
    public void generateOnlyPassword() {
        state.username = null;
        state.password = UserUtil.generateDefaultPassword();
        logger.info("Generated password only: {}", state.password);
    }

    @When("I attempt to generate a token with missing username")
    public void generateTokenWithoutUsername() {
        logger.info("Attempting to generate token with missing username...");
        String body = String.format("""
                {
                  "password": "%s"
                }
                """, state.password);

        state.response = RestAssured.given().relaxedHTTPSValidation().contentType(ContentType.JSON).body(body)
                                    .post("/Account/v1/GenerateToken");
        logger.debug("Missing username response: {}", state.response.getBody().asString());
    }
}
