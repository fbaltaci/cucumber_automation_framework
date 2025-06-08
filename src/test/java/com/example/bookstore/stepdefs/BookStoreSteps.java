package com.example.bookstore.stepdefs;

import com.example.bookstore.util.ApiHelper;
import com.example.bookstore.util.LoggerUtil;
import com.example.bookstore.util.UserUtil;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.slf4j.Logger;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BookStoreSteps {

    private static final Logger logger = LoggerUtil.getLogger(BookStoreSteps.class);
    private final ScenarioState state;
    private static final String BASE_URL = "https://bookstore.toolsqa.com";

    public BookStoreSteps(ScenarioState scenarioState) {
        this.state = scenarioState;
    }

    @Given("I have a valid user and token")
    public void createValidUserAndToken() {
        state.username = UserUtil.generateUniqueUsername();
        state.password = UserUtil.generateDefaultPassword();

        state.response = ApiHelper.createUser(state.username, state.password);
        assertEquals(201, state.response.getStatusCode(), "Failed to create user.");
        state.userId = state.response.jsonPath().getString("userID");

        state.response = ApiHelper.generateToken(state.username, state.password);
        assertEquals(200, state.response.getStatusCode(), "Failed to generate token.");
        state.token = state.response.jsonPath().getString("token");

        logger.info("Created user with ID: {} and token: {}", state.userId, state.token);
    }

    @When("I send a GET request to fetch all books")
    public void getAllBooks() {
        state.response = ApiHelper.getAllBooks();
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
        state.response = ApiHelper.getBookByIsbn(isbn);
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
        state.response = ApiHelper.addBooksToUser(state.token, state.userId, List.of(isbn));
        logger.info("Add book response status: {}", state.response.statusCode());
        logger.debug("Add book response: {}", state.response.getBody().asString());
    }

    @When("I send a PUT request to replace book with ISBN {string} with {string}")
    public void replaceBookInUserCollection(String oldIsbn, String newIsbn) {
        state.response = ApiHelper.updateBookForUser(BASE_URL, state.token, state.userId, oldIsbn, newIsbn, null, null, null);
        logger.info("Replacing book {} with {} for user {}", oldIsbn, newIsbn, state.userId);
        logger.debug("Replace book response: {}", state.response.getBody().asString());
    }

    @When("I send a DELETE request to remove book with ISBN {string}")
    public void deleteBookFromUserCollection(String isbn) {
        state.response = ApiHelper.deleteBookForUser(state.token, state.userId, isbn);
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

        state.response = io.restassured.RestAssured.given().relaxedHTTPSValidation()
                                                   .baseUri("https://bookstore.toolsqa.com")
                                                   .contentType(io.restassured.http.ContentType.JSON)
                                                   .header("Authorization", "Bearer " + state.token).body(payload)
                                                   .delete("/BookStore/v1/Book");

        logger.info("Attempted deleting book {} without user ID", isbn);
        logger.debug("Delete response without user ID: {}", state.response.getBody().asString());
    }

    @When("I send a POST request with ISBN {string} with invalid userId")
    public void i_send_a_post_request_with_isbn_with_invalid_user_id(String isbn) {
        String invalidUserId = "invalid-user-123";

        String payload = String.format("""
                {
                  "userId": "%s",
                  "collectionOfIsbns": [{"isbn": "%s"}]
                }
                """, invalidUserId, isbn);

        state.response = io.restassured.RestAssured.given().relaxedHTTPSValidation()
                                                   .baseUri("https://bookstore.toolsqa.com")
                                                   .contentType(io.restassured.http.ContentType.JSON)
                                                   .header("Authorization", "Bearer " + state.token).body(payload)
                                                   .post("/BookStore/v1/Books");

        logger.info("Sent POST request with invalid user ID. Status: {}", state.response.getStatusCode());
        logger.debug("Response body: {}", state.response.getBody().asString());
    }

    @Given("I have a valid user and a book with ISBN {string} in their collection")
    public void i_have_a_valid_user_and_a_book_with_isbn_in_their_collection(String isbn) {
        createValidUserAndToken();
        state.response = ApiHelper.addBooksToUser(state.token, state.userId, List.of(isbn));
        int statusCode = state.response.getStatusCode();
        assertEquals(201, statusCode, "Failed to add book to user's collection");
        logger.info("Created user and added book with ISBN {}. Status: {}", isbn, statusCode);
        logger.debug("Add book response body: {}", state.response.getBody().asString());
    }
}
