# 🛒 Retail Shop System – REST API Development

## 📘 Project Overview

The **Retail Shop System** is a secure RESTful API designed to manage key retail business operations such as product inventory, customer records, and order processing. This project follows enterprise-level design patterns to ensure **scalability, security, maintainability, and modularity**.

---

## 🎯 Objectives

- Implement creational, structural, and behavioral design patterns in a real-world application.
- Develop and test a secure REST API using **JWT-based authentication**.
- Apply **DAO** and **MVC** design patterns to maintain a clean system architecture.
- Document and test the API using **Swagger** and **Postman**.
- Ensure robustness via **JUnit testing** and **Test-Driven Development (TDD)**.

---

## ✅ Functional Requirements

### 🔍 View All Orders
- **Endpoint:** `GET /orders`
- **Use Case:** Admin views all customer orders with status (Pending, Completed).

### 🔍 View Order by ID
- **Endpoint:** `GET /orders/{id}`
- **Use Case:** View invoice or detailed order information.

### 🆕 Create New Order
- **Endpoint:** `POST /orders`
- **Input:** Customer ID and list of product IDs with quantities.
- **System Tasks:** Calculate total, check stock, reduce inventory.
- **Use Case:** A customer places an order.

### 📦 Create Bulk Orders
- **Endpoint:** `POST /orders/bulk`
- **Input:** List of multiple orders:
  - Each order includes:
    - Customer ID
    - Product IDs and quantities
- **System Tasks:**
  - Validate data
  - Check stock
  - Calculate totals
  - Reduce inventory
  - Return per-order summary (success/failure)
- **Use Case:** Admin uploads offline sales batch.

### ✏️ Modify Order
- **Endpoint:** `PUT /orders/{id}`
- **Condition:** Only if order status is `Pending`.
- **Use Case:** Customer or admin updates product or quantity.

### ❌ Cancel/Delete Order
- **Endpoint:** `DELETE /orders/{id}`
- **System Tasks:** Delete order, optionally restore inventory.
- **Use Case:** Order canceled by customer or admin.

---

## 🔄 Order Processing Flow

1. Customer selects items → `GET /products`
2. Place single order → `POST /orders`
    - System checks stock, calculates total, reduces stock
3. Admin submits bulk orders → `POST /orders/bulk`
    - System processes each:
        - Validates data
        - Checks stock
        - Calculates total
        - Creates order (if valid)
        - Returns per-order result
4. View order → `GET /orders/{id}`
5. If needed:
    - Modify → `PUT /orders/{id}`
    - Cancel → `DELETE /orders/{id}`

---

## 🧰 Technology Stack

| Component       | Technology                         |
|----------------|-------------------------------------|
| Language        | Java                               |
| Framework       | JAX-RS / Spring Boot               |
| Build Tool      | Maven / Gradle                     |
| Database        | H2 / MySQL / PostgreSQL            |
| API Testing     | Postman                            |
| Documentation   | Swagger (OpenAPI)                  |
| Security        | JWT (e.g., Nimbus JOSE or Spring)  |
| IDE             | IntelliJ / Eclipse                 |
| Testing         | JUnit                              |

---

## 🔐 API Authentication

- JWT token generated on login
- Token required for protected endpoints using the `Authorization` header:
