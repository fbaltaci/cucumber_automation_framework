package com.example.bookstore.stepdefs;

import com.example.bookstore.util.LoggerUtil;
import com.example.bookstore.util.UserUtil;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.slf4j.Logger;

import static org.junit.jupiter.api.Assertions.*;

public class LoginSteps {

    private static final Logger logger = LoggerUtil.getLogger(LoginSteps.class);

    private String username;
    private String password;
    private String userId;
    private String token;
    private Response response;

    @Given("I generate a unique test username and password")
    public void generateUniqueUser() {
        username = UserUtil.generateUniqueUsername();
        password = UserUtil.generateDefaultPassword();
        logger.info("Generated username: {}, password: {}", username, password);
    }

    @When("I send a POST request to create the user")
    public void createUser() {
        RestAssured.baseURI = "https://bookstore.toolsqa.com";
        String payload = String.format("""
                {
                  "userName": "%s",
                  "password": "%s"
                }
                """, username, password);

        logger.info("Sending POST request to create user: {}", username);
        response = RestAssured.given().relaxedHTTPSValidation().contentType(ContentType.JSON).body(payload).post("/Account/v1/User");
        logger.debug("Create user response: {}", response.getBody().asString());

        if (response.statusCode() == 201) {
            userId = response.jsonPath().getString("userID");
            logger.info("User created successfully with userId: {}", userId);
        } else {
            logger.warn("User creation failed: Status {}, Body: {}", response.statusCode(), response.getBody().asString());
        }
    }

    @When("I send a POST request to generate a token for the user")
    public void generateToken() {
        String payload = String.format("""
                {
                  "userName": "%s",
                  "password": "%s"
                }
                """, username, password);

        logger.info("Requesting token for user: {}", username);
        response = RestAssured.given().relaxedHTTPSValidation().contentType(ContentType.JSON).body(payload).post("/Account/v1/GenerateToken");
        logger.debug("Generate token response: {}", response.getBody().asString());

        token = response.jsonPath().getString("token");
        logger.info("Received token: {}", token);
    }

    @Then("the response status code should be {int}")
    public void validateStatusCode(int expected) {
        int actual = response.statusCode();
        logger.info("Validating response status: expected {}, actual {}", expected, actual);
        assertEquals(expected, actual, response.body().asPrettyString());
    }

    @Then("the response status code should be 200 and contain a token")
    public void validateTokenResponse() {
        logger.info("Validating token response...");
        assertEquals(200, response.statusCode());
        assertNotNull(token, "Token should not be null");
    }

    @Then("the user should be retrievable by GET request")
    public void retrieveUser() {
        logger.info("Retrieving user with userId: {}", userId);
        response = RestAssured.given().relaxedHTTPSValidation().header("Authorization", "Bearer " + token).get("https://bookstore.toolsqa.com/Account/v1/User/" + userId);
        logger.debug("Retrieve user response: {}", response.getBody().asString());

        assertEquals(200, response.statusCode());
        assertEquals(username, response.jsonPath().getString("username"));
    }

    @Given("I generate a unique test username and an empty password")
    public void generateUsernameWithEmptyPassword() {
        username = UserUtil.generateUniqueUsername();
        password = "";
        logger.info("Generated user with empty password - username: {}", username);
    }

    @Then("the response should contain {string}")
    public void responseShouldContain(String expectedMessage) {
        String body = response.getBody().asString();
        logger.info("Validating response contains '{}'", expectedMessage);
        assertTrue(body.contains(expectedMessage), "Expected response to contain: " + expectedMessage + "\nActual: " + body);
    }

    @When("I send a POST request to generate a token with invalid password")
    public void generateTokenWithInvalidPassword() {
        logger.info("Sending token request with invalid password for user: {}", username);
        String body = String.format("""
                {
                  "userName": "%s",
                  "password": "WrongPassword123!"
                }
                """, username);

        response = RestAssured.given().relaxedHTTPSValidation().contentType(ContentType.JSON).body(body).post("/Account/v1/GenerateToken");
        logger.debug("Token response: {}", response.getBody().asString());

        token = response.jsonPath().getString("token");
    }

    @Given("I set an invalid token")
    public void setInvalidToken() {
        this.token = "invalid_token_123";
        logger.info("Set invalid token manually");
    }

    @When("I send a GET request to retrieve the user")
    public void retrieveUserWithToken() {
        logger.info("Sending GET request with token: {}", token);
        response = RestAssured.given().relaxedHTTPSValidation().header("Authorization", "Bearer " + token).get("https://bookstore.toolsqa.com/Account/v1/User/" + userId);
        logger.debug("Response: {}", response.getBody().asString());
    }

    @Given("I generate a unique test username and invalid password")
    public void generateUsernameWithInvalidPassword() {
        username = UserUtil.generateUniqueUsername();
        password = "Password123"; // missing non-alphanumeric
        logger.info("Generated user with invalid password (missing special char): {}", username);

    }

    @When("I send another POST request to create the same user again")
    public void sendDuplicateUserRequest() {
        logger.info("Sending second user creation attempt for username: {}", username);
        String body = String.format("""
                {
                  "userName": "%s",
                  "password": "%s"
                }
                """, username, password);

        response = RestAssured.given().relaxedHTTPSValidation().contentType(ContentType.JSON).body(body).post("/Account/v1/User");
        logger.debug("Duplicate user response: {}", response.getBody().asString());
    }

    @When("I attempt to generate a token with an empty password")
    public void generateTokenWithEmptyPassword() {
        logger.info("Attempting to generate token with empty password for user: {}", username);
        String body = String.format("""
                {
                  "userName": "%s",
                  "password": ""
                }
                """, username);

        response = RestAssured.given().relaxedHTTPSValidation().contentType(ContentType.JSON).body(body).post("/Account/v1/GenerateToken");
        logger.debug("Empty password response: {}", response.getBody().asString());
    }

    @Given("I generate a valid password only")
    public void generateOnlyPassword() {
        this.username = null;
        this.password = UserUtil.generateDefaultPassword();
        logger.info("Generated password only: {}", password);
    }

    @When("I attempt to generate a token with missing username")
    public void generateTokenWithoutUsername() {
        logger.info("Attempting to generate token with missing username...");
        String body = String.format("""
                {
                  "password": "%s"
                }
                """, password);

        response = RestAssured.given().relaxedHTTPSValidation().contentType(ContentType.JSON).body(body).post("/Account/v1/GenerateToken");
        logger.debug("Missing username response: {}", response.getBody().asString());
    }

}
