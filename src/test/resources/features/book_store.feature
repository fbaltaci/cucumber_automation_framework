@book_store
Feature: Book Store API functionality

  @positive
  Scenario: Retrieve all books from the catalog
    When I send a GET request to fetch all books
    Then the response status code should be 200
    And the response should contain a non-empty list of books

  @positive
  Scenario: Retrieve a specific book by ISBN
    When I send a GET request to fetch book with ISBN "9781449325862"
    Then the response status code should be 200
    And the response should contain the book title "Git Pocket Guide"

  @negative
  Scenario: Retrieve a book using an invalid ISBN
    When I send a GET request to fetch book with ISBN "invalid-isbn-123"
    Then the response status code should be 400
    And the response should contain "ISBN supplied is not available in Books Collection!"

  @positive
  Scenario: Add a book to the user's collection
    Given I have a valid user and token
    When I send a POST request to add book with ISBN "9781449325862" to the user's account
    Then the response status code should be 201

  @negative
  Scenario: Add a book with an invalid ISBN to the user's collection
    Given I have a valid user and token
    When I send a POST request to add book with ISBN "invalid-isbn-123" to the user's account
    Then the response status code should be 400
    And the response should contain "ISBN supplied is not available in Books Collection!"

  @negative
  Scenario: Add a book with an invalid token
    Given I have a valid user and token
    And I set an invalid token
    When I send a POST request to add book with ISBN "9781449325862" to the user's account
    Then the response status code should be 401
    And the response should contain "User not authorized!"

  @negative
  Scenario: Add a book with missing userId in request payload
    Given I have a valid user and token
    When I send a POST request with ISBN "9781449325862" with invalid userId
    Then the response status code should be 401
    And the response should contain "User Id not correct!"

  @positive
  Scenario: Update a book in the user's collection
    Given I have a valid user and a book with ISBN "9781449325862" in their collection
    When I send a PUT request to replace book with ISBN "9781449325862" with "9781449331818"
    Then the response status code should be 200

  @negative
  Scenario: Replace a book that does not exist in the user's collection
    Given I have a valid user and token
    When I send a PUT request to replace book with ISBN "9999999999999" with "9781449331818"
    Then the response status code should be 400
    And the response should contain "ISBN supplied is not available in User's Collection!"

  @negative
  Scenario: Replace a book with an invalid token
    Given I have a valid user and a book with ISBN "9781449325862" in their collection
    And I set an invalid token
    When I send a PUT request to replace book with ISBN "9781449325862" with "9781449331818"
    Then the response status code should be 401
    And the response should contain "User not authorized!"

  @negative
  Scenario: Replace a book with an invalid new ISBN
    Given I have a valid user and a book with ISBN "9781449325862" in their collection
    When I send a PUT request to replace book with ISBN "9781449325862" with "invalid-isbn"
    Then the response status code should be 400
    And the response should contain "ISBN supplied is not available in Books Collection!"

  @positive
  Scenario: Delete a book from the user's collection
    Given I have a valid user and a book with ISBN "9781449325862" in their collection
    When I send a DELETE request to remove book with ISBN "9781449325862"
    Then the response status code should be 204

  @negative
  Scenario: Delete a book not present in the user's collection
    Given I have a valid user and token
    When I send a DELETE request to remove book with ISBN "9999999999999"
    Then the response status code should be 400
    And the response should contain "ISBN supplied is not available in User's Collection!"

  @negative
  Scenario: Delete a book with an invalid token
    Given I have a valid user and a book with ISBN "9781449325862" in their collection
    And I set an invalid token
    When I send a DELETE request to remove book with ISBN "9781449325862"
    Then the response status code should be 401
    And the response should contain "User not authorized!"

  @negative
  Scenario: Delete a book with missing user ID
    Given I have a valid user and token
    When I send a DELETE request to remove book with ISBN "9781449325862" without user ID
    Then the response status code should be 400
    And the response should contain "User Id is required."