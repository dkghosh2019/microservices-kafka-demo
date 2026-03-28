# Microservices Kafka Demo

A Spring Boot-based project demonstrating an **event-driven microservices architecture** using Java, Spring Boot, and Apache Kafka.

---

## 📌 Overview

This project showcases how microservices communicate asynchronously using Kafka.

It consists of three services:

- **order-service**
  - Exposes REST APIs
  - Persists orders in H2
  - Publishes `OrderCreatedEvent` to Kafka

- **inventory-service**
  - Consumes `OrderCreatedEvent`
  - Simulates inventory reservation

- **payment-service**
  - Consumes `OrderCreatedEvent`
  - Simulates payment processing

---

## 🛠️ Tech Stack

- Java 17
- Spring Boot 3
- Spring Web
- Spring Data JPA
- H2 Database
- Spring Kafka
- Docker Compose

---

## 🧭 Architecture Diagram

```text
+------------------+
|   Software User  |
+------------------+
         |
         | HTTP POST /api/orders
         v
+------------------+
|   order-service  |
|------------------|
| - REST API       |
| - H2 persistence |
| - Kafka producer |
+------------------+
         |
         | publishes OrderCreatedEvent
         v
+------------------------+
|   Kafka                |
|   topic:               |
|   order-created-topic  |
+------------------------+
        / \
       /   \
      v     v
+----------------------+   +----------------------+
| inventory-service    |   | payment-service      |
|----------------------|   |----------------------|
| - Kafka consumer     |   | - Kafka consumer     |
| - reserve inventory  |   | - process payment    |
+----------------------+   +----------------------+
```


## 🏗️ Architecture Flow

### 1. Client sends:
```
1. HTTP request to Order Service
```

### 2. `order-service`:
```
- Saves order in H2 database
- Publishes `OrderCreatedEvent` to Kafka topic:
- order-created-topic
  ```

### 3. `inventory-service`:
```
- Consumes event
- Logs inventory reservation
```


### 4. `payment-service`:
```
- Consumes event
- Logs payment processing
```

---

## 📂 Project Structure

```text
microservices-kafka-demo/
├── pom.xml
├── docker-compose.yml
├── README.md
├── order-service/
├── inventory-service/
└── payment-service/
```

## 🚀 How to Run

### 1. Start Kafka

```bash
docker compose up -d
```

### 2. Build the Project
```Bash 
mvn clean install
```

### 3. Start Services

#### Run each service in a separate terminal:

```Bash
cd order-service
mvn spring-boot:run
```

```Bash
cd inventory-service
mvn spring-boot:run
```

```Bash
cd payment-service
mvn spring-boot:run
```

## 🧪 Test the System
### Create an Order

Option 1: Using curl (Linux / Mac / Git Bash)
```Bash
curl -X POST http://localhost:8081/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "productCode": "LAPTOP-001",
    "quantity": 2,
    "unitPrice": 1200,
    "customerId": "CUST-1001"
  }'
  ```
Option 2: PowerShell (Recommended)
  ```Powershell
$body = @{
    customerId  = "1"
    productCode = "BOOK-123"
    quantity    = 2
    unitPrice   = 49.99
} | ConvertTo-Json

Invoke-RestMethod `
  -Method POST `
  -Uri "http://localhost:8081/api/orders" `
  -ContentType "application/json" `
  -Body $body
```
Option 3: PowerShell (Inline JSON)
```
Invoke-RestMethod `
  -Method POST `
  -Uri "http://localhost:8081/api/orders" `
  -ContentType "application/json" `
  -Body '{"customerId":"1","productCode":"BOOK-123","quantity":2,"unitPrice":49.99}'
```

## ✅ Expected Result
```text 
order-service returns order response 
 ```

```text 
Kafka event is published successfully  
```

inventory-service logs:

```text 
Inventory reserved successfully 
```

payment-service logs:

```text 
Payment processed successfully 
```

## 💡 Key Concepts Demonstrated

- Event-driven architecture
- Asynchronous communication using Kafka
- Microservices separation of concerns
- Producer/Consumer pattern
- REST + Messaging integration

🚀 Future Improvements
- Add database support for inventory and payment services
- Implement retry mechanism and dead-letter queue (DLQ)
- Add API Gateway
- Add centralized logging and tracing
- Add Dockerfiles for each service
- Add unit and integration tests

## 📝 Notes
- This is a simplified demo for learning and demonstration purposes
- Each service can be independently deployed