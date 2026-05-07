# Ophthalmology Scheduling Backend

Backend Java pentru programari la consultatii oftalmologice.

## Tehnologii alese

- Java 21: limbajul si versiunea LTS folosita pentru backend.
- Maven: build, dependinte si generare `.jar`.
- Spring Boot 3.5.x: porneste serverul web si expune API-ul REST.
- Spring JDBC: executa SQL direct catre Microsoft SQL Server, fara Hibernate/JPA.
- Microsoft SQL Server JDBC Driver: driverul prin care Java se conecteaza la SQL Server.
- Flyway: ruleaza automat scripturile SQL din `src/main/resources/db/migration`.
- Notification API: backend-ul Java cere serviciului Node.js sa trimita emailurile.
- Apache POI: genereaza fisiere Excel `.xlsx`.

## Configurare locala

Fisierul principal de configurare este:

`src/main/resources/application.properties`

Setari importante:

```properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=OphthalmologyScheduling;encrypt=true;trustServerCertificate=true
spring.datasource.username=sa
spring.datasource.password=ChangeThisPassword

app.frontend.base-url=http://localhost:3000
app.cors.allowed-origins=http://localhost:3000

notification.api.enabled=false
notification.api.url=https://garotodeprograma.freddydanilo.com/api/vision-slot
notification.api.token=

app.admin.authorization-enabled=false
app.admin.user-header=X-Authenticated-User
app.admin.allowed-users=
```

Emailurile sunt trimise prin serviciul Node.js al colegului:

```http
POST https://garotodeprograma.freddydanilo.com/api/vision-slot
Authorization: Bearer <token>
Content-Type: application/json

{
  "title": "Email Subject",
  "recipient": "user@example.com",
  "body": "Your email content"
}
```

In dezvoltare, `notification.api.enabled=false` permite testarea fara trimitere reala de email.
Pentru integrare, se seteaza `notification.api.enabled=true` si se completeaza `notification.api.token` cu tokenul primit de la coleg.
Tokenul nu trebuie pus in frontend, pentru ca ar ajunge vizibil in browser.

Pentru zona admin, in dezvoltare `app.admin.authorization-enabled=false` lasa endpointurile admin accesibile.
In companie se seteaza `app.admin.authorization-enabled=true`, iar infrastructura cu Active Directory trebuie sa trimita userul intr-un header intern, de exemplu `X-Authenticated-User`.
Adminii acceptati se trec in `app.admin.allowed-users`, separati prin virgula.

## Rulare

Este necesar Maven 3.6.3+.

```powershell
mvn spring-boot:run
```

Build `.jar`:

```powershell
mvn clean package
```

Rulare `.jar`:

```powershell
java -jar target/ophthalmology-scheduling-0.0.1-SNAPSHOT.jar
```

## Endpoint-uri

Verificare aplicatie:

```http
GET /api/health
```

Campania publicata si deschisa pentru programari:

```http
GET /api/campaigns/current
```

Sloturi disponibile:

```http
GET /api/slots/available?from=2026-02-01&to=2026-02-15
```

Sunt returnate doar sloturile dintr-o campanie `PUBLISHED`, in perioada in care programarile sunt permise.

Creare programare:

```http
POST /api/appointments
Content-Type: application/json

{
  "slotId": 1,
  "employeeNumber": "12345",
  "fullName": "Popescu Ana",
  "email": "ana.popescu@company.local"
}
```

Raspunsul include si `cancelToken`, folosit pentru linkul de modificare/anulare.

Modificare programare prin token:

```http
PUT /api/appointments/token/11111111-1111-1111-1111-111111111111
Content-Type: application/json

{
  "newSlotId": 2
}
```

Anulare programare prin token:

```http
DELETE /api/appointments/token/11111111-1111-1111-1111-111111111111
```

Modificare programare dupa id, doar admin:

```http
PUT /api/admin/appointments/1
Content-Type: application/json

{
  "newSlotId": 2
}
```

Anulare programare dupa id, doar admin:

```http
DELETE /api/admin/appointments/1
```

Creare campanie admin, cu generare automata de sloturi:

```http
POST /api/admin/campaigns
Content-Type: application/json

{
  "name": "Consultatii oftalmologice februarie 2026",
  "bookingStartDate": "2026-02-01",
  "bookingEndDate": "2026-02-15",
  "consultationDays": [
    {
      "consultationDate": "2026-02-03",
      "startTime": "09:00",
      "endTime": "15:00",
      "lunchStart": "12:00",
      "lunchEnd": "12:30",
      "slotDurationMinutes": 5
    }
  ]
}
```

Listare campanii admin:

```http
GET /api/admin/campaigns
```

Publicare campanie:

```http
PUT /api/admin/campaigns/1/publish
```

Inchidere campanie:

```http
PUT /api/admin/campaigns/1/close
```

Export Excel:

```http
GET /api/admin/appointments/export.xlsx
```

Export Excel pentru o campanie:

```http
GET /api/admin/appointments/export.xlsx?campaignId=1
```

Raspunsul este un fisier `programari-oftalmologie.xlsx` cu data, ora, marca, nume si email.

## Reguli implementate

- Sloturile sunt generate automat din zile, ore, pauza si durata consultatiei.
- Sloturile care se suprapun cu pauza de masa nu sunt generate.
- Utilizatorii pot crea, modifica sau anula doar in fereastra `bookingStartDate` - `bookingEndDate`.
- Utilizatorii folosesc `cancelToken`; endpointurile cu `appointmentId` sunt doar pentru admin.
- O marca poate avea o singura programare activa intr-o campanie.
- Un slot poate avea o singura programare activa.
- Zona `/api/admin/**` are autorizare configurabila prin header de user preautentificat.

## Structura explicabila la prezentare

- `controller`: primeste cereri HTTP de la frontend.
- `service`: contine regulile aplicatiei.
- `repository`: comunica direct cu SQL Server.
- `dto`: obiectele trimise intre frontend si backend.
- `db/migration`: scripturile SQL versionate.

Flux simplu:

1. Frontend-ul trimite o cerere catre un endpoint REST.
2. Controllerul primeste cererea.
3. Service-ul verifica regulile de business.
4. Repository-ul citeste/scrie in SQL Server.
5. Service-ul trimite cererea de email catre Notification API, daca este activ.
6. Backend-ul raspunde catre frontend cu JSON sau fisier Excel.

## Observatii pentru urmatorii pasi

- Trebuie confirmat cu firma numele headerului trimis de infrastructura lor dupa preautentificarea Active Directory.
- Daca firma vrea grupuri AD in loc de lista simpla de useri admin, autorizarea admin trebuie extinsa.
- Dupa instalarea JDK si Maven, trebuie rulat `mvn clean package`.
