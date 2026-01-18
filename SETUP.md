# Project Setup & Infrastructure Guide

This document provides step-by-step instructions for developers to set up the infrastructure, environment variables, and security keys required to run the **FlowerPlus** backend.

## 1. Prerequisites

- **Java Development Kit (JDK) 25**
- **Docker & Docker Compose**
- **OpenSSL** (for generating keys)
- **Maven** (optional, wrapper is provided)

---

## 2. Infrastructure Setup (Docker)

We use Docker Compose to run essential services: **PostgreSQL**, **RabbitMQ**, **Redis**, and **MailDev**.

### Start Services
Run the following command in the project root:

```bash
docker-compose up -d
```

### Services Overview
- **PostgreSQL**: `localhost:5432` 
  - **IMPORTANT**: The values `thang`/`123456` in configuration files are **PLACEHOLDERS ONLY**.
  - **DO NOT** use these defaults. You **MUST** change the `POSTGRES_USER` and `POSTGRES_PASSWORD` in your `.env` file or environment variables to something secure and unique to your local setup.
- **RabbitMQ**: 
  - AMQP: `localhost:5672`
  - Management UI: `http://localhost:15672`
- **Redis**: `localhost:6379`
- **MailDev** (SMTP Server):
  - SMTP: `localhost:1025`
  - Web UI: `http://localhost:1080` (Use this to view sent emails locally)

---

## 3. Security Keys (RSA - PKCS#8)

The application uses RSA keys for JWT signing and verification. You must generate these keys and place them in the correct directory.

### Step 3.1: Create Directory
Create the `certs` folder inside resources:
```bash
mkdir -p src/main/resources/certs
```

### Step 3.2: Generate Keys using OpenSSL
Run the following commands to generate the **Private Key** (PKCS#8) and **Public Key** inside the `src/main/resources/certs` directory.

**1. Generate Private Key (PKCS#8 format):**
```bash
openssl genpkey -algorithm RSA -out src/main/resources/certs/private_key.pem -pkeyopt rsa_keygen_bits:2048
```
*Verify the file content starts with `-----BEGIN PRIVATE KEY-----`.*

**2. Generate Public Key:**
```bash
openssl rsa -pubout -in src/main/resources/certs/private_key.pem -out src/main/resources/certs/public_key.pem
```
*Verify the file content starts with `-----BEGIN PUBLIC KEY-----`.*

---

## 4. Environment Variables

Create environment variables in your IDE run configuration or a `.env` file.

**security Warning**: The defaults listed below (`thang`, `123456`) are for illustrative purposes only. **DO NOT USE THEM**. Set your own strong username and password.

| Variable Name | Description | Example (Change These!) |
| :--- | :--- | :--- |
| **Database** | | |
| `POSTGRES_USER` | DB Username | `my_secure_user` |
| `POSTGRES_PASSWORD` | DB Password | `my_strong_password` |
| **Cloudinary** (Images) | | |
| `CLOUDINARY_CLOUD_NAME` | Cloud Name | `your_cloud_name` |
| `CLOUDINARY_API_KEY` | API Key | `your_api_key` |
| `CLOUDINARY_API_SECRET` | API Secret | `your_api_secret` |
| **Google OAuth2** | | |
| `GOOGLE_CLIENT_ID` | OAuth2 Client ID | `your_google_client_id` |
| `GOOGLE_CLIENT_SECRET` | OAuth2 Client Secret | `your_google_client_secret` |

---

## 5. Database Migration (Flyway)

Ref: `resources/db/migration`

Flyway is enabled by default to manage database schemas.
- **Auto-migrate**: Enabled on startup.
- **Baseline**: Configured to baseline on migrate (version 0) to prevent errors if the DB is not empty.
- **Locations**: SQL files are located in `src/main/resources/db/migration`.

**Note**: If you modify an existing migration file after it has been applied, Flyway will throw a checksum error. In the `dev` profile (`application-dev.yml`), `spring.flyway.validate-on-migrate` is set to `true`. Avoid changing applied migration files; create a new version instead.

---

## 6. How to Run

### Via Maven Wrapper
```bash
./mvnw spring-boot:run
```

### Via IDE (IntelliJ/Eclipse)
1. Reload Maven dependencies.
2. Ensure the `dev` profile is active (default in `pom.xml`).
3. Run `FlowerplusApplication.java`.

---

## 7. Troubleshooting

- **Check Keys**: If you see "Invalid KeySpec" or decoding errors, ensure your private key doesn't have extra headers like `-----BEGIN RSA PRIVATE KEY-----` (PKCS#1). It MUST be `-----BEGIN PRIVATE KEY-----` (PKCS#8).
- **MailDev**: Emails sent by the app will not go to real addresses. Open `http://localhost:1080` to view them.
- **ShedLock**: If the application crashes on startup regarding `shedlock`, ensure the `shedlock` table exists (managed by Flyway) and the database time zone is synced.
