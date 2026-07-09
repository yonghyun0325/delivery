# 🍽️ 음식 주문 관리 플랫폼 (Delivery)

## 📌 프로젝트 개요

사용자와 사장님을 연결하는 음식 주문 관리 플랫폼입니다.

회원가입부터 음식점 관리, 메뉴 관리, 주문, 결제, 리뷰 및 사장님 답글 기능까지 하나의 서비스에서 제공하는 것을 목표로 개발하고 있습니다.

도메인 중심(Domain-Driven) 구조를 기반으로 설계하여 유지보수성과 확장성을 고려하였으며, Spring Boot와 JPA를 활용한 REST API 서버를 구현하고 있습니다.

---

# 🎯 프로젝트 목적

* 음식 주문 서비스를 위한 REST API 서버 개발
* Spring Boot 기반의 실무형 백엔드 프로젝트 경험
* 도메인 중심 설계를 통한 유지보수성 향상
* Spring Security 및 JWT 기반 인증·인가 적용
* Docker를 활용한 개발 환경 통일
* AWS EC2 배포를 통한 운영 환경 구축

---

# 🗂️ ERD

> 추후 예정

---

# ✨ 주요 기능

## 1. 회원(User)

* 회원가입
* 로그인 (JWT)
* 권한(Role) 관리
* 회원 정보 조회 및 수정

## 2. 가게(Store)

* 가게 등록
* 가게 조회
* 가게 수정 및 삭제
* 가게별 평균 평점 조회

## 3. 메뉴(Menu)

* 메뉴 등록
* 메뉴 조회
* 메뉴 수정
* 메뉴 삭제

## 4. 주문(Order)

* 주문 생성
* 주문 조회
* 주문 상태 변경
* 주문 취소

## 5. 결제(Payment)

* 주문 결제
* 결제 상태 관리

## 6. 리뷰(Review)

* 리뷰 작성
* 리뷰 조회
* 리뷰 수정
* 리뷰 삭제
* 가게별 리뷰 조회
* 평균 평점 조회
* 사장님 리뷰 답글 작성
* 사장님 리뷰 답글 조회
* 사장님 리뷰 답글 수정
* 사장님 리뷰 답글 삭제

---

# 🛠 기술 스택

## Backend

* Java 17
* Spring Boot 3.5.16
* Spring Data JPA
* Spring Security
* Validation
* Lombok

## Database

* PostgreSQL 17

## DevOps

* Docker
* Docker Compose

## Build Tool

* Gradle

---

# 🏗 프로젝트 아키텍처

<img width="1289" height="715" alt="Architecture" src="https://github.com/user-attachments/assets/b3c5092b-fdf3-46d0-b967-6fd5b5023f0d" />

---

# 📂 프로젝트 구조

```text
src
 ├── common
 │   ├── base
 │   ├── config
 │   └── exception
 │
 ├── domain
 │   ├── auth
 │   ├── user
 │   ├── store
 │   ├── menu
 │   ├── order
 │   ├── payment
 │   ├── review
 │   └── reviewreply
 │
 ├── security
 └── resources
```

---

# 📑 API 명세

> 추가 예정

---

# 🚀 실행 방법

### PostgreSQL 실행

```bash
docker-compose up -d
```

### 프로젝트 빌드

```bash
./gradlew build
```

### 프로젝트 실행

```bash
./gradlew bootRun
```

---

### API 문서
```bash
Swagger : http://localhost:8080/swagger-ui/index.html

Scalar : http://localhost:8080/scalar#tag/address-controller/PATCH/api/v1/users/me/addresses/{addressId}
```

# 👥 팀원 및 역할 분담

| 이름   | 담당                                              |
| ---    | ----------------------------------------------- |
| 이강석  | 회원(Authentication)                              |
| 정수민  | 가게(Store)                                       |
| 임은택  | 메뉴(Menu)                                        |
| 안예진  | 주문(Order)                                       |
| 송채영  | 결제(Payment)                                     |
| 이용현 | 리뷰(Review), 리뷰 답글(Review Reply) |

> 역할은 프로젝트 진행 상황에 따라 변경될 수 있습니다.

---

# 📅 개발 환경

| 항목          | 버전     |
| ----------- | ------ |
| Java        | 17     |
| Spring Boot | 3.5.16 |
| PostgreSQL  | 17     |
| Gradle      | 8.x    |
| Docker      | Latest |

---

# 📌 향후 개발 계획

* Spring Security + JWT 인증·인가
* Redis 적용
* Swagger(OpenAPI) 문서화
* AWS EC2 배포
* GitHub Actions 기반 CI/CD 구축
* 테스트 코드 작성
* 성능 최적화
