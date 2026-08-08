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
- Customer Engagement & AI Assistant (Web Chat & WhatsApp)

## Configuration
The following environment variables can be configured:

- `AKARSHA_WHATSAPP_TOKEN`: WhatsApp Webhook Verification Token (Default: `akarsha_verify_123`)
- `AKARSHA_WHATSAPP_SECRET`: Meta App Secret for Signature Validation (Default: `dummy_secret`)
- `AKARSHA_WHATSAPP_PHONE_ID`: Meta Phone Number ID (Default: `test_phone_id`)
- `AKARSHA_WHATSAPP_ACCESS_TOKEN`: Meta Graph API Access Token (Default: `test_access_token`)
- `AKARSHA_AI_PROVIDER`: The AI provider to use (`mock`, `openai`, `anthropic`). (Default: `mock`)

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

To run the backend with the test data and the H2 database, use the `demo` profile and set the port to 8090:

```bash
cd backend
$env:SPRING_PROFILES_ACTIVE="demo"
$env:PORT="8090"
mvn spring-boot:run
```
*(This starts the backend on `http://localhost:8090` to avoid conflicts with other applications)*

### Starting the Demo Frontend

In a separate terminal, start the Vite development server:

```bash
cd frontend
npm install
npm run dev
```
*(This starts the frontend on `http://localhost:5173`. It is configured to automatically communicate with the backend on port 8090)*

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
