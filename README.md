# Anti-Fraud System

## Summary

This project implements a simplified anti-fraud system, demonstrating principles used in the financial sector. It features a RESTful API built with Spring Boot, incorporating user authentication, role-based authorization, and transaction validation logic based on amount thresholds, IP/Card blacklists, and correlation rules using transaction history. The system includes a feedback mechanism allowing thresholds to be dynamically adjusted.

## Features Implemented (by Stage)

1.  **Simple Transaction Validation:** Validates transactions based on amount (`ALLOWED`, `MANUAL_PROCESSING`, `PROHIBITED`).
2.  **Authentication:** Implements user registration and HTTP Basic authentication using Spring Security and persists users in an H2 database.
3.  **Authorization:** Introduces roles (ADMINISTRATOR, MERCHANT, SUPPORT), assigns roles upon registration (Admin first, then Merchants), implements account locking for non-Admin users, and restricts API access based on roles.
4.  **Stolen Cards & Suspicious IPs:** Adds management (CRUD operations) for IP and Card blacklists (accessible by SUPPORT role) and integrates checks against these blacklists into transaction validation. Updates transaction response format.
5.  **Rule-based System:** Persists transaction history and implements correlation checks based on the number of unique IPs and Regions used for a card number within the last hour, adding potential "ip-correlation" and "region-correlation" reasons.
6.  **Feedback:** Allows SUPPORT users to submit feedback on transactions, which dynamically adjusts the `ALLOWED` and `MANUAL_PROCESSING` amount thresholds using predefined formulas. Adds endpoints to view transaction history.

## Technologies Used

* Java (17+ recommended)
* Spring Boot
* Spring Security
* Spring Data JPA
* H2 Database Engine
* Lombok
* Gradle

## API Endpoints

**Authentication (`/api/auth`)**

* `POST /user`: Register a new user (Permit All).
* `GET /list`: List all users (ADMINISTRATOR, SUPPORT).
* `DELETE /user/{username}`: Delete a user (ADMINISTRATOR).
* `PUT /role`: Change a user's role (ADMINISTRATOR).
* `PUT /access`: Lock/Unlock a user (ADMINISTRATOR).

**Anti-Fraud (`/api/antifraud`)**

* `POST /transaction`: Validate a transaction (MERCHANT).
* `PUT /transaction`: Submit feedback for a completed transaction (SUPPORT).
* `POST /suspicious-ip`: Add a suspicious IP address (SUPPORT).
* `GET /suspicious-ip`: List all suspicious IP addresses (SUPPORT).
* `DELETE /suspicious-ip/{ip}`: Delete a suspicious IP address (SUPPORT).
* `POST /stolencard`: Add a stolen card number (SUPPORT).
* `GET /stolencard`: List all stolen card numbers (SUPPORT).
* `DELETE /stolencard/{number}`: Delete a stolen card number (SUPPORT).
* `GET /history`: List all transaction history (SUPPORT).
* `GET /history/{number}`: List transaction history for a specific card number (SUPPORT).

## Setup & Running

1.  **Prerequisites:** Java JDK (version 17 or higher recommended) installed.
2.  **Clone:** Clone the repository (replace with actual clone command if applicable).
    ```bash
    # git clone <repository-url>
    # cd <repository-directory>
    ```
3.  **Build:** Use Gradle wrapper to build the project.
    ```bash
    ./gradlew build
    ```
    (On Windows: `gradlew build`)
4.  **Run:** Execute the generated JAR file.
    ```bash
    java -jar task/build/libs/Anti-Fraud_System-task-0.0.1-SNAPSHOT.jar
    ```
    (Adjust JAR file name if necessary)
5.  **Access:** The application will start on port `28852`.

## Configuration (`application.properties`)

* **Port:** `server.port=28852`
* **Database:** Uses an H2 file-based database (`service_db`).
* **H2 Console:** Accessible at `/h2-console` with JDBC URL `jdbc:h2:file:../service_db`, username `sa`, password `password`. Note: The path `../service_db` is relative to where the JAR runs.

## Notes
* Default user credentials need to be created via the registration endpoint (`POST /api/auth/user`). The first registered user gets the ADMINISTRATOR role. Subsequent users get the MERCHANT role and are initially locked.
