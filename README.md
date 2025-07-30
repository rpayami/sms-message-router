# SMS Message Router

A Spring Boot application that routes SMS messages to different carriers based on phone number country codes and handles opt-out functionality.

## Features

- **Carrier Routing**: Routes messages to appropriate carriers based on phone number country codes
  - AU numbers → Telstra/Optus (random selection)
  - NZ numbers → Spark
  - All other countries → Global carrier (default)
- **Opt-out Management**: Prevents sending messages to opted-out phone numbers
- **Message Status Tracking**: Tracks message status

## Assumptions

**Current Scope and Limitations:**

This application is designed as a **message routing and validation service** that handles the initial stages of SMS message processing. The system:

- **Routes messages to appropriate carriers** based on phone number country codes
- **Validates phone numbers** and checks opt-out status
- **Saves messages with initial status** (PENDING for valid messages, BLOCKED for opted-out numbers)
- **Provides integration points** for carrier service integration, but does not perform actual message delivery. As an assummption for the integration point, it provides webhook for updating status to DELIVERED.

**What the system does NOT do:**
- **Actual message delivery** to end users
- **Status updates** to SENT or DELIVERED which require integration with carrier services. Currently there are extensible and helper integration points, e.g. `onMessagePending` hook method, `updateSentStatus` and `updateDeliveredStatus` methods in `MessageService`), and a webhook API to update status to `DELIVERED`.

**Future Integration Requirements:**
To complete the SMS delivery pipeline, integration with actual carrier services would be required to:
- Send messages to carrier APIs
- Update message status from PENDING to SENT/DELIVERED

**Current Implementation Notes:**
- **Carrier routing logic**: The logic to route a messge to a carrier service is centralized in `CarrierRouterService` and can be extended e.g. using more complicated logic or using exrernal services. This includes Australian carrier selection (Telstra vs Optus) which is currently implemented as a simple random selection. 
- **Phone Number Validation and Normalization**: Uses Google's libphonenumber library.
- **Extending Message Types**: The `Message` abstract class, `MessageType` enum, and `MessgeFactory`, help extend and introduce other `Message` subclasses like `MMSMessage`, in addition to the currently existing `SMSMessage` class
- **SENT and DELIVERED Status Update**: Currently there are extensible and helper integration points, e.g. `onMessagePending` hook method which calls `updateSentStatus`, and a `DELIVERED` webhook which can be called by carrier services. 
  - **NOTE**: A Messging framework like `Kafka`, providing a queue or `Topic` for interacting with carrier services, can help here for scalability.

## Quick Start

### Prerequisites

- Java 17 or higher
- Maven (or use the included Maven wrapper)

### Build the Application

```bash
# Clean and build the project
./mvnw clean install
```

### Run the Application

```bash
# Run using Maven Spring Boot plugin
./mvnw spring-boot:run
```

The application will start on `http://localhost:8080` by default.

## API Endpoints

### Send SMS Message

**POST** `/messages`

Send an SMS message to a phone number.

**Request Body:**
```json
{
  "destinationNumber": "+61412345678",
  "content": "A sample text",
  "format": "SMS"
}
```

**Response (Success - 201 Created):**
```json
{
  "messageId": 1,
  "status": "PENDING"
}
```

**Response (Opted-out - 403 Forbidden):**
```
Cannot send message to this phone number as it is opted out
```

**Response (Invalid Phone - 400 Bad Request):**
```
Invalid phone number
```

### Get Message Status

**GET** `/messages/{id}`

Get the status of a specific message.

**Response (200 OK):**
```
PENDING
```

**Response (404 Not Found):**
```
(empty response)
```

### Get All Messages (testing purpose)

**GET** `/messages`

Get all messages in the system.

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "destinationNumber": "+61412345678",
    "content": "A sample text",
    "type": "SMS",
    "status": "PENDING",
    "carrier": "TELSTRA",
    "createdAt": "2025-07-30T13:30:00"
  }
]
```

### Update Message to Delivered Status (based on assumptions)

**POST** `/messages/{id}/delivered`

Update a message status to DELIVERED (webhook endpoint for carrier services).

**Response (200 OK):**
```
Message status updated
```

**Response (404 Not Found):**
```
(empty response)
```

### Opt-out Phone Number

**POST** `/optout/{phoneNumber}`

Opt-out a phone number from receiving messages.

**Response (201 Created):**
```json
{
  "id": 1,
  "phoneNumber": "+61487654321"
}
```

**Response (400 Bad Request - Already opted out):**
```
Phone number already opted out
```

### Remove Opt-out

**DELETE** `/optout/{phoneNumber}`

Remove a phone number from the opt-out list.

**Response (200 OK):**
```
1
```

**Response (400 Bad Request - Not opted out):**
```
Phone number cannot be opted in as it is not opted out
```

## API Examples

### Example 1: Send Message to Australian Number

```bash
curl -X POST http://localhost:8080/messages \
  -H "Content-Type: application/json" \
  -d '{
    "destinationNumber": "+61412345678",
    "content": "A sample text",
    "format": "SMS"
  }'
```

**Expected Response:**
```json
{
  "messageId": 1,
  "status": "PENDING"
}
```

### Example 2: Send Message to New Zealand Number

```bash
curl -X POST http://localhost:8080/messages \
  -H "Content-Type: application/json" \
  -d '{
    "destinationNumber": "+64211234567",
    "content": "A sample text",
    "format": "SMS"
  }'
```

### Example 3: Send Message to US Number (Global Carrier)

```bash
curl -X POST http://localhost:8080/messages \
  -H "Content-Type: application/json" \
  -d '{
    "destinationNumber": "+14158438453",
    "content": "A sample text",
    "format": "SMS"
  }'
```

### Example 4: Opt-out a Phone Number

```bash
curl -X POST http://localhost:8080/optout/+61487654321
```

### Example 5: Send Message to Opted-out Number

```bash
curl -X POST http://localhost:8080/messages \
  -H "Content-Type: application/json" \
  -d '{
    "destinationNumber": "+61487654321",
    "content": "This should be blocked",
    "format": "SMS"
  }'
```

**Expected Response (403 Forbidden):**
```
Cannot send message to this phone number as it is opted out
```

### Example 6: Get Message Status

```bash
curl http://localhost:8080/messages/1
```

**Expected Response:**
```
PENDING
```

### Example 7: Get All Messages (testing purpose)

```bash
curl http://localhost:8080/messages
```

### Example 8: Send Message with Invalid Phone Number

```bash
curl -X POST http://localhost:8080/messages \
  -H "Content-Type: application/json" \
  -d '{
    "destinationNumber": "12345",
    "content": "This should fail",
    "format": "SMS"
  }'
```

**Expected Response (400 Bad Request):**
```
Invalid phone number
```

### Example 9: Opt-out with Invalid Phone Number

```bash
curl -X POST http://localhost:8080/optout/12345
```

**Expected Response (400 Bad Request):**
```
Invalid phone number
```

### Example 10: Update Message to Delivered Status (based on assumptions)

```bash
curl -X POST http://localhost:8080/messages/1/delivered
```

**Expected Response (200 OK):**
```
Message status updated
```

**Expected Response (404 Not Found):**
```
(empty response)
```

## Testing

Run the test suite:

```bash
# Run all tests
./mvnw test

# Run only tests (skip compilation)
./mvnw test -Dmaven.test.skip=false

# Run only unit tests
./mvnw test -Dtest=*UnitTest

# Run only integration tests
./mvnw test -Dtest=*ApplicationTest
```

## Database

Usng Spring-Data-JDBC, the application uses an H2 in-memory database which makes it xtensible to another database in the future.

- **H2 Console**: Available at `http://localhost:8080/h2-console`
- **JDBC URL**: `jdbc:h2:mem:testdb`
- **Username**: `sa`
- **Password**: password

## Project Structure

```
src/
├── main/java/com/sinch/demo/smsMessageRouter/
│   ├── message/
│   │   ├── controller/     # REST API controllers
│   │   ├── model/         # Message entities
│   │   ├── repository/    # Data access layer
│   │   └── service/       # Business logic
│   ├── optOut/
│   │   ├── controller/    # Opt-out API
│   │   ├── model/        # Opt-out entities
│   │   ├── repository/   # Data access layer
│   │   └── service/      # Business logic
│   └── utils/            # Utility classes
└── test/java/            # Test classes
```

## Carrier Routing Logic

- **AU (+61)**: Routes to Telstra or Optus (currently, random selection)
- **NZ (+64)**: Routes to Spark
- **All others**: Routes to Global carrier (default)

## Error Handling

- **Invalid phone numbers**: Returns 400 Bad Request
- **Opted-out numbers**: Returns 403 Forbidden
- **Message not found**: Returns 404 Not Found
- **Server errors**: Returns 500 Internal Server Error
