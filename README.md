# Akarsha Salon Management Platform

Akarsha is a modern, premium, multi-tenant Software as a Service (SaaS) platform built for salon businesses. It handles everything from appointment scheduling and customer management to staff operations, service configurations, and localized financial reporting.

## Architecture

- **Backend**: Spring Boot 3, Java 17, Spring Security (JWT), Spring Data JPA, Flyway Migrations
- **Frontend**: React 18, TypeScript, Vite, Tailwind CSS, i18next (English, Singlish, Tanglish)
- **Database**: PostgreSQL (Production), H2 (Testing/Demo)
- **Design Philosophy**: Human, Simple, Warm, Premium, Calm, Modern, Practical, Trustworthy

## Implemented Modules

- Multi-tenant data isolation
- JWT Authentication & Onboarding
- Strict Role-Based Access Control (RBAC: Owner, Manager, Receptionist, Staff)
- Real-time Dashboard & Operations Statistics
- Customer Profiles & Visit History
- Staff Availability, Time Off, & Scheduling
- Service Configuration & Pricing
- Conflict-Aware Appointment Booking
- Billing, Checkout & Invoicing
- Localized Financial Reports & Analytics

## Running the Application (Production)

### Database

The production application requires PostgreSQL.
Configure the `application.yml` in the backend to point to your PostgreSQL instance.

### Backend

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

### Frontend

```bash
cd frontend
npm install
npm run build
npm run preview
```

## Running the Demo Environment (H2 In-Memory)

A deterministic demo environment using an H2 database is provided. This allows you to explore the platform without setting up PostgreSQL. 

### Starting the Demo Backend

To run the backend with the test data and the H2 database, use the `test` profile:

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=test
```

*(Note: Windows users may need to set the environment variable explicitly: `$env:SPRING_PROFILES_ACTIVE="test"; mvn spring-boot:run`)*

### H2 TEST/DEMO CREDENTIALS

The demo environment automatically seeds the following users for the `alpha` salon tenant.

```text
Tenant:
alpha

SALON OWNER
Email: owner@alpha.com
Password: Owner123!

MANAGER
Email: manager@alpha.com
Password: Manager123!

RECEPTIONIST
Email: receptionist@alpha.com
Password: Receptionist123!

STAFF
Email: staff@alpha.com
Password: Staff123!
```

## Running Verification Tests

To verify the backend logic and security constraints:

```bash
cd backend
mvn clean test
```

*(This automatically uses the H2 test profile and runs 82+ end-to-end security and logic verifications)*

## Deployment Requirements

Ensure these environment variables are set in production:

- `DATABASE_URL`
- `DATABASE_USER`
- `DATABASE_PASSWORD`
- `JWT_SECRET` (Must be at least 256 bits)
