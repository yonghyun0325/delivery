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

고객은 음식점을 조회하고 주문·결제를 진행하며 리뷰를 작성하고, 

사장님은 음식점과 메뉴를 관리하고 주문을 처리하며 리뷰에 답글을 작성할 수 있습니다.

**Gemini API**로 메뉴 설명과 가게별 리뷰 요약을 자동 생성하고,

**Caffeine Cache**로 반복 조회되는 데이터의 성능을 개선하였습니다.

</div>

---

# 📑 목차

- [📌 프로젝트 소개](#-프로젝트-소개)
- [👥 팀원 및 역할](#-팀원-및-역할)
- [🛠 기술 스택](#-기술-스택)
- [🏗 시스템 아키텍처](#-시스템-아키텍처)
- [🏗 배포 인프라 아키텍처](#-배포-인프라-아키텍처)
- [🗄 ERD](#-erd)
- [📦 프로젝트 구조](#-프로젝트-구조)
- [✨ 주요 기능 및 구현](#-주요-기능-및-구현)
- [📡 API 엔드포인트](#-api-엔드포인트)
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
- AI 메뉴 설명 및 리뷰 요약 자동 생성
- Docker 및 AWS EC2 배포
- GitHub Actions 기반 CI/CD 구축

---

## 프로젝트 정보

|항목|내용|
|---|---|
|개발 기간|2026.07.03 ~ 2026.07.20|
|개발 인원|6명|
|개발 방식|Git Flow 기반 협업|
|Backend|Spring Boot|
|Database|PostgreSQL|
|Deployment|Docker + AWS EC2|

---

# 👥 팀원 및 역할

| 팀원 | 담당 도메인 |
|---|---|
| 이강석 | 회원·인증 |
| 정수민 | 음식점 |
| 임은택 | 메뉴·AI·CI/CD |
| 안예지 | 주문 |
| 송채영 | 결제·장바구니 |
| 이용현 | 리뷰·리뷰 답글·Docker·AWS EC2 |

---

# 🛠 기술 스택

| 구분 | 기술 |
|---|---|
| Backend | Java 17, Spring Boot, Spring Security, Spring Data JPA, Hibernate, Validation |
| Database | PostgreSQL |
| Cache | Caffeine Cache |
| AI | Google Gemini API |
| DevOps | Docker, Docker Compose, AWS EC2, GitHub Actions (CI/CD) |

---

# 🏗 시스템 아키텍처

<img width="5015" height="1425" alt="Distributed Order Service-2026-07-16-044614" src="https://github.com/user-attachments/assets/0c4ec884-cd7a-4cb5-aa78-6d50a8f1d541" />

---

# 🏗 배포 인프라 아키텍처

<img width="1289" height="715" alt="스크린샷 2026-07-06 오후 12 08 15" src="https://github.com/user-attachments/assets/468237bf-8ae5-4c0b-bdba-2536b21627aa" />


- GitHub Actions가 Push마다 빌드·테스트를 검증하고, `main` 반영 시 SSH로 EC2에 접속해 Docker Compose로 재배포합니다.
- Security Group으로 `80`(전체) · `22`(팀 IP) · `5432`(내부 전용) 외 포트를 차단합니다.

---

# 🗄 ERD

<img width="1920" height="1080" alt="6" src="https://github.com/user-attachments/assets/ef288773-5e90-4b16-b549-9d91e973b43a" />


### 도메인 간 관계

```text
User
 ├── 1 : N Store
 ├── 1 : N Order
 ├── 1 : N Review
 └── 1 : N Cart

Store
 ├── N : 1 Category
 ├── N : 1 Region
 ├── 1 : N Menu
 └── 1 : N Order

Menu
 └── N : 1 Store

Cart
 └── 1 : N CartItem

Order
 ├── 1 : N OrderItem
 ├── 1 : 1 Payment
 └── 1 : 1 Review

Review
 └── 1 : 1 ReviewReply

AiRequestLog
 └── (감사 로그, 특정 도메인 FK 없음)
```

---

# 📦 프로젝트 구조

```text
domain
 ├── user     (회원, 인증/JWT 포함)
 ├── store    (가게, 카테고리, 지역 포함)
 ├── menu
 ├── cart
 ├── order
 ├── payment
 ├── review
 ├── reviewreply
 └── ai        (메뉴 설명 생성, 리뷰 요약, 요청 로그)
```

각 도메인은 `Controller → Service → Repository → Entity → DTO` 구조를 동일하게 유지하여 높은 응집도와 낮은 결합도를 유지하였습니다.

> `auth`는 별도 패키지가 아니라 `user` 도메인 안에 포함되어 있고, `category`·`region`도 `store` 도메인 안에 포함되어 있습니다.

---

# ✨ 주요 기능 및 구현

| 카테고리 | 핵심 기능 | 구현 기술 |
|---|---|---|
| **User·인증** | 회원가입/로그인/토큰 재발급/로그아웃 | Spring Security + JWT, Role(CUSTOMER/OWNER/MANAGER/MASTER) 기반 인가, Caffeine Cache로 사용자 정보·Refresh Token·블랙리스트 관리 |
| **Store** | 가게 CRUD, 영업 시작·종료, 카테고리·지역 조건 조회 | Role + 실제 자원 소유권(`Store.userId`) 이중 검증 |
| **Menu** | 일반 메뉴 등록, AI 메뉴 설명 생성 | Gemini API 호출과 DB 저장 트랜잭션 분리(`Propagation.NOT_SUPPORTED` + `TransactionTemplate`) |
| **Cart** | 장바구니 담기·수정·삭제 | 단일 가게 정책(다른 가게 메뉴 추가 시 거부), 메뉴명·가격 스냅샷 저장 |
| **Order** | 주문 생성 및 상태 전이 | `REQUESTED → ACCEPTED → COOKING → DELIVERING → DELIVERED → COMPLETED` 상태 머신, Specification 기반 동적 검색 |
| **Payment** | 결제 처리 및 취소 | 주문 상태와 연동된 결제 상태 관리 |
| **Review** | 리뷰 CRUD, 평균 평점 | 주문 완료 후 작성·주문당 1개 제한, 등록/수정/삭제 시 평균 평점 자동 갱신 |
| **Review Reply** | 사장님 답글 | 실제 가게 소유자 검증, 리뷰당 답글 1개 제한 |
| **AI** | 메뉴 설명 생성, 가게별 리뷰 요약, 관리자 AI 요청 로그 조회 | Gemini API, 리뷰 10개 이상 가게 대상 스케줄러 기반 요약 캐싱, MANAGER/MASTER 전용 감사 로그 |

---

# 📡 API 엔드포인트

주요 엔드포인트만 정리했습니다. 전체 목록은 실행 후 Swagger(`/swagger-ui/index.html`)에서 확인할 수 있습니다.

| Resource | Method | URI | 설명 | 권한 |
|---|---|---|---|---|
| 인증 | POST | `/api/v1/auth` | 회원가입 | 공개 |
| | POST | `/api/v1/auth/login` | 로그인 | 공개 |
| | POST | `/api/v1/auth/refresh` | Access Token 재발급 | 로그인 필요 |
| | POST | `/api/v1/auth/logout` | 로그아웃 | 로그인 필요 |
| 가게 | POST | `/api/v1/stores` | 가게 등록 | OWNER |
| | GET | `/api/v1/stores` | 가게 목록 조회 | 공개 |
| | PATCH | `/api/v1/stores/{storeId}/status` | 영업 상태 변경 | OWNER 본인 소유 |
| 메뉴 | POST | `/api/v1/stores/{storeId}/menus` | 메뉴 등록(AI 설명 생성 포함) | OWNER 본인 소유 |
| | GET | `/api/v1/menus` | 전체 메뉴 검색 | 로그인 필요 |
| 장바구니 | POST | `/api/v1/carts/items` | 장바구니에 메뉴 담기 | CUSTOMER |
| 주문 | POST | `/api/v1/orders` | 주문 생성 | CUSTOMER |
| | PATCH | `/api/v1/stores/{storeId}/orders/{orderId}/accept` | 주문 수락 | OWNER 본인 소유 |
| | PATCH | `/api/v1/orders/{orderId}/complete` | 주문 완료 처리 | CUSTOMER 본인 |
| 결제 | GET | `/api/v1/payments/me` | 내 결제 내역 조회 | CUSTOMER |
| | PATCH | `/api/v1/payments/{paymentId}/cancel` | 결제 취소 | CUSTOMER 본인 |
| 리뷰 | POST | `/api/v1/reviews` | 리뷰 작성 | CUSTOMER |
| | GET | `/api/v1/stores/{storeId}/reviews` | 가게 리뷰 목록 조회 | 공개 |
| 리뷰 답글 | POST | `/api/v1/reviews/{reviewId}/replies` | 답글 작성 | OWNER 본인 소유 |
| AI | GET | `/api/v1/ai-logs` | AI 요청 로그 조회 | MANAGER/MASTER |
| | GET | `/api/v1/stores/{storeId}/review-summary` | 가게 리뷰 요약 조회 | 공개 |

---

# 🧪 테스트

## Unit Test

Mockito·JUnit5 기반으로 서비스 계층의 비즈니스 로직을 격리해서 검증합니다. (예: `UserServiceUnitTest`, `MenuServiceTest`)

- 정상 동작 / 예외 처리 / 권한 검증 / 상태 변경

## Integration Test

Testcontainers로 실제 PostgreSQL 컨테이너를 띄워 트랜잭션 경계와 실제 시큐리티 필터 체인까지 검증합니다.

- `MenuServiceIntegrationTest` — AI 호출 실패 시 메뉴 미저장, 실패 로그는 `REQUIRES_NEW`로 별도 커밋되는지 검증
- `MenuControllerIntegrationTest` / `AiLogControllerIntegrationTest` — 실제 필터 체인 기반 역할·소유권 검증
- `MenuStoreCascadeIntegrationTest` — 가게 삭제 시 소속 메뉴 cascade 소프트 삭제 검증
- `ReviewSummarySchedulerIntegrationTest` — 리뷰 요약 배치 생성/스킵/재생성 흐름 검증

---

## End-to-End Test

```
회원가입 → 로그인 → 가게 생성 → 메뉴 생성 → 장바구니 → 주문 → 결제 → 리뷰 → 답글 → AI 로그 → 리뷰 요약
```

---

# 📈 성능 개선

| 구분 | 흐름 |
|---|---|
| Before | `Request → Database → Response` |
| After | `Request → Caffeine Cache → (Hit) Response` / `(Miss) → Database → Response` |

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

### 1. 필수 도구 설치

- **JDK**: 17 버전
- **Gradle**: Gradle Wrapper 사용 권장 (별도 설치 불필요)
- **Docker Desktop**: Docker Engine 및 Docker Compose 포함

### 2. 환경변수 설정

프로젝트 루트에 `.env` 파일을 생성합니다.

```env
DB_USER=
DB_PASSWORD=

JWT_ACCESS_SECRET=
JWT_REFRESH_SECRET=

ENCRYPT_KEY=
ENCRYPT_SALT=

GEMINI_API_KEY=
GEMINI_MODEL=gemini-3.5-flash
GEMINI_BASE_URL=https://generativelanguage.googleapis.com
```

> `.env` 파일과 비밀키는 Git에 커밋하지 않습니다.

### 3. 실행

```bash
git clone <repo-url>
docker-compose up -d
./gradlew bootRun
```

실행 후 Swagger에서 API 명세를 확인할 수 있습니다: `http://localhost:8080/swagger-ui/index.html`

---

# 📚 프로젝트 회고

이번 프로젝트를 통해

- Spring Boot 기반 REST API 설계
- JWT 인증 및 권한 관리
- 도메인 중심 설계
- Docker 및 AWS EC2 배포
- GitHub Actions CI/CD
- AI API 연동
- Caffeine Cache 적용

등 실제 서비스 개발에 필요한 기술을 경험할 수 있었습니다.

---

# 🔮 향후 개발 계획

- Redis 캐시 도입을 통한 다중 서버 환경 대응 및 반복 조회 성능 개선
- 주문·결제 동시 요청에 대한 락과 멱등성 강화
- 테스트 커버리지 확대 및 통합 테스트 보강
- Kafka 기반 이벤트 처리로 도메인 간 결합도 완화
- 트래픽 증가에 대비한 MSA 전환 및 Kubernetes 배포 검토
- Prometheus·Grafana 기반 모니터링 도입
- 운영 환경의 로그 수집·검색 체계 구축
- 데이터베이스 마이그레이션 도구 도입

---

<div align="center">

### ⭐ Thanks for visiting.

**Happy Coding!**

</div>
