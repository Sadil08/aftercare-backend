#  After-Care Portal Backend

Backend service for the **After-Care Portal**, a digital platform that streamlines Sri Lanka's death registration and post-death service workflow.

This service handles business logic, secure data processing, and workflow orchestration for managing death registration cases from initiation to final certificate issuance.

---

##  Features
- **RESTful API Architecture**
- **Role-Based Access Control** (Family, GN, Doctor, Registrar)
- **End-to-End Case Workflow Management**
- **Secure Authentication & Authorization (JWT-based)**
- **Digital Document Handling & Validation**
- **Audit Logging & Case Tracking**

---

##  Core Workflow

```
Case Intake → GN Verification → Medical Certification → Registrar Approval → B-2 Issued
```

The backend enforces strict state transitions and ensures data integrity at every step of the workflow.

---

##  Tech Stack
- **Framework:** Spring Boot
- **Language:** Java
- **Build Tool:** Maven
- **Database:** MySQL / PostgreSQL
- **Authentication:** JWT / Spring Security

---

##  Quick Start

```bash
git clone https://github.com/Sadil08/aftercare-backend.git
cd aftercare-backend
./mvnw spring-boot:run
```

---

##  Security
- JWT-based authentication
- Role-based authorization
- Encrypted data handling
- Input validation and error handling

---

##  Architecture
- **Controller Layer** — Handles API requests
- **Service Layer** — Business logic and workflow control
- **Repository Layer** — Database interactions
- **Security Layer** — Authentication & authorization

---

##  Future Enhancements
- Integration with government APIs (NIC validation, health systems)
- Advanced fraud detection mechanisms
- Scalable microservices architecture
- Enhanced monitoring and logging

---

##  License
This project is developed for academic and educational purposes.
