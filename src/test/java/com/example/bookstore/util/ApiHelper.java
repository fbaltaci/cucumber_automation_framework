package com.example.bookstore.util;

import com.example.bookstore.stepdefs.LoginSteps;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

public class ApiHelper {

    private static final String BASE_URL = "https://bookstore.toolsqa.com";
    private static final Logger logger = LoggerUtil.getLogger(LoginSteps.class);

    public static Response createUser(String username, String password) {
        String payload = String.format("""
                {
                  "userName": "%s",
                  "password": "%s"
                }
                """, username, password);
        logger.info("Creating user: {}", username);
        Response response = RestAssured.given().relaxedHTTPSValidation().baseUri(BASE_URL).contentType(ContentType.JSON).body(payload).post("/Account/v1/User");
        logger.debug("Create user response: {}", response.getBody().asString());

        return response;
    }

    public static Response generateToken(String username, String password) {
        String payload = String.format("""
                {
                  "userName": "%s",
                  "password": "%s"
                }
                """, username, password);

        return RestAssured.given().relaxedHTTPSValidation().baseUri(BASE_URL).contentType(ContentType.JSON).body(payload).post("/Account/v1/GenerateToken");
    }

    public static Response retrieveUser(String userId, String token) {
        return RestAssured.given().relaxedHTTPSValidation().baseUri(BASE_URL).header("Authorization", "Bearer " + token).get("/Account/v1/User/" + userId);
    }

    public static Response getAllBooks() {
        return RestAssured.given().relaxedHTTPSValidation().baseUri(BASE_URL).get("/BookStore/v1/Books");
    }

    public static Response getBookByIsbn(String isbn) {
        return RestAssured.given().relaxedHTTPSValidation().baseUri(BASE_URL).queryParam("ISBN", isbn).get("/BookStore/v1/Book");
    }

    public static Response addBooksToUser(String token, String userId, String isbn) {
        Map<String, Object> payload = Map.of("userId", userId, "collectionOfIsbns", List.of(Map.of("isbn", isbn)));

        return RestAssured.given().relaxedHTTPSValidation().baseUri(BASE_URL).header("Authorization", "Bearer " + token).contentType(ContentType.JSON).body(payload).post("/BookStore/v1/Books");
    }

    public static Response updateBookForUser(String token, String userId, String isbn) {
        Map<String, Object> payload = Map.of("userId", userId, "isbn", isbn);

        return RestAssured.given().relaxedHTTPSValidation().baseUri(BASE_URL).header("Authorization", "Bearer " + token).contentType(ContentType.JSON).body(payload).put("/BookStore/v1/Books");
    }

    public static Response deleteBookForUser(String token, String userId, String isbn) {
        Map<String, Object> payload = Map.of("userId", userId, "isbn", isbn);

        return RestAssured.given().relaxedHTTPSValidation().baseUri(BASE_URL).header("Authorization", "Bearer " + token).contentType(ContentType.JSON).body(payload).delete("/BookStore/v1/Book");
    }
}
