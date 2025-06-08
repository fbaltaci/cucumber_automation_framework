# Cucumber Automation Framework for Bookstore API

This is an API automation framework built using **Java**, **RestAssured**, and **Cucumber**. It tests
the [ToolsQA Bookstore API](https://bookstore.toolsqa.com/), including endpoints like login, add book, delete book, and
replace book.

## Project Structure

```
├── src
│   ├── main
│   │   └── java
│   │       └── com.example.bookstore.utils        # Helper classes
│   └── test
│       ├── java
│       │   └── com.example.bookstore.stepdefs    # Step definitions
│       │   └── com.example.bookstore             # Test runner
│       └── resources
│           └── features                          # Feature files
├── target                                         # Reports (generated)
├── pom.xml
└── README.md
```

## How to Run Tests

### From IntelliJ:

1. Right-click on `RunCucumberTest.java`
2. Click **Run**

### From Command Line:

```bash
mvn clean test
```

## Sample Feature

```gherkin
Feature: Replace Book in User's Collection

  Scenario: Replace a book with a new one
    Given I have a valid token and user ID
    And I add a book with ISBN "9781449325862" to the user
    When I send a PUT request to replace book with ISBN "9781449325862" with "9781449331818"
    Then the response status code should be 200
```

## Reports

After running tests, reports will be available under:

```
target/cucumber-reports/cucumber.html
```

You can open the HTML report in your browser for detailed results.

## Tech Stack

- Java 17+
- Maven
- Cucumber (BDD)
- JUnit Platform
- RestAssured
- Log4j (for logging)

## Setup Instructions

1. Clone the repo:
   ```bash
   git clone https://github.com/your-username/cucumber_automation_framework.git
   cd cucumber_automation_framework
   ```

2. Run tests:
   ```bash
   mvn clean test
   ```

## Notes

- This framework supports negative and positive API test scenarios.
- Easily extendable for more endpoints and services.

## Author

Fehmi Baltacı  
[LinkedIn](https://linkedin.com/in/fehmi-baltaci) • [GitHub](https://github.com/fbaltaci)
