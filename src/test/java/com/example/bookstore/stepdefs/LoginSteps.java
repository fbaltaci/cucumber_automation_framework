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

        response = RestAssured.given().relaxedHTTPSValidation().contentType(ContentType.JSON).body(payload).post("/Account/v1/User");

        if (response.statusCode() == 201) {
            userId = response.jsonPath().getString("userID");
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

        response = RestAssured.given().relaxedHTTPSValidation().contentType(ContentType.JSON).body(payload).post("/Account/v1/GenerateToken");

        token = response.jsonPath().getString("token");
    }

    @Then("the response status code should be {int}")
    public void validateStatusCode(int expected) {
        assertEquals(expected, response.statusCode(), response.body().asPrettyString());
    }

    @Then("the response status code should be 200 and contain a token")
    public void validateTokenResponse() {
        assertEquals(200, response.statusCode());
        assertNotNull(token, "Token should not be null");
    }

    @Then("the user should be retrievable by GET request")
    public void retrieveUser() {
        response = RestAssured.given().relaxedHTTPSValidation().header("Authorization", "Bearer " + token).get("https://bookstore.toolsqa.com/Account/v1/User/" + userId);

        assertEquals(200, response.statusCode());
        assertEquals(username, response.jsonPath().getString("username"));
    }

    @Given("I generate a unique test username and an empty password")
    public void generateUsernameWithEmptyPassword() {
        username = UserUtil.generateUniqueUsername();
        password = "";
    }

    @Then("the response should contain {string}")
    public void responseShouldContain(String expectedMessage) {
        String body = response.getBody().asString();
        assertTrue(body.contains(expectedMessage), "Expected response to contain: " + expectedMessage + "\nActual: " + body);
    }

    @When("I send a POST request to generate a token with invalid password")
    public void generateTokenWithInvalidPassword() {
        String body = String.format("""
                {
                  "userName": "%s",
                  "password": "WrongPassword123!"
                }
                """, username);

        response = RestAssured.given().relaxedHTTPSValidation().contentType(ContentType.JSON).body(body).post("/Account/v1/GenerateToken");

        token = response.jsonPath().getString("token");
    }

    @Given("I set an invalid token")
    public void setInvalidToken() {
        this.token = "invalid_token_123";
    }

    @When("I send a GET request to retrieve the user")
    public void retrieveUserWithToken() {
        response = RestAssured.given().relaxedHTTPSValidation().header("Authorization", "Bearer " + token).get("https://bookstore.toolsqa.com/Account/v1/User/" + userId);
    }

    @Given("I generate a unique test username and invalid password")
    public void generateUsernameWithInvalidPassword() {
        username = UserUtil.generateUniqueUsername();
        password = "Password123"; // missing non-alphanumeric
    }

    @When("I send another POST request to create the same user again")
    public void sendDuplicateUserRequest() {
        String body = String.format("""
                {
                  "userName": "%s",
                  "password": "%s"
                }
                """, username, password);

        response = RestAssured.given().relaxedHTTPSValidation().contentType(ContentType.JSON).body(body).post("/Account/v1/User");
    }

    @When("I attempt to generate a token with an empty password")
    public void generateTokenWithEmptyPassword() {
        String body = String.format("""
                {
                  "userName": "%s",
                  "password": ""
                }
                """, username);

        response = RestAssured.given().relaxedHTTPSValidation().contentType(ContentType.JSON).body(body).post("/Account/v1/GenerateToken");
    }

    @Given("I generate a valid password only")
    public void generateOnlyPassword() {
        this.username = null;
        this.password = UserUtil.generateDefaultPassword();
    }

    @When("I attempt to generate a token with missing username")
    public void generateTokenWithoutUsername() {
        String body = String.format("""
                {
                  "password": "%s"
                }
                """, password);

        response = RestAssured.given().relaxedHTTPSValidation().contentType(ContentType.JSON).body(body).post("/Account/v1/GenerateToken");
    }

}
