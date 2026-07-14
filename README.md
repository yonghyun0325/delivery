<div align="center">

# 🍽️ Delivery

### 사용자와 음식점 사장님을 연결하는 음식 주문 관리 플랫폼

회원가입부터 음식점·메뉴 관리, 장바구니, 주문, 결제, 리뷰와 사장님 답글까지  
음식 주문 서비스의 전체 흐름을 구현한 **Spring Boot 백엔드 팀 프로젝트**입니다.

도메인 중심 설계와 역할 기반 권한 제어를 적용하고,  
Docker·AWS EC2·GitHub Actions를 활용해 개발부터 배포까지 경험했습니다.

<br>

![Java](https://img.shields.io/badge/Java-17-007396?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5.16-6DB33F?logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-JWT-6DB33F?logo=springsecurity&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-4169E1?logo=postgresql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-Token_Storage-DC382D?logo=redis&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker&logoColor=white)
![AWS](https://img.shields.io/badge/AWS-EC2-FF9900?logo=amazonwebservices&logoColor=white)
![GitHub Actions](https://img.shields.io/badge/GitHub_Actions-CI-2088FF?logo=githubactions&logoColor=white)

</div>

---

## 📌 프로젝트 소개

**Delivery**는 고객과 음식점 사장님을 연결하는 음식 주문 관리 플랫폼입니다.

각 팀원이 하나의 핵심 도메인을 담당하고, 공통 응답·예외 처리·인증 구조를 함께 설계했습니다.  
단순 CRUD 구현을 넘어 주문 상태 전이, 실제 자원 소유자 검증, 토큰 관리, 데이터 영속성,
외부 AI 연동, 페이징·검색 및 단위 테스트까지 실무에 가까운 문제를 다루었습니다.

### 프로젝트 목표

- Spring Boot 기반 REST API 서버 구현
- 도메인 중심 패키지 구조와 계층별 책임 분리
- Spring Security·JWT 기반 인증 및 역할별 인가
- Docker를 활용한 일관된 개발 환경 구성
- AWS EC2 기반 공용 서버 및 PostgreSQL 운영
- 테스트와 CI를 통한 안정적인 협업 환경 구축

---

## 🛠️ 기술 스택

| 구분 | 기술 |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.5.16 |
| Web | Spring MVC, Validation |
| Security | Spring Security, JWT |
| Persistence | Spring Data JPA, Hibernate |
| Database | PostgreSQL 17 |
| Cache | Redis |
| External API | Gemini API |
| Test | JUnit 5, Mockito, Spring Boot Test |
| Documentation | Swagger/OpenAPI, Scalar |
| Logging | SLF4J, Logback |
| Build | Gradle |
| Infrastructure | Docker, Docker Compose, AWS EC2 |
| CI | GitHub Actions |
| Collaboration | GitHub Issues, Pull Request, Git Flow |

---

## 🏗️ 시스템 아키텍처

<div align="center">

<img width="1289" height="715" alt="Delivery Architecture"
src="https://github.com/user-attachments/assets/b3c5092b-fdf3-46d0-b967-6fd5b5023f0d" />

</div>

- 클라이언트 요청은 Spring Security의 JWT 인증 필터를 거쳐 Controller로 전달됩니다.
- Service 계층에서 트랜잭션과 비즈니스 규칙을 처리합니다.
- PostgreSQL은 영속 데이터를, Redis는 인증 관련 데이터를 관리합니다.
- Docker Compose로 실행 환경을 구성하고 AWS EC2에 배포했습니다.
- GitHub Actions를 통해 코드 빌드와 테스트를 자동 검증합니다.

---

## 🗂️ ERD

> ERD 이미지 추가 예정

---

## ✨ 도메인별 주요 기능

| 도메인 | 주요 기능 |
|---|---|
| **회원·인증** | 회원가입, 로그인, 토큰 재발급, 로그아웃, 회원 정보 관리, 회원 탈퇴 |
| **음식점** | 가게 CRUD, 영업 상태 관리, 카테고리·지역 조건 조회, 평균 평점 관리 |
| **메뉴** | 메뉴 CRUD, 공개·숨김 상태 관리, 전체 메뉴 검색, AI 설명 생성 |
| **장바구니** | 장바구니 조회, 메뉴 추가·수량 변경·삭제, 장바구니 비우기 |
| **주문** | 주문 생성, 주문 내역 조회, 역할별 접근 제어, 주문 상태 전이·취소 |
| **결제** | 주문 결제, 결제 조회, 결제 상태 및 취소 처리 |
| **리뷰** | 리뷰 CRUD, 작성 권한 검증, 페이징·정렬, 평균 평점 계산 |
| **리뷰 답글** | 사장님 답글 CRUD, 실제 가게 소유자 검증, 중복 답글 방지 |
| **AI** | Gemini API 기반 메뉴 설명 생성, 성공·실패 로그 저장 |

---

## ⭐ 핵심 기술 구현

### 1. JWT와 Redis를 활용한 인증 흐름

- Spring Security의 `AuthenticationManager`를 이용해 로그인 인증
- 로그인 성공 시 Access Token과 Refresh Token 발급
- Refresh Token을 사용자 UUID 기준으로 저장
- 재발급 시 요청 토큰과 서버 저장 토큰을 비교
- 로그아웃 및 회원 탈퇴 시 Refresh Token 삭제
- `CustomUserDetails`를 통해 사용자 ID·UUID·권한을 인증 객체에 연동

```text
로그인
  → 사용자 인증
  → Access / Refresh Token 발급
  → Refresh Token 저장
  → 인증 요청 및 토큰 재발급
```

---

### 2. 역할과 실제 자원 소유권을 함께 검증

단순히 `OWNER` 역할만 검사하지 않고, 실제로 해당 가게를 소유한 사용자인지 추가로 검증했습니다.

- OWNER: 본인 가게의 음식점·메뉴·주문·리뷰 답글만 관리
- CUSTOMER: 본인 주문과 리뷰만 관리
- MANAGER·MASTER: 정책에 따라 전체 자원 관리 가능
- 숨김 메뉴는 권한 없는 사용자에게 존재 여부를 노출하지 않도록 `404` 처리

```text
Role 검증
   +
Store.userId / Order.userId / Review.userId 검증
```

---

### 3. 주문 상태 전이와 주문 생성 규칙

주문 상태를 임의로 변경하지 못하도록 허용된 흐름을 명시적으로 검증했습니다.

```text
REQUESTED
 ├─ ACCEPTED → COOKING → DELIVERING → DELIVERED → COMPLETED
 ├─ REJECTED
 └─ CUSTOMER_CANCELLED
```

주문 생성 시에는 다음 규칙을 함께 검증합니다.

- 영업 중인 가게인지 확인
- 주문 수량이 1개 이상인지 검증
- 메뉴가 해당 가게에 소속되어 있고 주문 가능한 상태인지 검증
- 주문 당시 메뉴명과 가격을 스냅샷으로 저장
- 최소 주문 금액 충족 여부 검증
- 고객 주문 취소는 생성 후 5분 이내에만 허용

---

### 4. Specification과 Pageable 기반 조건 조회

주문·음식점·메뉴·리뷰 목록은 데이터 증가를 고려해 페이징을 적용했습니다.

주문 목록에서는 `Specification`을 조합하여 선택적으로 조회 조건을 적용합니다.

- 사용자 또는 음식점
- 주문 상태
- 시작일과 종료일
- Soft Delete 제외
- 페이지 번호·크기
- 최신순·오래된순

```java
Specification<Order> specification =
        Specification.<Order>unrestricted()
                .and(userIdEquals(userId))
                .and(deletedAtIsNull())
                .and(statusEquals(status))
                .and(createdAtGoe(startDateTime))
                .and(createdAtLoe(endDateTime));
```

---

### 5. 외부 AI 호출과 DB 트랜잭션 분리

메뉴 설명 생성 시 Gemini API 응답을 기다리는 동안 DB 커넥션을 점유하지 않도록
외부 API 호출과 DB 저장 트랜잭션을 분리했습니다.

```text
입력·권한 검증
  → Gemini API 호출
  → 호출 성공
  → 짧은 DB 트랜잭션으로 메뉴 저장
```

- AI 호출 전에 권한·입력값을 먼저 검증해 불필요한 API 비용 방지
- 외부 API 호출은 DB 트랜잭션 밖에서 처리
- DB 저장 구간만 `TransactionTemplate`로 짧게 구성
- AI 실패 시 메뉴 저장을 진행하지 않음
- 성공·실패 결과를 AI 로그로 기록

---

### 6. 장바구니의 단일 음식점 정책

하나의 장바구니에는 한 음식점의 메뉴만 담을 수 있도록 검증했습니다.

- 장바구니가 없으면 첫 메뉴의 가게 기준으로 생성
- 같은 메뉴를 다시 담으면 수량 증가
- 다른 음식점의 메뉴 추가 시 요청 거부
- 삭제 후 장바구니 항목이 없으면 장바구니도 Soft Delete
- 메뉴명과 가격을 장바구니 항목에 스냅샷으로 저장

---

### 7. 리뷰와 평균 평점 관리

- 배송 완료 또는 주문 완료 상태에서만 리뷰 작성
- 로그인 사용자와 실제 주문자 일치 여부 검증
- 주문당 리뷰 하나만 등록
- 작성자 본인만 리뷰 수정·삭제
- 최신순·오래된순·평점순 정렬
- Pageable 기반 음식점 리뷰 조회
- 리뷰 등록·수정·삭제 시 음식점 평균 평점 갱신
- 실제 음식점 OWNER만 리뷰 답글 등록 가능

---

### 8. 이벤트 기반 회원 탈퇴 후속 처리

회원 탈퇴 시 UserService가 모든 도메인을 직접 호출하지 않고 이벤트를 발행합니다.

```text
회원 탈퇴
  → Refresh Token 삭제
  → User Soft Delete
  → UserDeletedEvent 발행
  → 필요한 도메인이 이벤트 수신
```

도메인 간 직접 의존성을 줄이고, 각 도메인이 자신의 후속 처리 책임을 갖도록 구성했습니다.

---

## 🧪 테스트

핵심 비즈니스 규칙을 검증하기 위해 JUnit 5와 Mockito 기반 테스트를 작성했습니다.

### 주요 검증 항목

- 정상적인 CRUD 처리
- 사용자 및 실제 자원 소유자 권한
- 존재하지 않거나 삭제된 데이터 처리
- 잘못된 주문 상태 전이
- 중복 회원·가게·리뷰·답글 등록
- 주문 가능 메뉴와 최소 주문 금액
- 토큰 재발급 및 로그아웃
- 장바구니의 음식점 일치 여부
- 외부 AI 호출 성공·실패 처리

```bash
./gradlew test
```

GitHub Actions에서도 Push와 Pull Request 시 빌드 및 테스트를 자동 검증합니다.

---

## 🔥 트러블슈팅 후보

발표 자료에는 팀원 투표를 통해 아래 후보 중 2개를 선정할 예정입니다.

<details>
<summary><strong>1. 외부 AI 호출 중 DB 커넥션 장시간 점유</strong></summary>

### 문제상황

메뉴 생성 트랜잭션 안에서 Gemini API를 호출하면 외부 응답을 기다리는 동안
DB 트랜잭션과 커넥션이 불필요하게 유지될 수 있었습니다.

### 원인

메서드 전체에 트랜잭션이 적용되어 외부 네트워크 작업과 DB 저장 작업의 범위가 분리되지 않았습니다.

### 해결방법

AI 호출 메서드에는 `Propagation.NOT_SUPPORTED`를 적용하고,
실제 DB 저장 부분만 `TransactionTemplate`로 짧게 처리했습니다.

### 왜 이렇게 해결했는지

외부 API는 응답 지연이나 장애가 발생할 수 있습니다.
네트워크 대기 시간 동안 DB 커넥션을 점유하지 않음으로써 커넥션 풀 고갈 위험을 줄이고,
AI 호출이 성공했을 때만 저장 트랜잭션을 시작하기 위해 선택했습니다.

</details>

<details>
<summary><strong>2. OWNER 역할만으로 다른 가게 자원 접근 가능</strong></summary>

### 문제상황

OWNER 권한 보유 여부만 검사하면 다른 사장님의 가게·메뉴·주문·리뷰에도 접근할 수 있었습니다.

### 원인

역할 기반 인가와 실제 자원의 소유권 검증이 분리되어 있지 않았습니다.

### 해결방법

`Store.userId`와 로그인 사용자 ID를 비교하여 실제 소유자인지 검증했습니다.
MANAGER와 MASTER는 별도 정책으로 소유권 검증을 우회하도록 구성했습니다.

### 왜 이렇게 해결했는지

역할은 사용자의 범주만 나타낼 뿐, 특정 자원의 소유권까지 보장하지 않습니다.
권한과 소유권을 함께 검증해야 수평적 권한 상승을 방지할 수 있기 때문입니다.

</details>

<details>
<summary><strong>3. 잘못된 주문 상태 변경</strong></summary>

### 문제상황

주문의 현재 상태를 고려하지 않으면 `REQUESTED` 상태에서 바로 `DELIVERED`로 변경하는 등
비정상적인 주문 흐름이 발생할 수 있었습니다.

### 원인

각 API에서 상태를 단순 대입하고 상태 간 전이 규칙을 중앙에서 관리하지 않았습니다.

### 해결방법

현재 상태와 다음 상태를 비교하는 공통 검증 메서드를 만들고,
허용된 상태 전이만 통과하도록 구현했습니다.

### 왜 이렇게 해결했는지

주문 상태는 여러 API에서 변경되므로 규칙을 한곳에서 관리해야 일관성을 유지할 수 있습니다.
종료 상태의 주문이 다시 변경되는 것도 함께 차단할 수 있습니다.

</details>

<details>
<summary><strong>4. JWT 로그아웃 상태를 서버에서 판단하기 어려움</strong></summary>

### 문제상황

JWT는 발급 후 서버가 상태를 보관하지 않기 때문에 로그아웃 이후에도
유효한 Refresh Token으로 토큰을 재발급할 가능성이 있었습니다.

### 원인

JWT의 무상태 특성만으로는 사용자 로그아웃 여부를 제어하기 어려웠습니다.

### 해결방법

Refresh Token을 사용자 UUID 기준으로 저장하고,
로그아웃과 회원 탈퇴 시 저장된 토큰을 삭제했습니다.
재발급 시 요청 토큰과 저장 토큰을 비교했습니다.

### 왜 이렇게 해결했는지

Access Token의 장점은 유지하면서도 Refresh Token의 유효성을 서버에서 통제하기 위해서입니다.
다중 로그인을 허용하지 않는 프로젝트 정책에도 적합했습니다.

</details>

<details>
<summary><strong>5. 리뷰 삭제 후 동일 주문으로 재작성</strong></summary>

### 문제상황

리뷰를 Soft Delete한 뒤 동일 주문으로 다시 리뷰를 작성할 수 있었습니다.

### 원인

삭제되지 않은 리뷰만 대상으로 중복 여부를 확인했기 때문에
삭제된 리뷰는 존재하지 않는 것으로 판단되었습니다.

### 해결방법

삭제 여부와 관계없이 `orderId`를 기준으로 리뷰 존재 여부를 검사하고,
DB에도 주문 ID Unique 제약을 추가했습니다.

### 왜 이렇게 해결했는지

프로젝트 정책이 ‘주문당 리뷰 한 개’이므로 애플리케이션 검증과 DB 제약을 함께 적용해
동시 요청 상황에서도 데이터 무결성을 지키기 위해서입니다.

</details>

---

## 📑 API 문서

애플리케이션 실행 후 아래 주소에서 API 명세를 확인할 수 있습니다.

| 문서 | 주소 |
|---|---|
| Swagger UI | `http://localhost:8080/swagger-ui/index.html` |
| Scalar | `http://localhost:8080/scalar` |

---

## 🚀 실행 방법

### 1. 환경변수 설정

프로젝트 루트에 `.env` 파일을 생성합니다.

```env
JWT_ACCESS_SECRET=
JWT_REFRESH_SECRET=

ENCRYPT_KEY=
ENCRYPT_SALT=

GEMINI_API_KEY=
GEMINI_MODEL=gemini-3.5-flash
GEMINI_BASE_URL=https://generativelanguage.googleapis.com
```

> `.env` 파일과 비밀키는 Git에 커밋하지 않습니다.

### 2. PostgreSQL 실행

```bash
docker compose up -d
```

### 3. 테스트 및 빌드

```bash
./gradlew clean build
```

Windows:

```powershell
.\gradlew clean build
```

### 4. 애플리케이션 실행

```bash
./gradlew bootRun
```

---

## 🤝 협업 방식

- `main`: 운영 기준 브랜치
- `develop`: 기능 통합 브랜치
- `feature/*`: 도메인별 기능 개발 브랜치
- GitHub Issue를 이용한 작업 단위 관리
- Pull Request 기반 코드 리뷰
- 도메인별 담당자 분리 및 공통 규칙 합의
- Swagger를 활용한 API 명세 공유
- GitHub Actions를 이용한 빌드·테스트 자동화
- Docker Compose 기반 PostgreSQL 개발 환경 통일

---

## 👥 팀원 및 역할

| 팀원 | 담당 도메인 |
|---|---|
| 이강석 | 회원·인증 |
| 정수민 | 음식점 |
| 임은택 | 메뉴·AI |
| 안예진 | 주문 |
| 송채영 | 결제·장바구니 |
| 이용현 | 리뷰·리뷰 답글·Docker·AWS EC2 |

---

## 📈 프로젝트를 통해 얻은 경험

- 도메인 중심 구조로 협업하는 방법
- 역할 기반 권한과 실제 자원 소유권을 함께 검증하는 방법
- 주문 상태와 같은 핵심 비즈니스 규칙을 Service 계층에서 보호하는 방법
- 외부 API와 DB 트랜잭션 범위를 분리하는 방법
- JPA의 Dirty Checking, Soft Delete, Specification, Pageable 활용
- JWT와 Redis를 이용한 인증 상태 관리
- JUnit 5와 Mockito 기반 단위 테스트 작성
- Docker와 AWS EC2 기반 서버·DB 운영
- Pull Request와 코드 리뷰를 통한 협업
- GitHub Actions 기반 지속적 통합

---

## 🚧 향후 고도화 계획

- GitHub Actions 기반 자동 배포 파이프라인 구축
- Redis 캐시를 활용한 반복 조회 성능 개선
- 주문·결제 동시 요청에 대한 락과 멱등성 강화
- 테스트 커버리지 확대 및 통합 테스트 보강
- Prometheus·Grafana 기반 모니터링 도입
- 운영 환경의 로그 수집·검색 체계 구축
- 데이터베이스 마이그레이션 도구 도입

---

<div align="center">

### 🍽️ Delivery Team

**기능 구현을 넘어 인증, 테스트, 협업과 운영 환경까지 경험한 백엔드 팀 프로젝트**

<br>

Made with ❤️ by Delivery Team

</div>
