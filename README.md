**MedicalProject**

MedicalProject is a Java-based REST API automation framework designed for testing medical service endpoints using TestNG, Rest-Assured, and Allure.

**Overview**

This project is designed to automate RESTful API testing for a medical service platform. It provides a scalable structure with externalized test data, encrypted configurations, reporting, and database integration.

**Technologies and Tools**

- Java 21 (with preview features enabled)
- Maven (for dependency management and build)
- TestNG (test execution and suite management)
- Rest-Assured (for REST API testing)
- Allure (for advanced test reporting)
- Jackson (JSON serialization and deserialization)
- Apache POI (Excel utilities if needed)
- JDBC (for database integration)
- JavaMail (for sending reports via email)
- AES Encryption (for storing credentials securely)

**Project Structure**

MedicalProject/
├── src/
│ ├── main/java/ → Common utility classes, configuration, and encryption
│ └── test/java/ → Test cases and test logic organized by modules
├── testng.xml → TestNG suite file
├── pom.xml → Maven dependencies and plugins
├── config.properties → Central configuration file (encrypted credentials, DB, SMTP, etc.)
├── allure-wrapper.bat → Batch file to serve Allure reports
└── README.md


**Key Features**

- Modular test case design using TestNG and Java
- Page Object Model (POM) design pattern for API endpoints
- External test data in JSON format
- Reusable utility classes (e.g., for files, reports, email, encryption)
- MySQL or SQL Server database access using JDBC
- Automated HTML and Allure reporting
- Emailing report summary and attachments
- AES-encrypted sensitive configuration (e.g., DB password, SMTP credentials)

**Dependencies**

Key Maven dependencies used:
<dependencies>
    <dependency>io.rest-assured:rest-assured</dependency>
    <dependency>org.testng:testng</dependency>
    <dependency>io.qameta.allure:allure-testng</dependency>
    <dependency>io.qameta.allure:allure-rest-assured</dependency>
    <dependency>com.fasterxml.jackson.core:jackson-databind</dependency>
    <dependency>com.microsoft.sqlserver:mssql-jdbc</dependency>
    <dependency>org.apache.poi:poi-ooxml</dependency>
    <dependency>com.sun.mail:jakarta.mail</dependency>
    <dependency>org.slf4j:slf4j-simple</dependency>
</dependencies>

**Build and Run**

To run tests and generate reports:

# Clean, compile, test, and generate Allure results
mvn clean verify

# Generate Allure report
mvn allure:report

# Optionally serve the report on a local server
allure serve target/allure-results

Ensure Allure CLI is installed via:

npm install -g allure-commandline

**Configuration**

Update your config.properties file for:

    Base URI

    Database connection details (host, port, db name, user, encrypted password)

    SMTP email configuration (host, port, sender, receiver, encrypted credentials)

Passwords should be encrypted using the built-in AES encryption utility.


**Report Sharing**

After test execution, the framework can:

    Generate Allure HTML reports

    Compress and encode reports in base64

    Embed or attach reports in emails using JavaMail

    Send embedded Allure summary (HTML) in the email body

**Contribution**

Feel free to fork the repo, make improvements, and open pull requests. Contributions should include:

    Well-structured code and class organization

    Appropriate logging and error handling

    Relevant test cases and validation logic

    Unit or integration test coverage

**Author**

Abdullah Mostafa
GitHub: Abdullahmostafah
