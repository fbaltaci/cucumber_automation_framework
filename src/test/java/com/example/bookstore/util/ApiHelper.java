package com.example.bookstore.util;

import com.example.bookstore.stepdefs.LoginSteps;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;

public class ApiHelper {

    private static final String BASE_URL = "https://bookstore.toolsqa.com";
    private static final Logger logger = LoggerUtil.getLogger(LoginSteps.class);

    /**
     * Creates a new user
     *
     * @param username Username
     * @param password Password
     * @return Response
     */
    public static Response createUser(String username, String password) {
        String payload = String.format("""
                {
                  "userName": "%s",
                  "password": "%s"
                }
                """, username, password);
        logger.info("Creating user: {}", username);
        Response response = RestAssured.given().relaxedHTTPSValidation().baseUri(BASE_URL).contentType(ContentType.JSON)
                                       .body(payload).post("/Account/v1/User");
        logger.debug("Create user response: {}", response.getBody().asString());

        return response;
    }

    /**
     * Generates a token for the user
     *
     * @param username Username
     * @param password Password
     * @return Response
     */
    public static Response generateToken(String username, String password) {
        String payload = String.format("""
                {
                  "userName": "%s",
                  "password": "%s"
                }
                """, username, password);

        return RestAssured.given().relaxedHTTPSValidation().baseUri(BASE_URL).contentType(ContentType.JSON)
                          .body(payload).post("/Account/v1/GenerateToken");
    }

    /**
     * Retrieves the user's information
     *
     * @param userId User ID
     * @param token  Token
     * @return Response
     */
    public static Response retrieveUser(String userId, String token) {
        return RestAssured.given().relaxedHTTPSValidation().baseUri(BASE_URL).header("Authorization", "Bearer " + token)
                          .get("/Account/v1/User/" + userId);
    }

    /**
     * Retrieves all books from the catalog
     *
     * @return Response
     */
    public static Response getAllBooks() {
        return RestAssured.given().relaxedHTTPSValidation().baseUri(BASE_URL).get("/BookStore/v1/Books");
    }

    /**
     * Retrieves a book by ISBN
     *
     * @param isbn ISBN
     * @return Response
     */
    public static Response getBookByIsbn(String isbn) {
        return RestAssured.given().relaxedHTTPSValidation().baseUri(BASE_URL).queryParam("ISBN", isbn)
                          .get("/BookStore/v1/Book");
    }

    /**
     * Add a book to the user's collection
     *
     * @param token  Token
     * @param userId User ID
     * @param isbns  ISBN of the book to add
     * @return Response
     */
    public static Response addBooksToUser(String token, String userId, List<String> isbns) {
        List<Map<String, String>> isbnList = isbns.stream().map(isbn -> Map.of("isbn", isbn)).toList();

        Map<String, Object> payload = Map.of("userId", userId, "collectionOfIsbns", isbnList);

        return RestAssured.given().relaxedHTTPSValidation().baseUri(BASE_URL).header("Authorization", "Bearer " + token)
                          .contentType(ContentType.JSON).body(payload).post("/BookStore/v1/Books");
    }

    /**
     * Replaces a book in the user's collection with another book.
     *
     * @param baseUrl         Base URL of the API
     * @param token           Authorization token
     * @param userId          User ID to update the book for
     * @param oldIsbn         ISBN of the book to replace
     * @param newIsbn         ISBN of the new book
     * @param customPath      Custom endpoint path (optional)
     * @param customHeaders   Custom headers (optional)
     * @param payloadOverride Custom payload (optional)
     * @return RestAssured Response
     */
    public static Response updateBookForUser(String baseUrl, String token, String userId, String oldIsbn, String newIsbn, String customPath, Map<String, String> customHeaders, Map<String, Object> payloadOverride) {

        String path = (customPath != null) ? customPath : "/BookStore/v1/Books/" + oldIsbn;
        String url = baseUrl + path;

        Map<String, String> headers = (customHeaders != null) ? customHeaders : Map.of("accept", "application/json", "Authorization", "Bearer " + token);

        Map<String, Object> payload = (payloadOverride != null) ? payloadOverride : Map.of("userId", userId, "isbn", newIsbn);

        logger.info("Replacing book {} with {} for user {}", oldIsbn, newIsbn, userId);

        Response response = RestAssured.given().relaxedHTTPSValidation().headers(headers).contentType(ContentType.JSON)
                                       .body(payload).put(url);

        logger.debug("Update book response: {} - {}", response.getStatusCode(), response.getBody().asString());

        return response;
    }

    /**
     * Deletes a book from the user's collection
     *
     * @param token  Token
     * @param userId User ID
     * @param isbn   ISBN of the book to delete
     * @return Response
     */
    public static Response deleteBookForUser(String token, String userId, String isbn) {
        Map<String, Object> payload = Map.of("userId", userId, "isbn", isbn);

        return RestAssured.given().relaxedHTTPSValidation().baseUri(BASE_URL).header("Authorization", "Bearer " + token)
                          .contentType(ContentType.JSON).body(payload).delete("/BookStore/v1/Book");
    }
}
