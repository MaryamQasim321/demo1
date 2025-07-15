Requirements Specification Document
Project Title: Retail Shop System – REST API Development
Project Overview
The Retail Shop System is a secure RESTful API application designed to manage core operations of a retail business, including product inventory, customer records, and order processing. The system is designed using enterprise-level design patterns to ensure scalability, security, maintainability, and modularity.
Objectives
•	To implement core creational, structural, and behavioral design patterns in a real-world application.
•	To develop and test a secure REST API using JWT-based authentication.
•	To apply the DAO and MVC patterns to separate concerns in the system architecture.
•	To document and test the API using Swagger and Postman.
•	To ensure system robustness through JUnit testing and Test-Driven Development (TDD).
System Features and Functional Requirements
Functional Requirements:
• View All Orders
o Endpoint: GET /orders
o Shows all customer orders with their status (Pending, Completed).
o Use case: Admin views sales report or processes pending orders.
• View Order by ID
o Endpoint: GET /orders/{id}
o Shows details like product(s) ordered, quantity, total amount, customer, date.
o Use case: View invoice or order details.
• Create New Order
o Endpoint: POST /orders
o Requires customer ID and list of product IDs with quantities.
o System should calculate total price and reduce stock of ordered products.
o Use case: A customer places an order.
• Create Bulk Orders 
o Endpoint: POST /orders/bulk
o Accepts a list of multiple orders in a single request. Each order should include:
- Customer ID
- List of product IDs and quantities
o System will process each order:
- Check stock availability
- Calculate total
- Reduce stock accordingly
- Return summary for each order (success/failure, reasons)
o Use case: Admin processes multiple customer orders at once (e.g., offline sales batch or upload from file).
• Modify Order
o Endpoint: PUT /orders/{id}
o Change products or quantity in an existing order (only if status is "Pending").
o Use case: Customer made a mistake or wants to change order.
• Cancel/Delete Order
o Endpoint: DELETE /orders/{id}
o Removes the order and optionally restocks items if already reserved.
o Use case: Order cancelled by customer or admin.
 Updated Flow of Operations

C. Order Processing Flow (Updated with Bulk Orders)
1.	Customer selects items to order (from GET /products).
2.	Single order is placed → POST /orders (with selected products and customer ID).
o	System:
o Calculates total
o Checks stock
o Reduces stock if available
3.	Bulk order submission (Admin or System Batch Upload) → POST /orders/bulk.
o	System:
o Iterates through each order
o For each:
	Validates data
	Checks stock
	Calculates total
	Creates order if all validations pass
	Collects response (success/failure per order)
4.	Admin or customer checks order details → GET /orders/{id}.
5.	If needed:
o	Modify order → PUT /orders/{id}
o	Cancel order → DELETE /orders/{id} (stock is restored)
Technology Stack
Component	Technology
Language	Java
Framework	JAX-RS / Spring Boot (depending on setup)
Build Tool	Maven or Gradle
Testing	JUnit
API Testing	Postman
Documentation	Swagger (OpenAPI)
Security	JWT (via Nimbus JOSE JWT or Spring Security JWT)
Database	H2 / MySQL / PostgreSQL (any supported DB)
Tools	IntelliJ / Eclipse
API Authentication
•	JWT token generated upon login
•	Token required for protected endpoints via Bearer token header
•	Example:
makefile
CopyEdit
Authorization: Bearer <token>
Testing Strategy
•	Follow Test-Driven Development (TDD) with JUnit
•	Write tests before implementation
•	Test suites include:
o	ProductServiceTest
o	CustomerServiceTest
o	OrderServiceTest
o	JwtTokenUtilTest
Deliverables
•	✅ Source code (pushed to GitHub)
•	✅ Swagger UI (API documentation accessible via browser)
•	✅ Postman Collection (exported JSON for endpoint testing)
•	✅ JUnit test reports
•	✅ ReadMe file with instructions
•	✅ This Requirements Specification Document

