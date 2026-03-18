![Banner](https://raw.githubusercontent.com/Vishnu-Yadav0/Revshop-payment-service/main/banner.png)

# 💳 RevShop — Payment Service

Handling secure transaction simulations, credit/debit card processing, and integrated wallet verification with OTP-based security.

[![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square&logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen?style=flat-square&logo=springboot)](https://spring.io/projects/spring-boot)
[![Twilio](https://img.shields.io/badge/Twilio-OTP%20Verification-red?style=flat-square&logo=twilio)](https://www.twilio.com/)
[![Docker](https://img.shields.io/badge/Docker-Containerized-blue?style=flat-square&logo=docker)](https://www.docker.com/)

---

## Features

- **Multi-Payment Support:** Simulated Credit/Debit Card, COD, and Wallet payments.
- **Security Check:** Twilio-powered OTP verification for high-value wallet transactions.
- **Transaction Logging:** Maintains persistent records of all platform payments.
- **Failure Handling:** Returns granular result codes to the Order service to handle retries or cancellations.

## Integrations

- **Twilio AI:** SMS delivery for transaction verification.
- **Order Service:** Callback mechanism for payment completion.
- **MySQL:** Reliable ledger for payment events.

---

## RevShop Microservices Repos

- 🌐 [Front-end](https://github.com/Vishnu-Yadav0/Revshop-frontend)
- 🤖 [Revshop AI assistant](https://github.com/Vishnu-Yadav0/Revshop-ai-chat-service)
- 👤 [Identity Provider](https://github.com/Vishnu-Yadav0/Revshop-user-service)

