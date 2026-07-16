<div align="center">

# 🍽️ Delivery

### AI 기반 음식 주문 관리 플랫폼

회원가입부터 음식점 관리, 메뉴, 주문, 결제, 리뷰 및 답글,
AI 메뉴 생성까지 구현한 **Spring Boot 기반 팀 프로젝트**

<br>

![Java](https://img.shields.io/badge/Java-17-007396?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5.16-6DB33F?logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-JWT-6DB33F?logo=springsecurity&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-336791?logo=postgresql&logoColor=white)
![Caffeine](https://img.shields.io/badge/Cache-Caffeine-8B5CF6)
![Docker](https://img.shields.io/badge/Docker-2496ED?logo=docker&logoColor=white)
![AWS](https://img.shields.io/badge/AWS-EC2-FF9900?logo=amazonaws&logoColor=white)
![GitHub Actions](https://img.shields.io/badge/GitHub_Actions-CI-2088FF?logo=githubactions&logoColor=white)
![Gemini API](https://img.shields.io/badge/Google-Gemini_API-4285F4?logo=google&logoColor=white)

---

### 📌 프로젝트 개요

사용자와 음식점 사장님을 연결하는 **AI 기반 음식 주문 관리 플랫폼**입니다.

고객은 음식점을 조회하고 주문 및 결제를 진행하며 리뷰를 작성할 수 있고,

사장님은 음식점과 메뉴를 관리하고 주문을 처리하며 리뷰에 답글을 작성할 수 있습니다.

또한 **Gemini API**를 활용하여 메뉴 설명을 자동 생성하고,

**Caffeine Cache**를 적용하여 반복 조회되는 데이터의 성능을 개선하였습니다.

</div>

---

# 📑 목차

- [📌 프로젝트 소개](#-프로젝트-소개)
- [🎯 프로젝트 목표](#-프로젝트-목표)
- [🛠 기술 스택](#-기술-스택)
- [🏗 시스템 아키텍처](#-시스템-아키텍처)
- [🗄 ERD](#-erd)
- [📦 프로젝트 구조](#-프로젝트-구조)
- [✨ 주요 기능](#-주요-기능)
- [🔥 핵심 기술 구현](#-핵심-기술-구현)
- [🧪 테스트](#-테스트)
- [🚨 트러블슈팅](#-트러블슈팅)
- [📈 성능 개선](#-성능-개선)
- [🚀 실행 방법](#-실행-방법)
- [🤝 협업](#-협업)
- [📚 프로젝트 회고](#-프로젝트-회고)
- [🔮 향후 개발 계획](#-향후-개발-계획)

---

# 📌 프로젝트 소개

## 프로젝트 목표

- Spring Boot 기반 REST API 서버 구현
- 도메인 중심 패키지 구조 설계
- JWT 기반 인증 및 권한 관리
- AI 메뉴 설명 자동 생성
- Docker 및 AWS EC2 배포
- GitHub Actions 기반 CI 구축

---

## 프로젝트 정보

|항목|내용|
|---|---|
|개발 기간|2026.06 ~ 2026.07|
|개발 인원|6명|
|개발 방식|Git Flow 기반 협업|
|Backend|Spring Boot|
|Database|PostgreSQL|
|Deployment|Docker + AWS EC2|

---

# 🛠 기술 스택

## Backend

- Java 17
- Spring Boot
- Spring Security
- Spring Data JPA
- Hibernate
- Validation

## Database

- PostgreSQL

## Cache

- Caffeine Cache

## AI

- Google Gemini API

## DevOps

- Docker
- Docker Compose
- AWS EC2
- GitHub Actions

---

# 🏗 시스템 아키텍처

```text
                  Client

                     │

                     ▼

          Spring Security

               JWT Filter

                     │

                     ▼

              Controller

                     │

                     ▼

               Service

        ┌──────────┴──────────┐

        ▼                     ▼

 Caffeine Cache         PostgreSQL

        │

        ▼

    Gemini API
```

---

# 🗄 ERD

> ERD 이미지 삽입

### 도메인 간 관계

```text
User
 ├── 1 : N Store
 ├── 1 : N Order
 ├── 1 : N Review

Store
 ├── 1 : N Menu
 ├── 1 : N Order

Menu
 └── N : 1 Store

Order
 ├── 1 : N OrderItem
 ├── 1 : 1 Payment
 └── 1 : 1 Review

Review
 └── 1 : 1 ReviewReply
```

---

# 📦 프로젝트 구조

```text
domain
 ├── auth
 ├── user
 ├── category
 ├── region
 ├── store
 ├── menu
 ├── cart
 ├── order
 ├── payment
 ├── review
 ├── reviewreply
 └── ai
```

각 도메인은

Controller

↓

Service

↓

Repository

↓

Entity

↓

DTO

구조를 동일하게 유지하여 높은 응집도와 낮은 결합도를 유지하였습니다.

---

# ✨ 주요 기능

|도메인|기능|
|------|------|
|User|회원가입 / 로그인 / JWT|
|Store|가게 CRUD / 영업 시작·종료|
|Menu|일반 메뉴 / AI 메뉴 생성|
|Cart|장바구니|
|Order|주문 생성 및 상태 변경|
|Payment|결제|
|Review|리뷰 CRUD / 평균 평점|
|Review Reply|사장님 답글|
|AI|Gemini 메뉴 설명 생성|

---

# 🔥 핵심 기술 구현

<details>
<summary><b>🔐 JWT 인증 및 권한 관리</b></summary>

- Spring Security 기반 인증
- JWT Access Token 발급
- Role(USER / OWNER / MANAGER / MASTER)
- 실제 자원 소유권 검증

</details>

<details>
<summary><b>⚡ Caffeine Cache</b></summary>

현재 프로젝트는 **모놀리식 단일 서버 구조**이므로

Redis 대신 JVM 메모리 기반 **Caffeine Cache**를 적용하여

반복 조회되는 평균 평점과 리뷰 조회 성능을 개선하였습니다.

</details>

<details>
<summary><b>🤖 Gemini API</b></summary>

메뉴 이름과 Prompt를 이용하여

AI가 자동으로 메뉴 설명을 생성합니다.

</details>

<details>
<summary><b>⭐ Review</b></summary>

- 주문 완료 후 리뷰 작성
- 주문당 리뷰 1개
- 평균 평점 자동 계산

</details>

<details>
<summary><b>💬 Review Reply</b></summary>

- OWNER 권한 검증
- 실제 가게 소유자 검증
- 리뷰당 답글 1개 제한

</details>

---

# 🧪 테스트

## Unit Test

- Mockito
- JUnit5

검증 내용

- 정상 동작
- 예외 처리
- 권한 검증
- 상태 변경

---

## End-to-End Test

```text
회원가입

↓

로그인

↓

가게 생성

↓

메뉴 생성

↓

장바구니

↓

주문

↓

결제

↓

리뷰

↓

답글

↓

AI 로그

↓

리뷰 요약
```

---

# 📈 성능 개선

## Before

```text
Request

↓

Database

↓

Response
```

## After

```text
Request

↓

Caffeine Cache

↓

Response

(Cache Miss)

↓

Database
```

---

# 🚨 트러블슈팅

|문제|해결|
|------|------|
|리뷰 작성 검증|주문 완료 여부 검증|
|평균 평점 미갱신|StoreService 연동|
|답글 권한 문제|실제 Owner 검증|
|공개 API 접근|permitAll 적용|

---

# 🤝 협업

- Git Flow
- Pull Request
- Code Review
- Notion
- Swagger
- ERDCloud

---

# 🚀 실행 방법

```bash
git clone

docker-compose up -d

./gradlew bootRun
```

Swagger

```
http://localhost:8080/swagger-ui/index.html
```

---

# 📚 프로젝트 회고

이번 프로젝트를 통해

- Spring Boot 기반 REST API 설계
- JWT 인증 및 권한 관리
- 도메인 중심 설계
- Docker 및 AWS EC2 배포
- GitHub Actions CI
- AI API 연동
- Caffeine Cache 적용

등 실제 서비스 개발에 필요한 기술을 경험할 수 있었습니다.

---

# 🔮 향후 개발 계획

- Redis Cache 적용
- Refresh Token 관리
- Kafka 이벤트 기반 처리
- MSA 전환
- Kubernetes 배포
- Prometheus + Grafana 모니터링

---

<div align="center">

### ⭐ Thanks for visiting.

**Happy Coding!**

</div>
