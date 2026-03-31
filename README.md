# 🏆 StreetLeague Backend - Esprit PI 2026

![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4.3-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-Auth-000000?style=for-the-badge&logo=json-web-tokens&logoColor=white)

Welcome to the *StreetLeague Backend* repository. This is the core engine of the StreetLeague platform, built with Spring Boot 3 and Java 21. It provides a robust, secure, and scalable REST API for managing amateur sports leagues, venue bookings, and a dynamic sponsorship ecosystem.

---

## ✨ Features

The backend orchestrates several mission-critical modules:

### 🔐 Advanced Security & Auth
- *JWT Authentication*: Stateless authentication with encrypted tokens.
- *Role-Based Access Control (RBAC)*: Support for 7+ unique roles (Admin, Player, Coach, Referee, Sponsor, etc.).
- *Password Protection*: BCrypt encryption for user credentials.

### 💰 Sponsorship & Economy
- *Request Lifecycle*: Sponsors can submit requests, and Admins approve or reject them.
- *Proof of Payment*: Secure handling of payment confirmation documents.
- *Dynamic Sponsorships*: Integration with teams, venues, and specific events.

### 🛒 E-commerce & Merchandise
- *Shopping Cart*: Real-time cart management with stock validation.
- *Player Merch*: Players can submit custom merchandise for Admin approval before listing.
- *Orders*: Full lifecycle management (Pending -> Processing -> Shipped -> Delivered).

### 🏟️ Sports Management
- *Venues*: Management of sports facilities and their reservation systems.
- *Events*: Coordination of matches, tournaments, and training sessions.
- *Carpooling*: Logic for coordinating transportation for team members.

---

## 🛠️ Technology Stack

- *Core Framework*: Spring Boot 3.4.3
- *Language*: Java 21 (LTS)
- *Database*: MySQL 8.0
- *ORM*: Spring Data JPA / Hibernate
- *Security*: Spring Security 6.x + JJWT (JSON Web Token)
- *Utilities*: Lombok, Jackson (JSON), Maven
- *Environment*: Local environment at http://localhost:8089/SpringSecurity

---

## 🚀 Getting Started

### Prerequisites
- *JDK 21*
- *Maven 3.8+*
- *MySQL 8.0*

### Installation & Run
1.  *Configure the Database*:
    - Build a database named pi in MySQL.
    - Update src/main/resources/application.properties with your MySQL credentials.
2.  *Initialize the Schema*:
    - Run the SQL script found in database_migration.sql to set up the tables.
3.  *Build & Run*:
    
    mvn clean install
    mvn spring-boot:run
    
    Or use the provided batch scripts:
    
cmd
    run-app.bat
    

---

## 📁 Project Architecture

text
src/main/java/tn/esprit/PI/
├── controller/    # REST API Controllers (Exposition)
├── service/       # Business Logic Layer
├── repository/    # Data Access Layer (Spring Data JPA)
├── domain/        # JPA Entities (Database Schema)
├── dto/           # Data Transfer Objects (Payloads)
├── security/      # Security filters & JWT configurations
└── config/        # General Bean & App configurations

---

## 📖 API Documentation

The project includes built-in documentation for developers:
- *Detailed Endpoints*: See [API_REFERENCE.md](API_REFERENCE.md)
- *Sponsorship Details*: See [SPONSORSHIP_SHOP_ENHANCEMENTS.md](SPONSORSHIP_SHOP_ENHANCEMENTS.md)
- *Integration Guide*: See [FRONTEND_INTEGRATION.md](FRONTEND_INTEGRATION.md)

---

## 🧪 Testing & Quality
- *Unit Testing*: JUnit 5 & Mockito.
- *Coverage*: JaCoCo reports are generated after running tests.
- *Batch Tools*: Use test-and-run.bat for quick validation.

---

## 📄 License
Academic Project - Esprit University 2026. All rights reserved.
