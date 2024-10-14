# Customer Management Application

## Description

**Customer Management Application** is a Spring Boot application designed to manage customer information and audit actions. The application provides a REST API for creating, updating, deleting customers, and viewing audit entries with pagination and sorting. All data is stored in an in-memory H2 database for easy setup and testing.

## Table of Contents

- [Requirements](#requirements)
- [Technologies](#technologies)
- [Installation](#installation)
- [Running the Application](#running-the-application)
- [API Endpoints](#api-endpoints)
    - [Customer Management](#customer-management)
    - [Audit Entries](#audit-entries)
    - [Actuator](#actuator)
- [Testing](#testing)
- [Additional Details](#additional-details)

## Requirements
List the original requirements of the assignment. It’s important to reflect the specific expectations clearly so anyone reviewing the code understands the purpose behind each implementation.

```markdown
Create a REST based web application (Spring Boot, REST API, Spring DATA, JPA) that supports customer management:
- CRUD operations
    - **New customer can be created**
        - Name (max length is 100 characters)
        - Age (must be positive number)
        - Date of birth (supported date format is yyyy-MM-dd)
        - Address (max length is 200 characters, can be null)
        - Gender (M - Male, F - Female, can be null)
    - **Existing customers can be fetched**
        - Pagination and ordering are supported (default order by name, default pagination size=10)
    - **An existing customer can be updated**
        - Any details can be changed
    - **An existing customer can be deleted**
        - Any existing customer can be deleted
- Proper data and action validations are in place!
    - Example for failing validations:
        - Update a customer that does not exist -> Throw exception!
        - Provided customer name is null or empty! -> Throw exception!
        - etc.
- Health check available
    - Endpoint to check whether service is up and running
- For write operations (CREATE, UPDATE, DELETE) audit entries to be created
    - Fields
        - action: describe the action that was executed
        - customerId: write operation on which customer (might be null)
        - request: contains the incoming request (might be null)
        - status: outcome of the action (SUCCESS, FAILED)
        - creation datetime: when the audit event got created
    - Audit entries must be created and persisted regardless the outcome of the customer write operation!
    - Audit entries can be fetched (default sorting is LATEST FIRST, default pagination size = 100)
- The class names and package names should be descriptive of the functionality it serves!

For persistent storage an in-memory H2 DB or a dockerized database can be used!

For building tool please use Maven or Gradle!

For incoming requests and outgoing responses please use JSON format!
```

## Technologies

- **Java 17**
- **Spring Boot 3.3.3**
- **Spring Data JPA**
- **Hibernate**
- **MapStruct**
- **Lombok**
- **Maven**
- **H2 Database** (in-memory for development and testing)
- **JUnit 5** and **Mockito** (for testing)

## Installation

### Prerequisites

- **Java 17** or higher
- **Maven 3.8.x** or higher

### Steps

1. **Clone the repository:**

    ```bash
    git clone https://github.com/leksinch/customer-management.git
    cd customer-management
    ```

2. **Build the project:**

    ```bash
    mvn clean install
    ```

   This will compile the project, run the tests, and create an executable JAR file.

## Running the Application

### Running with Maven

```bash
mvn spring-boot:run
```

### Running from the JAR file

After successfully building, execute the following command:

```bash
java -jar target/customer-management-0.0.1-SNAPSHOT.jar
```

### Accessing the Application

The application will be available at: `http://localhost:8080`

## H2 Database Console

The application uses an embedded **H2** database to store customer and audit data. You can access the H2 console to view and manage the data:

- **URL:** `http://localhost:8080/h2-console`
- **JDBC URL:** `jdbc:h2:mem:customermanagementdb`
- **Username:** `sa`
- **Password:** *(leave empty)*

**Note:** The database runs in memory, so all data will be lost when the application stops.

## API Endpoints

### Customer Management

#### 1. Create a Customer

- **URL:** `/api/customers`
- **Method:** `POST`
- **Description:** Creates a new customer.

- **Sample Request:**

    ```http
    POST /api/customers
    ```

- **Request Body:**

    ```json
    {
        "name": "John",
        "age": 30,
        "dateOfBirth": "1994-04-15",
        "address": "123 Main St",
        "gender": "M"
    }
    ```

- **Response:**
    - **Status:** `201 Created`
    - **Body:**

        ```json
        {
            "id": 1,
            "name": "John",
            "age": 30,
            "dateOfBirth": "1994-04-15",
            "address": "123 Main St",
            "gender": "M"
        }
        ```
  - **Errors:**
      - `400 Bad Request` — if the input data is invalid.

#### 2. Get Customer by ID

- **URL:** `/api/customers/{id}`
- **Method:** `GET`
- **Description:** Retrieves a customer by their ID.
- **Parameters:**
    - `id` — Customer ID.
- **Sample Request:**

    ```http
    GET /api/customers/1
    ```
- **Response:**
    - **Status:** `200 OK`
    - **Body:**

        ```json
        {
            "id": 1,
            "name": "John",
            "age": 30,
            "dateOfBirth": "1994-04-15",
            "address": "123 Main St",
            "gender": "M"
        }
        ```
    - **Errors:**
        - `404 Not Found` — if the customer is not found.

#### 3. Get All Customers with Pagination and Sorting

- **URL:** `/api/customers`
- **Method:** `GET`
- **Description:** Retrieves a paginated list of customers with optional sorting.
- **Request Parameters:**
    - `page` (optional) — Page number (0-based). **Default:** `0`
    - `size` (optional) — Number of customers per page. **Default:** `10`
    - `sortBy` (optional) — Field to sort by. **Default:** `name`
    - `sortDir` (optional) — Sort direction (`asc` or `desc`). **Default:** `asc`
- **Sample Request:**

    ```http
    GET /api/customers?page=0&size=10&sortBy=name&sortDir=asc
    ```

- **Response:**
    - **Status:** `200 OK`
    - **Body:**

        ```json 
        {
            "content": [                        
                {
                    "id": 2,
                    "name": "Alice",
                    "age": 28,
                    "dateOfBirth": "1996-05-10",
                    "address": "456 Maple St",
                    "gender": "F"
                },
                {
                    "id": 1,
                    "name": "Bob",
                    "age": 31,
                    "dateOfBirth": "1993-04-15",
                    "address": "456 Oak St",
                    "gender": "M"
                }
            ],
            "page": {
                "size": 10,
                "number": 0,
                "totalElements": 2,
                "totalPages": 1
            }
        }
        ```

    - **Errors:**
        - `400 Bad Request` — if any query parameters are invalid.

#### 4. Update a Customer

- **URL:** `/api/customers/{id}`
- **Method:** `PUT`
- **Description:** Updates a customer's details.
- **Parameters:**
    - `id` — Customer ID.

- **Sample Request:**

    ```http
    PUT /api/customers/1
    ```

- **Request Body:**

    ```json
    {
        "name": "Bob",
        "age": 31,
        "dateOfBirth": "1993-04-15",
        "address": "456 Oak St",
        "gender": "M"
    }
    ```

- **Response:**
    - **Status:** `200 OK`
    - **Body:**

        ```json
        {
            "id": 1,
            "name": "Bob",
            "age": 31,
            "dateOfBirth": "1993-04-15",
            "address": "456 Oak St",
            "gender": "M"
        }
        ```
    - **Errors:**
        - `400 Bad Request` — if the input data is invalid.
        - `404 Not Found` — if the customer is not found.

#### 5. Delete a Customer

- **URL:** `/api/customers/{id}`
- **Method:** `DELETE`
- **Description:** Deletes a customer by their ID.
- **Parameters:**
    - `id` — Customer ID.

- **Sample Request:**

    ```http
    DELETE /api/customers/1
    ```
- **Response:**
    - **Status:** `204 No Content`
    - **Errors:**
        - `404 Not Found` — if the customer is not found.

### Audit Entries

#### 1. Retrieve Audit Entries

- **URL:** `/api/audit-entries`
- **Method:** `GET`
- **Description:** Retrieves audit entries with pagination and sorting.
- **Request Parameters:**
    - `page` (optional) — Page number (0-based). **Default:** `0`
    - `size` (optional) — Page size. **Default:** `100`
    - `sortBy` (optional) — Sorting field. **Allowed values:** `id`, `action`, `customerId`, `status`, `creationDatetime`. **Default:** `creationDatetime`
    - `sortDir` (optional) — Sorting direction (`asc` or `desc`). **Default:** `desc`
- **Sample Request:**

    ```http
    GET /api/audit-entries?page=0&size=50&sortBy=action&sortDir=asc
    ```

- **Response:**
    - **Status:** `200 OK`
    - **Body:**

        ```json
        {
            "content": [
                {
                    "id": 1,
                    "action": "CREATE",
                    "customerId": null,
                    "request": "{\"name\":\"John\",\"age\":30,\"dateOfBirth\":\"1994-04-15\",\"address\":\"123MainSt\",\"gender\":\"M\"}",
                    "status": "SUCCESS",
                    "creationDatetime": "2024-09-26T12:25:29.57721"
                }
            ],
            "page": {
                "size": 50,
                "number": 0,
                "totalElements": 1,
                "totalPages": 1
            }
        }
        ```
    - **Errors:**
        - `400 Bad Request` — for invalid sorting or pagination parameters.
### Actuator

#### 1. Health Check

- **URL:** `/actuator/health`
- **Method:** `GET`
- **Description:** Provides the health status of the application.

Spring Boot Actuator is used to expose this endpoint, and it is enabled by default in the application.

## Testing

### Running Tests

To execute all tests, use the following command:

```bash
mvn test
```

This will run all tests to ensure the correctness of the application.

## Additional Details

### Configuration

The application configuration file is located in `src/main/resources/application.yml`. By default, the application uses the in-memory H2 database. You can change the database settings by modifying the parameters in `application.yml`.
