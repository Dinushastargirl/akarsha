# Akarsha Product Audit

## Executive Summary
The Akarsha platform currently suffers from a fractured architecture. A highly robust, production-grade Spring Boot Java backend with 9 comprehensive database migration schemas (handling billing, appointments, staff, customers, and time-off) was abandoned mid-flight in favor of a Vercel Serverless (Next.js/Prisma) rewrite. This rewrite was initiated solely due to deployment issues on Railway, throwing away 90% of the functional backend architecture. 

**Recommendation:** Immediately halt the Vercel Serverless rewrite. Revert to and stabilize the Spring Boot backend. Fix the deployment pipeline rather than rewriting the application architecture.

## Current Architecture
* **Frontend:** React + Vite + TailwindCSS. Configured for Vercel deployment. Good foundation, supports internationalization (en, si_lk, ta_lk).
* **Backend (Original - Recommended):** Java Spring Boot 3, PostgreSQL, Flyway, Spring Security, JWT. Contains extensive multi-tenant architecture.
* **Backend (New - Abandoned):** Vercel Serverless API routes with Prisma. Extremely shallow, missing 90% of domain models (Billing, Staff Time Off, Services Pricing).
* **Database:** Neon PostgreSQL (currently configured via Prisma in frontend).

## Problems Found
1. **Architectural Fracture:** The project is split between a robust Java backend and a shallow Serverless backend due to a reactive decision to abandon Java over deployment errors.
2. **Missing Super Admin:** No overarching Super Admin platform exists for managing tenants, subscriptions, or feature flags.
3. **Tenant Isolation:** Enforced at the Prisma/JPA layer via `tenantId`, but needs rigorous testing to ensure zero cross-tenant leakage.
4. **AI Receptionist:** Currently a placeholder concept. Needs full integration with real database entities and conversation engines.
5. **Customer Experience:** No standalone booking portal for end-customers exists; everything is currently within the Salon Operating System workspace.

## Reusable Components
* **Frontend UI Foundation:** The Tailwind/Lucide-React design system and responsive layout.
* **Localization System:** The i18n implementation supporting Sinhala, Tamil, and English.
* **Java Domain Models:** The extensive Flyway migrations (V1 to V9) containing the true relational architecture.
* **Authentication Flow:** JWT-based auth structure is solid, though needs strict separation of roles.

## Missing Functionality
* Super Admin Platform (Platform-level analytics, billing, salon suspension).
* Customer-facing Booking Portal (Mobile-first booking engine without login).
* AI Orchestrator (NLP parsing, language detection between Singlish/Tanglish/Sinhala/Tamil/English).
* WhatsApp Cloud API Webhooks.
* Subscription/Feature Flag Enforcement.

## Security & Deployment Issues
* **Deployment:** Java backend deployment pipeline needs to be correctly containerized (Docker) and deployed to a robust provider (e.g., Render, AWS, or Railway properly configured) to prevent build failures.
* **Environment Variables:** Mixed `.env` configuration across Vercel and Spring Boot requires standardization.
* **Authorization:** Role-based access control (RBAC) needs strict enforcement on the backend endpoints, not just UI hiding.

## Recommended Architecture
```text
[ Vercel - Frontend ]
      ├── /admin     (Super Admin Portal)
      ├── /salon     (Salon Operating System)
      └── /book      (Customer Booking Experience)
             |
[ AWS / Render / Railway - Backend ]
      └── Spring Boot API Server (Java 17)
             ├── JWT Auth Filter (Tenant Scoped)
             ├── Core Controllers (Appointments, Billing, AI)
             ├── WhatsApp Webhook Listener
             └── AI Orchestrator Layer (LLM Integration)
             |
[ Neon - Database ]
      └── PostgreSQL (Multi-tenant via tenant_id indexing)
```

## Proposed Implementation Phases

### Phase 1: Audit + Architecture Stabilization
Revert to the Java Spring Boot backend. Delete the Vercel API routes (`frontend/api/`). Update Spring Boot deployment configurations (Dockerize) to ensure reliable production deployments.

### Phase 2: Multi-tenant Foundation + Super Admin
Ensure strict tenant isolation in Java. Build the Super Admin UI to manage salons, subscriptions, and feature flags.

### Phase 3: Salon Operating System UX Overhaul
Flesh out the Dashboard, Appointments, Customers, Services, Staff, and Billing pages in the React frontend, connecting them to the robust Java endpoints.

### Phase 4: Customer-facing Booking Experience
Build the public `/book/:salonId` flow for end customers to view services and book appointments mobile-first.

### Phase 5: AI Conversation Engine & Languages
Build the Java AI orchestrator capable of detecting the 5 languages (English, සිංහල, தமிழ், Singlish, Tanglish) and querying the database safely.

### Phase 6: WhatsApp Integration & Unified Chat
Integrate WhatsApp Cloud API webhooks into the Java backend. Unify Web Chat and WhatsApp into a single conversation history per customer.

### Phase 7: Landing Page & Security Hardening
Build the public marketing site. Perform rigorous cross-tenant penetration testing. Write automated tests for booking conflicts and API authorization.
