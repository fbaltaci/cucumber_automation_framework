package com.example.bookstore.stepdefs;

import com.example.bookstore.util.LoggerUtil;
import com.example.bookstore.util.UserUtil;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.slf4j.Logger;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

public class BookStoreSteps {

    private static final Logger logger = LoggerUtil.getLogger(BookStoreSteps.class);
    private final ScenarioState state;

    public BookStoreSteps(ScenarioState scenarioState) {
        this.state = scenarioState;
    }

    private static final String BASE_URL = "https://bookstore.toolsqa.com";

    @When("I send a POST request with ISBN {string} with invalid userId")
    public void i_send_a_post_request_with_isbn_with_invalid_user_id(String isbn) {
        String invalidUserId = "invalid-user-123";

        String requestBody = String.format("""
                {
                  "userId": "%s",
                  "collectionOfIsbns": [
                    {
                      "isbn": "%s"
                    }
                  ]
                }
                """, invalidUserId, isbn);

        Response response = given().header("Content-Type", "application/json").body(requestBody).post("https://bookstore.toolsqa.com/BookStore/v1/Books");

        state.response = response;
    }

    @Given("I have a valid user and token")
    public void createValidUserAndToken() {
        state.username = UserUtil.generateUniqueUsername();
        state.password = UserUtil.generateDefaultPassword();

        // Create user
        String createUserPayload = String.format("""
                {
                  "userName": "%s",
                  "password": "%s"
                }
                """, state.username, state.password);

        state.response = given().relaxedHTTPSValidation().contentType(ContentType.JSON).body(createUserPayload).post(BASE_URL + "/Account/v1/User");
        assertEquals(201, state.response.getStatusCode(), "Failed to create user.");
        state.userId = state.response.jsonPath().getString("userID");

        // Generate token
        String tokenPayload = String.format("""
                {
                  "userName": "%s",
                  "password": "%s"
                }
                """, state.username, state.password);

        state.response = given().relaxedHTTPSValidation().contentType(ContentType.JSON).body(tokenPayload).post(BASE_URL + "/Account/v1/GenerateToken");

        assertEquals(200, state.response.getStatusCode(), "Failed to generate token.");
        state.token = state.response.jsonPath().getString("token");

        logger.info("Created user with ID: {} and token: {}", state.userId, state.token);
    }

    @When("I send a GET request to fetch all books")
    public void getAllBooks() {
        state.response = given().relaxedHTTPSValidation().get(BASE_URL + "/BookStore/v1/Books");

        logger.info("Fetched all books. Status: {}", state.response.statusCode());
        logger.debug("Books response body: {}", state.response.getBody().asString());
    }

    @Then("the response should contain a non-empty list of books")
    public void validateBooksList() {
        var books = state.response.jsonPath().getList("books");
        logger.debug("Books returned: {}", books);
        assertNotNull(books, "Books list is null");
        assertFalse(books.isEmpty(), "Books list is empty");
    }

    @When("I send a GET request to fetch book with ISBN {string}")
    public void getBookByIsbn(String isbn) {
        state.response = given().relaxedHTTPSValidation().get(BASE_URL + "/BookStore/v1/Book?ISBN=" + isbn);

        logger.info("Fetched book with ISBN: {}. Status: {}", isbn, state.response.statusCode());
        logger.debug("Book response body: {}", state.response.getBody().asString());
    }

    @Then("the response should contain the book title {string}")
    public void validateBookTitle(String expectedTitle) {
        String actualTitle = state.response.jsonPath().getString("title");
        logger.info("Validating book title. Expected: '{}', Actual: '{}'", expectedTitle, actualTitle);
        assertEquals(expectedTitle, actualTitle, "Book title mismatch");
    }

    @When("I send a POST request to add book with ISBN {string} to the user's account")
    public void addBookToUser(String isbn) {
        String payload = String.format("""
                {
                  "userId": "%s",
                  "collectionOfIsbns": [{"isbn": "%s"}]
                }
                """, state.userId, isbn);

        state.response = given().relaxedHTTPSValidation().contentType(ContentType.JSON).header("Authorization", "Bearer " + state.token).body(payload).post(BASE_URL + "/BookStore/v1/Books");

        logger.info("Add book response status: {}", state.response.statusCode());
        logger.debug("Add book response: {}", state.response.getBody().asString());
    }

    @When("I send a PUT request to replace book with ISBN {string} with {string}")
    public void replaceBookInUserCollection(String oldIsbn, String newIsbn) {
        String payload = String.format("""
                {
                  "userId": "%s",
                  "isbn": "%s"
                }
                """, state.userId, newIsbn);

        state.response = given().relaxedHTTPSValidation().contentType(ContentType.JSON).header("Authorization", "Bearer " + state.token).body(payload).put(BASE_URL + "/BookStore/v1/Books/" + oldIsbn);

        logger.info("Replacing book {} with {} for user {}", oldIsbn, newIsbn, state.userId);
        logger.debug("Replace book response: {}", state.response.getBody().asString());
    }

    @When("I send a DELETE request to remove book with ISBN {string}")
    public void deleteBookFromUserCollection(String isbn) {
        String payload = String.format("""
                {
                  "isbn": "%s",
                  "userId": "%s"
                }
                """, isbn, state.userId);

        state.response = given().relaxedHTTPSValidation().contentType(ContentType.JSON).header("Authorization", "Bearer " + state.token).body(payload).delete(BASE_URL + "/BookStore/v1/Book");

        logger.info("Deleting book {} from user {}", isbn, state.userId);
        logger.debug("Delete book response: {}", state.response.getBody().asString());
    }

    @When("I send a DELETE request to remove book with ISBN {string} without user ID")
    public void deleteBookWithoutUserId(String isbn) {
        String payload = String.format("""
                {
                  "isbn": "%s"
                }
                """, isbn);

        state.response = given().relaxedHTTPSValidation().contentType(ContentType.JSON).header("Authorization", "Bearer " + state.token).body(payload).delete(BASE_URL + "/BookStore/v1/Book");

        logger.info("Attempted deleting book {} without user ID", isbn);
        logger.debug("Delete response without user ID: {}", state.response.getBody().asString());
    }
}
