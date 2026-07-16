# 🍽️ Delivery

> **AI 기반 음식 주문 및 배달 플랫폼**

Spring Boot 기반으로 개발한 음식 주문 및 배달 관리 플랫폼입니다.

단순 CRUD 구현을 넘어 회원 인증, 음식점 및 메뉴 관리, 장바구니, 주문, 결제, 리뷰 및 답글, AI 메뉴 생성까지 실제 서비스에서 필요한 핵심 기능을 구현하였습니다.

또한 Docker와 AWS EC2를 활용한 배포 환경을 구축하고 GitHub Actions를 이용한 CI를 적용하여 실제 협업 환경을 경험하는 것을 목표로 프로젝트를 진행하였습니다.

---

# 📌 프로젝트 소개

Delivery는 고객과 음식점 사장님을 연결하는 음식 주문 플랫폼입니다.

고객은 음식점을 조회하고 메뉴를 주문하며 리뷰를 작성할 수 있고,

사장님은 음식점과 메뉴를 관리하며 주문을 처리하고 고객 리뷰에 답글을 작성할 수 있습니다.

관리자는 카테고리와 지역을 관리하며 AI 요청 로그를 조회할 수 있습니다.

프로젝트는 도메인 중심 패키지 구조(Domain Package Architecture)를 적용하여 각 기능의 책임을 명확하게 분리하였으며,

Spring Security와 JWT를 이용한 인증 및 권한 관리,

Gemini API를 이용한 AI 메뉴 생성,

Caffeine Cache를 이용한 조회 성능 개선,

Docker 및 AWS EC2 배포 환경까지 구축하였습니다.

---

# 🎯 프로젝트 목표

- 실제 음식 주문 플랫폼의 전체 비즈니스 흐름 구현
- Spring Boot 기반 REST API 설계
- Spring Security와 JWT 기반 인증 및 권한 관리
- AI를 활용한 메뉴 설명 자동 생성
- Docker 및 AWS EC2를 활용한 배포 환경 구축
- GitHub Actions 기반 CI 환경 구축
- 도메인 중심 설계를 통한 유지보수성 향상

---

# 🛠 기술 스택

## Backend

- Java 17
- Spring Boot 3
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

## Documentation

- Swagger(OpenAPI)
- ERDCloud
- Notion

---

# 🏗 시스템 아키텍처

```
                Client
                   │
                   ▼
        Spring Security (JWT)
                   │
                   ▼
            Controller Layer
                   │
                   ▼
             Service Layer
                   │
                   ▼
          Spring Data JPA
                   │
         ┌─────────┴─────────┐
         ▼                   ▼
 PostgreSQL          Caffeine Cache
                         │
                         ▼
                 Review Rating
                 Store Summary
```

### Architecture

- Spring Security와 JWT를 이용하여 사용자 인증을 처리합니다.
- Controller → Service → Repository 구조를 적용하여 계층별 책임을 분리하였습니다.
- PostgreSQL을 영속 데이터 저장소로 사용합니다.
- 반복 조회되는 데이터를 Caffeine Cache에 저장하여 조회 성능을 향상시켰습니다.
- Docker Compose를 이용하여 개발 환경을 구성하였습니다.
- AWS EC2를 이용하여 배포 환경을 구축하였습니다.
- GitHub Actions를 이용하여 CI를 적용하였습니다.

---

# 📂 프로젝트 구조

```
com.delivery
│
├── common
│
├── domain
│   ├── auth
│   ├── user
│   ├── category
│   ├── region
│   ├── store
│   ├── menu
│   ├── cart
│   ├── order
│   ├── payment
│   ├── review
│   ├── reviewreply
│   └── ai
│
├── global
│   ├── config
│   ├── exception
│   ├── security
│   └── util
│
└── DeliveryApplication
```

도메인 중심 패키지 구조를 적용하여 기능별 응집도를 높이고 유지보수성을 향상시켰습니다.

각 도메인은 Controller, Service, Repository, Entity, DTO를 독립적으로 관리하며 다른 도메인과의 결합도를 최소화하도록 설계하였습니다.

---

# 🗄 ERD

(ERD 이미지 삽입)

---

# ✨ 주요 기능

## 👤 사용자

- 회원가입
- 로그인
- JWT 인증
- 권한(Role) 관리

---

## 🏪 음식점

- 음식점 등록
- 음식점 수정
- 음식점 삭제
- 영업 시작 / 종료
- 평균 평점 조회

---

## 🍽 메뉴

- 일반 메뉴 등록
- AI 메뉴 생성
- 메뉴 수정
- 메뉴 삭제
- 메뉴 조회

---

## 🛒 장바구니

- 메뉴 담기
- 장바구니 조회
- 장바구니 수정
- 장바구니 삭제

---

## 📦 주문

- 주문 생성
- 주문 조회
- 주문 상태 변경
- 주문 완료

---

## 💳 결제

- 주문 결제
- 결제 조회

---

## ⭐ 리뷰

- 리뷰 작성
- 리뷰 수정
- 리뷰 삭제
- 음식점 리뷰 조회
- 평균 평점 조회

---

## 💬 리뷰 답글

- 사장님 답글 작성
- 답글 수정
- 답글 삭제
- 답글 조회

---

## 🤖 AI

- Gemini API 기반 메뉴 설명 생성
- AI 요청 로그 조회
- AI 리뷰 요약 조회

---

# 🔥 핵심 기술 구현

## 1️⃣ Spring Security와 JWT 기반 인증 및 권한 관리

프로젝트는 Spring Security와 JWT(JSON Web Token)를 기반으로 인증 및 인가를 구현하였습니다.

사용자가 로그인하면 JWT Access Token을 발급하고, 이후 모든 요청은 JwtRequestFilter를 통해 토큰을 검증하여 인증을 수행합니다.

또한 Spring Security의 Role 기반 권한 관리와 실제 자원 소유자 검증을 함께 적용하여 보안을 강화하였습니다.

### 구현 내용

- Spring Security AuthenticationManager 기반 로그인 인증
- JWT Access Token 발급 및 검증
- JwtRequestFilter를 이용한 요청 인증 처리
- CustomUserDetails를 통한 사용자 정보 관리
- Role(USER / OWNER / MANAGER / MASTER) 기반 접근 제어
- 실제 자원 소유권 검증

### 기술 선택 이유

현재 프로젝트는 **모놀리식 단일 서버 환경**으로 개발되었습니다.

Redis는 일반적으로 다중 서버 환경에서 Refresh Token 공유와 인증 상태 동기화를 위해 사용됩니다.

현재 프로젝트에서는 서버 간 데이터 정합성을 고려할 필요가 없었기 때문에 JWT 기반 인증 구조만 적용하여 시스템을 단순하게 유지하였습니다.

향후 MSA 환경으로 확장될 경우 Redis 기반 Refresh Token 저장 및 로그아웃 토큰 관리 기능을 적용할 계획입니다.

---

# 2️⃣ Caffeine Cache를 이용한 조회 성능 개선

리뷰 평균 평점과 음식점 정보는 반복적으로 조회되는 데이터입니다.

매 요청마다 Database를 조회하면 불필요한 I/O가 발생하기 때문에 Caffeine Cache를 적용하여 조회 성능을 개선하였습니다.

### 구현 내용

- 음식점 평균 평점 Cache
- 리뷰 조회 Cache
- 자동 만료 정책(Time To Live)
- CacheManager 기반 캐시 관리

### 적용 효과

- Database 조회 횟수 감소
- 평균 응답 속도 향상
- 반복 조회 성능 개선
- 애플리케이션 메모리 기반 빠른 조회

### 기술 선택 이유

현재 프로젝트는 모놀리식 단일 서버 환경이므로 별도의 캐시 서버를 구축할 필요가 없었습니다.

따라서 JVM 내부 메모리를 사용하는 Caffeine Cache를 적용하여 간단하면서도 높은 성능을 확보하였습니다.

향후 MSA 환경으로 확장될 경우 Redis Cache를 적용하여 여러 서버가 동일한 캐시를 공유할 계획입니다.

---

# 3️⃣ 주문 상태 전이(State Machine)

주문은 상태에 따라 수행 가능한 기능이 달라집니다.

잘못된 순서로 상태가 변경되지 않도록 주문 상태 전이 로직을 구현하였습니다.

```
REQUESTED
    │
    ▼
ACCEPTED
    │
    ▼
COOKING
    │
    ▼
DELIVERING
    │
    ▼
DELIVERED
    │
    ▼
COMPLETED
```

### 구현 내용

- 상태 변경 순서 검증
- 잘못된 상태 변경 차단
- OWNER 권한 검증
- CUSTOMER 완료 처리

### 적용 효과

- 비즈니스 규칙 보장
- 데이터 무결성 확보
- 주문 흐름 일관성 유지

---

# 4️⃣ AI 메뉴 설명 자동 생성

Google Gemini API를 이용하여 메뉴 설명을 자동 생성하도록 구현하였습니다.

사장님은 메뉴 이름만 입력하면 AI가 음식의 특징을 분석하여 메뉴 설명을 생성합니다.

### 구현 내용

- Gemini API 연동
- Prompt 기반 설명 생성
- AI 요청 로그 저장
- AI 생성 결과 조회

### 적용 효과

- 메뉴 등록 시간 단축
- 설명 작성 편의성 향상
- AI 활용 기능 경험

---

# 5️⃣ 리뷰 및 평균 평점 관리

리뷰 기능은 단순 CRUD가 아니라 실제 서비스에서 필요한 비즈니스 규칙을 적용하여 구현하였습니다.

### 구현 내용

- 주문 완료 고객만 리뷰 작성 가능
- 주문당 리뷰 1개 제한
- 리뷰 수정 및 삭제
- 음식점 리뷰 조회
- 평균 평점 계산
- Store 도메인과 평균 평점 연동

### 평균 평점 계산

ReviewRepository에서 AVG() 집계 함수를 이용하여 음식점 평균 평점을 계산하도록 구현하였습니다.

리뷰 등록, 수정, 삭제 시 Store 도메인과 연동하여 평균 평점을 자동으로 갱신하도록 구성하였습니다.

### 적용 효과

- 데이터 일관성 유지
- 최신 평균 평점 제공
- 비즈니스 규칙 보장

---

# 6️⃣ 리뷰 답글 기능

음식점 사장님이 고객 리뷰에 답글을 작성할 수 있도록 구현하였습니다.

리뷰당 하나의 답글만 등록할 수 있도록 설계하여 데이터 일관성을 유지하였습니다.

### 구현 내용

- OWNER 권한 검증
- 실제 가게 소유자 검증
- 리뷰당 답글 1개 제한
- 답글 수정
- 답글 삭제
- Swagger 인터페이스 분리

### 적용 효과

- 실제 서비스와 동일한 구조 구현
- 권한 기반 접근 제어
- 중복 데이터 방지

---

# 7️⃣ 예외 처리 및 공통 응답 구조

프로젝트 전체에서 동일한 응답 형식을 유지하기 위해 공통 응답 객체와 예외 처리 구조를 적용하였습니다.

### 구현 내용

- RestApiResponse
- GlobalExceptionHandler
- ErrorCode 인터페이스
- 도메인별 ErrorCode 관리
- BusinessException 구조 적용

### 적용 효과

- 일관된 API 응답
- 유지보수성 향상
- 프론트엔드 연동 편의성 증가

---

# 8️⃣ Docker 및 AWS EC2 배포

Docker를 이용하여 개발 환경을 컨테이너화하고 AWS EC2 환경에서 프로젝트를 배포하였습니다.

### 구현 내용

- Docker Compose
- PostgreSQL Container
- Spring Boot Container
- AWS EC2 배포
- GitHub Actions CI

### 적용 효과

- 동일한 개발 환경 제공
- 배포 자동화 기반 마련
- 협업 환경 개선

---

# 🧪 테스트

프로젝트의 안정성을 확보하기 위해 단위 테스트와 통합 테스트를 수행하였습니다.

---

## Unit Test

Mockito와 JUnit5를 이용하여 Service 계층의 비즈니스 로직을 검증하였습니다.

### 테스트 대상

- 회원가입 및 로그인
- 카테고리 관리
- 지역 관리
- 음식점 관리
- 메뉴 관리
- 장바구니
- 주문
- 결제
- 리뷰
- 리뷰 답글
- AI 기능

### 검증 내용

- 정상 동작
- 예외 발생
- 권한 검증
- 중복 데이터 검증
- 상태 변경 검증

---

## Integration Test

실제 사용자 흐름을 기준으로 API를 검증하기 위해 End-to-End 테스트 시나리오를 작성하였습니다.

### 테스트 시나리오

```
회원가입

↓

로그인

↓

카테고리 생성

↓

지역 생성

↓

가게 생성

↓

메뉴 생성

↓

장바구니

↓

주문

↓

주문 상태 변경

↓

결제

↓

리뷰

↓

리뷰 답글

↓

AI 로그

↓

리뷰 요약
```

### 권한 테스트

다음과 같은 권한 검증도 함께 수행하였습니다.

- CUSTOMER → 카테고리 생성 (403)
- CUSTOMER → 가게 생성 (403)
- CUSTOMER → 주문 수락 (403)
- OWNER → AI 로그 조회 (403)

실행 결과를 PASS / FAIL 형태로 출력하여 테스트 성공 여부를 쉽게 확인할 수 있도록 구성하였습니다.

---

# 🚨 트러블슈팅

프로젝트를 진행하면서 발생했던 주요 문제와 해결 과정을 정리하였습니다.

---

## 1. 주문 완료 이전에도 리뷰가 작성되는 문제

### 문제 상황

리뷰 등록 API 호출 시 주문 상태와 관계없이 리뷰가 등록되는 문제가 발생하였습니다.

---

### 원인

주문 존재 여부만 확인하고 실제 주문 상태를 검증하지 않았습니다.

---

### 해결 방법

- 주문 상태 확인
- DELIVERED / COMPLETED 상태 검증
- 주문자 본인 여부 확인
- 주문당 리뷰 1개 제한

---

### 결과

실제 주문을 완료한 고객만 리뷰를 작성할 수 있도록 개선하였습니다.

---

## 2. 리뷰 수정 후 평균 평점이 변경되지 않는 문제

### 문제 상황

리뷰 수정 및 삭제 이후에도 평균 평점이 이전 값으로 유지되는 문제가 발생하였습니다.

---

### 원인

평균 평점 계산 로직이 리뷰 등록(Create)에만 적용되어 있었습니다.

---

### 해결 방법

StoreService와 연동하여

- Create
- Update
- Delete

모든 시점에서 평균 평점을 다시 계산하도록 수정하였습니다.

---

### 결과

항상 최신 평균 평점을 유지하도록 개선하였습니다.

---

## 3. 실제 가게 사장님이 아닌 사용자도 답글 작성 가능

### 문제 상황

권한만 OWNER이면 다른 가게 리뷰에도 답글을 작성할 수 있었습니다.

---

### 원인

Role만 확인하고 실제 음식점 소유자 여부를 검증하지 않았습니다.

---

### 해결 방법

Store의 ownerId와 로그인 사용자의 ID를 비교하여 실제 소유자인 경우에만 답글 작성이 가능하도록 수정하였습니다.

---

### 결과

실제 자원 소유권 기반 권한 검증을 적용하였습니다.

---

## 4. 공개 API 접근 정책 문제

### 문제 상황

리뷰 목록과 평균 평점 조회가 로그인 없이 접근되지 않는 문제가 발생하였습니다.

---

### 원인

SecurityConfig에서 permitAll() 대상에 리뷰 API가 포함되지 않았습니다.

---

### 해결 방법

```
GET /stores/{storeId}/reviews

GET /stores/{storeId}/ratings
```

API를 permitAll 대상으로 추가하였습니다.

---

### 결과

회원가입 이전에도 리뷰와 평점을 확인할 수 있도록 개선하였습니다.

---

# 🤝 협업

GitHub Flow 전략을 기반으로 협업을 진행하였습니다.

---

## Branch 전략

```
main

↓

develop

↓

feature/*
```

각 팀원은 자신의 Feature Branch에서 개발을 진행한 후 Pull Request를 통해 develop 브랜치로 병합하였습니다.

---

## Pull Request

모든 기능은 PR을 통해 코드 리뷰를 진행하였습니다.

### Review 내용

- Coding Convention
- API 설계
- 예외 처리
- 권한 검증
- 성능 개선
- 테스트 코드

---

## Commit Convention

```
feat

fix

refactor

docs

test

style

chore
```

Convention을 통일하여 Git History를 관리하였습니다.

---

## 협업 도구

- GitHub
- Notion
- Discord
- ERDCloud
- Swagger

---

# 🚀 실행 방법

## 1. Repository Clone

```bash
git clone https://github.com/your-repository.git
```

---

## 2. Environment

```
.env
```

파일 생성

```
DB_HOST=

DB_PORT=

DB_NAME=

DB_USER=

DB_PASSWORD=

JWT_SECRET=

GEMINI_API_KEY=
```

---

## 3. Docker 실행

```bash
docker-compose up -d
```

---

## 4. Spring Boot 실행

```bash
./gradlew bootRun
```

---

## 5. Swagger

```
http://localhost:8080/swagger-ui/index.html
```

---

## 6. 테스트

```bash
./gradlew test
```

또는

```bash
./scripts/e2e-test.sh
```

End-to-End 통합 테스트를 통해 전체 사용자 시나리오를 검증할 수 있습니다.

---

# 📈 성능 개선

프로젝트를 개발하면서 단순히 기능 구현에 그치지 않고 조회 성능과 유지보수성을 함께 고려하였습니다.

---

## Caffeine Cache 적용

음식점 평균 평점과 리뷰 목록은 사용자들이 가장 많이 조회하는 데이터입니다.

매 요청마다 Database를 조회하면 불필요한 I/O가 발생하고 응답 속도가 저하될 수 있습니다.

현재 프로젝트는 **모놀리식(Monolithic) 단일 서버 구조**로 개발되었기 때문에 별도의 캐시 서버를 운영할 필요가 없다고 판단하였습니다.

이에 따라 JVM 메모리 기반의 **Caffeine Cache**를 적용하여 반복 조회되는 데이터를 캐싱하도록 구현하였습니다.

### 적용 대상

- 음식점 평균 평점
- 음식점 리뷰 목록
- 음식점 상세 정보

### 기대 효과

- Database 조회 횟수 감소
- 평균 응답 속도 향상
- 반복 조회 성능 개선
- 애플리케이션 부하 감소

---

## 도메인 중심 패키지 구조

프로젝트 규모가 커질 것을 고려하여 Layer 중심이 아닌 Domain 중심 패키지 구조를 적용하였습니다.

```
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

이를 통해

- 높은 응집도
- 낮은 결합도
- 유지보수성 향상
- 기능별 독립성 확보

를 달성하였습니다.

---

# 📚 프로젝트를 통해 배운 점

이번 프로젝트를 통해 단순한 CRUD 구현을 넘어 실제 서비스에서 필요한 다양한 기술과 협업 방식을 경험할 수 있었습니다.

### 기술적인 성장

- Spring Boot 기반 REST API 설계
- Spring Security와 JWT 기반 인증 및 권한 관리
- JPA를 활용한 객체 중심 데이터 처리
- Domain 중심 패키지 구조 설계
- Docker 기반 개발 환경 구축
- AWS EC2 배포 경험
- GitHub Actions 기반 CI 경험
- Caffeine Cache를 이용한 조회 성능 개선
- Gemini API 연동을 통한 AI 기능 구현

### 협업 경험

- Git Flow 기반 브랜치 전략
- Pull Request 기반 코드 리뷰
- GitHub Issues 및 Project 활용
- API 명세 협업
- ERD 공동 설계
- Notion을 활용한 문서화

---

# 🚧 향후 개발 계획

현재 프로젝트는 **모놀리식 단일 서버 환경**을 기준으로 설계되었습니다.

현재 구조에서는 Caffeine Cache만으로도 충분한 성능을 확보할 수 있었지만,

서비스 규모가 증가하거나 다중 서버 환경으로 확장될 경우에는 보다 확장성 있는 구조로 발전시킬 계획입니다.

---

## 1. Redis Cache 도입

현재는 단일 서버 환경이므로 JVM 내부 캐시인 Caffeine Cache를 사용하였습니다.

향후 다중 서버(MSA) 환경으로 확장될 경우에는 Redis Cache를 적용하여 여러 서버가 동일한 캐시 데이터를 공유하도록 개선할 계획입니다.

### 적용 예정

- 음식점 평균 평점 캐싱
- 리뷰 목록 캐싱
- 음식점 상세 조회 캐싱
- 인기 음식점 조회 캐싱

---

## 2. Redis 기반 인증 구조

현재 프로젝트는 JWT 기반 인증을 적용하였으며,

모놀리식 단일 서버 구조이기 때문에 Redis를 이용한 토큰 저장은 적용하지 않았습니다.

향후 서비스가 여러 서버로 확장될 경우 다음 기능을 Redis 기반으로 개선할 계획입니다.

### 적용 예정

- Refresh Token 저장
- 로그아웃 토큰(Blacklist) 관리
- 인증 정보 공유
- 서버 간 인증 상태 동기화

---

## 3. MSA 전환

서비스 규모가 증가할 경우 도메인별 서비스 분리를 고려하고 있습니다.

```
User Service

Store Service

Order Service

Payment Service

Review Service

AI Service
```

각 서비스를 독립적으로 운영하여 확장성과 유지보수성을 높일 계획입니다.

---

## 4. 메시지 브로커 도입

주문 및 결제 과정에서 비동기 처리를 위해 Kafka 또는 RabbitMQ를 도입할 계획입니다.

적용 예정 기능

- 주문 이벤트
- 결제 이벤트
- 리뷰 이벤트
- AI 요청 이벤트

---

## 5. 모니터링 구축

운영 환경에서 장애를 빠르게 확인할 수 있도록 모니터링 환경을 구축할 계획입니다.

### 적용 예정

- Prometheus
- Grafana
- ELK Stack

---

## 6. 테스트 자동화

현재는 Unit Test와 End-to-End 테스트를 수행하고 있습니다.

향후에는 GitHub Actions와 연동하여 Pull Request 생성 시 자동으로 테스트가 수행되는 CI 환경을 구축할 계획입니다.

---

# 👨‍👩‍👧‍👦 Contributors

| 담당 | 주요 기능 |
|------|----------|
| Authentication | 회원가입 / 로그인 / JWT 인증 |
| Store | 음식점 관리 |
| Menu | 메뉴 및 AI 메뉴 생성 |
| Order | 주문 및 주문 상태 관리 |
| Payment | 결제 |
| Review | 리뷰, 평균 평점, 리뷰 답글 |

---

# 🏆 프로젝트 회고

Delivery 프로젝트는 단순한 CRUD 구현을 넘어 실제 서비스에서 요구되는 인증, 권한 관리, 주문 상태 전이, AI 기능, 리뷰 시스템, 배포 환경 구축까지 경험할 수 있었던 프로젝트였습니다.

특히 기능 구현뿐 아니라 **"왜 이런 구조를 선택했는가"**를 고민하며 설계하는 과정에서 많은 성장을 할 수 있었습니다.

또한 팀 프로젝트를 진행하며 코드 리뷰, API 명세 협업, Git Flow 브랜치 전략, CI 환경 구축 등을 경험하면서 개발 실력뿐 아니라 협업 능력의 중요성도 배울 수 있었습니다.

앞으로는 Redis, MSA, 메시지 브로커, 모니터링 환경 등을 단계적으로 적용하여 보다 확장 가능한 서비스로 발전시킬 계획입니다.

---
