# VisionSlot

## English

### Overview
VisionSlot is a Spring Boot backend for managing ophthalmology consultation appointments inside a company health insurance workflow.

The application currently supports:
- consultation schedule configuration
- automatic generation of available appointment slots
- appointment creation
- appointment rescheduling
- appointment cancellation
- visual confirmation responses
- optional email notification integration
- printable appointment listing
- Excel export

### Tech Stack
- Java 21
- Spring Boot
- Spring Web
- Spring Data JPA
- H2 Database
- Maven
- Apache POI
- springdoc OpenAPI / Swagger UI

### Project Structure
- `controller` - REST endpoints
- `service` - business logic
- `repository` - database access
- `entity` - persistent domain models
- `dto` - request and response models
- `exception` - centralized error handling
- `mapper` - entity to DTO mapping

### Main Endpoints
- `GET /`
- `PUT /api/admin/configuration`
- `GET /api/admin/configuration`
- `GET /api/slots`
- `POST /api/appointments`
- `GET /api/appointments/{id}`
- `GET /api/appointments/by-employee/{employeeCode}`
- `PUT /api/appointments/{id}`
- `DELETE /api/appointments/{id}`
- `GET /api/reports/appointments`
- `GET /api/reports/appointments/export`

### Local Run
1. Open the project in IntelliJ IDEA as a Maven project.
2. Use JDK 21.
3. Reload Maven dependencies.
4. Run `VisionSlotApplication`.

Useful URLs:
- `http://localhost:8080/`
- `http://localhost:8080/swagger-ui.html`
- `http://localhost:8080/h2-console`

### Current Status
Implemented:
- backend architecture
- scheduling rules
- appointment workflows
- report export
- integration tests for core flows

Still needed for a full product:
- frontend UI
- authentication and authorization
- production database
- real SMTP configuration
- deployment setup
- additional business rules agreed by stakeholders

---

## Romana

### Prezentare Generala
VisionSlot este un backend Spring Boot pentru gestionarea programarilor la consultatii oftalmologice in cadrul fluxului intern de asigurare medicala al companiei.

Aplicatia suporta in acest moment:
- configurarea programului de consultatii
- generarea automata a sloturilor disponibile
- creare programare
- modificare programare
- anulare programare
- confirmare vizuala in raspunsul API
- integrare optionala pentru notificari pe email
- lista printabila de programari
- export Excel

### Stack Tehnic
- Java 21
- Spring Boot
- Spring Web
- Spring Data JPA
- H2 Database
- Maven
- Apache POI
- springdoc OpenAPI / Swagger UI

### Structura Proiectului
- `controller` - endpoint-uri REST
- `service` - logica de business
- `repository` - acces la baza de date
- `entity` - modelele persistente
- `dto` - modele de request si response
- `exception` - tratarea centralizata a erorilor
- `mapper` - mapare intre entitati si DTO-uri

### Endpoint-uri Principale
- `GET /`
- `PUT /api/admin/configuration`
- `GET /api/admin/configuration`
- `GET /api/slots`
- `POST /api/appointments`
- `GET /api/appointments/{id}`
- `GET /api/appointments/by-employee/{employeeCode}`
- `PUT /api/appointments/{id}`
- `DELETE /api/appointments/{id}`
- `GET /api/reports/appointments`
- `GET /api/reports/appointments/export`

### Rulare Locala
1. Deschide proiectul in IntelliJ IDEA ca proiect Maven.
2. Foloseste JDK 21.
3. Da reload la dependintele Maven.
4. Ruleaza `VisionSlotApplication`.

URL-uri utile:
- `http://localhost:8080/`
- `http://localhost:8080/swagger-ui.html`
- `http://localhost:8080/h2-console`

### Stadiul Actual
Implementat:
- arhitectura backend
- reguli de programare
- fluxurile de creare, modificare si anulare
- exportul de rapoarte
- teste de integrare pentru fluxurile principale

Ce mai trebuie pentru produsul complet:
- interfata frontend
- autentificare si autorizare
- baza de date de productie
- configurare SMTP reala
- setup de deploy
- reguli suplimentare de business validate de stakeholderi
