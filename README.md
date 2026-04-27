# Lending Application (Spring Boot)

## Overview

This project is a simplified lending application built using Spring Boot. It models core loan management processes including product configuration, loan issuance, repayment handling, overdue processing, and notifications.

The system is designed with a focus on clean architecture, domain-driven design principles, and extensibility, while keeping implementation pragmatic for the scope of the assignment.

---

## Features

### Product Management

* Create and manage loan products
* Configurable:

    * Tenure (days/months)
    * Fees (fixed or percentage-based)
    * Grace period
* Soft delete support via `deletedAt`

---

### Loan Management

* Apply for loans based on products
* Snapshot-based configuration to ensure historical consistency
* Supports:

    * Lump sum loans
    * Installment-based loans
    * Credit score–based loan eligibility and limit enforcement
* Automatic:

    * Installment generation
    * Fee application at origination
* Loan lifecycle states:

    * OPEN
    * CLOSED
    * CANCELLED
    * OVERDUE
    * WRITTEN_OFF

---

### Payment Processing

* Flexible payment handling
* Supports:

    * Partial payments
    * Overpayments
    * Multi-installment allocation

#### Allocation Strategy

Payments are applied using a FIFO approach:

* Earliest unpaid installment is settled first
* Remaining amount flows to subsequent installments

This ensures realistic and robust handling of real-world repayment behavior.

---

### Overdue Processing (Scheduled Job)

* Daily sweep job detects overdue loans
* Updates loan status to `OVERDUE`
* Triggers notification events

---

### Notifications (Event-Driven)

* Implemented using Spring’s event system
* Events:

    * Loan creation
    * Payment received
    * Loan overdue
* Current implementation uses console output
* Designed to support multiple channels (Email, SMS, etc.)

---

## Architecture

The system follows a modular structure:

```
customer → product → loan → payment → notification
```
* Each module is loosely coupled, with communication handled through service orchestration and domain events.

### Key Design Decisions

#### 1. Domain-Centric Loan Model

Loan acts as the aggregate root:

* Encapsulates payment application logic
* Maintains consistency of installments and balances

---

#### 2. Payment Allocation Model

A payment can span multiple installments.

To support this, a `PaymentAllocation` entity is used:

```
Payment → PaymentAllocation → LoanInstallment
```

This ensures:

* Accurate tracking of fund distribution
* Support for partial and excess payments

---

#### 3. Snapshotting Product Configuration

Loan captures product attributes (tenure, fees, etc.) at creation time.

This avoids inconsistencies if product configuration changes later.

---

#### 4. Event-Driven Notifications

Spring events are used to decouple business logic from notification handling.

This keeps the core domain clean and extensible.

---

#### 5. Soft Deletes

Products are not physically removed but marked using `deletedAt`.

In a production system, queries would exclude soft-deleted records.

---

## Technology Stack

* Java 25
* Spring Boot
* Spring Data JPA
* PostgreSQL (configurable)
* JUnit 5 + Mockito

---

## Running the Application

### Prerequisites

* Java 25
* Maven
* Docker

### Steps

```bash
git clone git@github.com:muchiri08/lenda.git
cd lenda
docker compose up -d
./mvnw spring-boot:run
```

The application will start on:

```
http://localhost:8080
```

---

## API Overview

### Customer

* `POST /customers` → Create customer
```json
{
    "fullName": "John Doe",
    "email": "john@example.com",
    "phone": "254712345678"
}
```
Above action returns a location header check it when you perform the action. Simply it's the below Get Customer with the id
* `GET /customers/{id}` → Get customer
* `GET /customers/{id}/loan-limit` Get customer loan limit

NOTE:
* At customer creation, I am generating random **credit score** just for demo purposes that will be used on loan request as a risk check.
* To check a customer loan limit based on the generated credit score use `GET /customers/{id}/loan-limit`

### Product

* `POST /products` → Create product
```json
{
  "name": "Premium Annual Subscription",
  "tenureType": "MONTHS",
  "tenureValue": 5,
  "gracePeriod": 7,
  "fees": [
    {
      "type": "SERVICE",
      "calculationType": "FIXED",
      "amount": 1000.00
    },
    {
      "type": "DAILY",
      "calculationType": "PERCENTAGE",
      "amount": 4
    },
    {
      "type": "LATE",
      "calculationType": "PERCENTAGE",
      "amount": 5
    }
  ]
}
```
Above action returns a location header check it when you perform the action. Simply it's the below Get Product with the id
* `GET /products/{id}` → Get product
* `GET /products` → List products
* `DELETE /products/{id}` → Soft delete

### Loan

* `POST /loans` → Apply for loan
```json
{
    "customerId": 1,
    "productId": 1,
    "amount": 20000,
    "type" : "INSTALLMENT" //or LUMP_SUM
}
```
Above action returns a location header check it when you perform the action. Simply it's the below Get Loan with the id
* `GET /loans/{id}` → Get loan details
* `GET /loans` → List loans

### Payment

* `POST /loans/payments/{loanId}` → Make payment
```json
{
    "amount" : 4800,
    "method": "MOBILE_MONEY"
}
```

NOTE:
* I used a simple credit score-based loan limit policy that uses static tiers for demo purposes. This can be replaced with a more sophisticated risk engine in a real world scenario.
* The score based policy table is as below:
### Loan Limit Policy (Demo)

| Credit Score | Max Loan Limit |
|-------------|---------------|
| 0 – 10      | 0             |
| 11 – 30     | 10,000        |
| 31 – 50     | 20,000        |
| 51 – 70     | 30,000        |
| 71 – 90     | 40,000        |
| 91 – 100    | 50,000        |

---

## Testing

Unit tests are implemented using JUnit 5 and Mockito.

Test coverage focuses on:

* Loan creation logic
* Payment allocation across installments
* Overdue detection
* Service-level behavior

Tests are designed to validate business logic rather than framework behavior.

Run tests:

```bash
./mvnw test
```

---

## Assumptions & Simplifications

* Notification channels are simulated via console output
* No external integrations (SMS/email providers)
* No authentication/authorization
* Late fee logic simplified for scope
* Validation and error handling are intentionally minimal for demonstration purposes
* Used spring in memory event mechanism for the demo

---

## Future Improvements

* Introduce API validation layer
* Add authentication and authorization
* Integrate real notification providers (Email/SMS)
* Improve late fee strategies (daily accrual vs one-time)
* Introduce caching for frequently accessed data
* Add pagination and filtering for APIs
* Expand reporting and analytics

---

## Conclusion

The design prioritizes simplicity while leaving clear extension points for production-grade enhancements

---
