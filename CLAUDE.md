# CLAUDE.md

이 파일은 Claude Code가 이 저장소에서 작업할 때 참고해야 할 프로젝트 전용 지침입니다.

## 프로젝트 개요

PENBOT은 펜션 예약 사이트를 위한 Spring Boot 3.5.x 애플리케이션입니다.

주요 기능:

- 비회원 예약 신청
- 관리자/호스트 대시보드
- 예약 승인, 거절, 취소, 예약 불가 날짜 관리
- CoolSMS 기반 SMS 인증
- OpenAI 호환 API 클라이언트를 통한 AI 채팅 안내
- Thymeleaf 기반 서버 사이드 렌더링 화면
- Vanilla JavaScript 기반 프론트엔드 동작
- MySQL/MariaDB 저장소와 Redis 캐시

이 프로젝트의 핵심 도메인은 예약 관리입니다. 예약 날짜 계산, 차단 날짜, 관리자 인증/인가, 외부 API 키 처리는 고위험 영역으로 다뤄야 합니다.

## 기술 스택

- Java 17
- Spring Boot 3.5.3
- Gradle
- Spring MVC, Spring Security, Spring Data JPA
- Thymeleaf
- MySQL/MariaDB
- Redis
- JUnit 5, Mockito
- Docker Compose

## 자주 쓰는 명령

저장소 루트에서 Gradle wrapper를 사용합니다.

```bash
./gradlew test
./gradlew build
./gradlew bootRun
```

Windows PowerShell에서는 다음 명령을 사용합니다.

```powershell
.\gradlew.bat test
.\gradlew.bat build
.\gradlew.bat bootRun
```

로컬 인프라는 `compose.yml`에 정의되어 있습니다.

```bash
docker compose up -d mariadb redis
docker compose up -d
docker compose down
```

애플리케이션은 데이터베이스와 외부 API 설정을 환경 변수로 받습니다. 시크릿 값을 코드에 직접 작성하지 마세요.

주요 환경 변수:

- `MYSQL_ROOT_PASSWORD`
- `MYSQL_DATABASE`
- `COOLSMS_API_KEY`
- `COOLSMS_API_SECRET`
- `COOLSMS_NUMBER`
- `OPENAI_API_KEY`
- `OPENAI_URL`
- `OPENAI_MODEL`
- `OPENAI_TEMPERATURE`
- `GOOGLE_PLACES_API_KEY`

## 저장소 구조

- `src/main/java/Project/PENBOT/Booking`: 예약 DTO, 엔티티, 레포지토리, 컨버터, 컨트롤러, 서비스
- `src/main/java/Project/PENBOT/Host`: 관리자/호스트 기능, 차단 날짜, 대시보드 데이터
- `src/main/java/Project/PENBOT/Payment`: 결제 관련 엔티티와 enum
- `src/main/java/Project/PENBOT/Verify`: SMS 인증
- `src/main/java/Project/PENBOT/OpenAi`: 채팅 컨트롤러, 클라이언트, DTO, 채팅 서비스
- `src/main/java/Project/PENBOT/Config`: 보안, 필터, 비밀번호, 프로퍼티, RestTemplate 설정
- `src/main/java/Project/PENBOT/CustomException`: 도메인 예외
- `src/main/resources/templates`: Thymeleaf 화면
- `src/main/resources/static`: CSS, JavaScript, 이미지
- `src/test/java/Project/PENBOT`: 단위 테스트와 Spring Boot 테스트

## 현재 아키텍처 메모

- 컨트롤러는 `/api/**` REST 엔드포인트와 MVC 페이지 라우트를 제공합니다.
- 비즈니스 규칙은 서비스 계층에 둡니다. 날짜 중복 계산과 권한 규칙을 JavaScript에 의존시키지 마세요.
- 컨버터는 DTO와 엔티티/응답 객체 간 변환을 담당합니다.
- 현재 보안 설정은 `/api/host/**`, `/admin/**`를 `ROLE_HOST` 권한으로 보호합니다.
- `LoginFilter`는 `/api/admin/login` 요청을 처리합니다.
- `application.properties`는 시크릿과 로컬 서비스 설정을 환경 변수로 참조합니다.
- 현재 `spring.jpa.hibernate.ddl-auto=update`가 활성화되어 있습니다. 엔티티 변경은 로컬 스키마를 자동 변경할 수 있으므로 주의하세요.

## 코딩 지침

- 기존 패키지 스타일인 `Project.PENBOT.<Domain>.<Layer>`를 따릅니다.
- 현재 서비스와 컨트롤러처럼 생성자 주입을 우선 사용합니다.
- 도메인 로직은 서비스에 두고, 컨트롤러나 프론트엔드 스크립트로 밀어내지 않습니다.
- 요청/응답 경계에는 DTO를 사용합니다. 기존 코드 패턴상 명확히 필요한 경우가 아니면 새 API에서 엔티티를 직접 노출하지 않습니다.
- 사용자가 명시적으로 API 변경을 요청하지 않는 한 기존 엔드포인트 경로를 유지합니다.
- 예약 가능 여부, 차단 날짜, 로그인/보안, SMS 인증, AI 채팅 도구 동작을 변경할 때는 테스트를 추가하거나 갱신합니다.
- 작은 변경에 새 프레임워크나 큰 추상화를 도입하지 않습니다.
- 주석은 짧고 유용하게 작성합니다. 스타일만 바꾸기 위해 관련 없는 한국어 문구나 주석을 수정하지 않습니다.
- 일부 한국어 텍스트는 터미널 인코딩에 따라 깨져 보일 수 있습니다. 인코딩 수정이 목적이 아닌 이상 대량 수정하지 않습니다.

## 보안과 시크릿 처리

- `.env`, API 키, 비밀번호, 토큰, 민감한 전화번호를 커밋하지 않습니다.
- 시크릿이나 민감 정보를 로그에 남기지 않습니다.
- 외부 API 전체 응답에는 민감 정보가 포함될 수 있으므로 그대로 로깅하지 않습니다.
- 인증/인가를 변경할 때는 허용 경로와 거부 경로를 모두 검증합니다.
- AI 채팅 코드는 신뢰할 수 없는 입력을 다루는 영역으로 취급합니다.
- 명시적으로 설계하고 테스트하기 전까지 AI 채팅이 예약/관리자 상태를 직접 변경하게 만들지 않습니다.
- 프론트엔드 검증에 의존하지 말고 서버 측 DTO/서비스 검증을 우선합니다.

## 예약 도메인 규칙

예약 로직을 수정할 때는 다음 규칙을 반드시 확인합니다.

- 예약은 기존 예약과 겹치면 안 됩니다.
- 예약은 차단 날짜 범위와 겹치면 안 됩니다.
- 예약 불가 날짜 계산은 기존 예약과 차단 날짜를 함께 반영합니다.
- 체크아웃/종료일 처리 방식은 예약 범위와 차단 날짜 범위에서 다를 수 있습니다. 변경 전에 기존 테스트를 먼저 읽으세요.

관련 파일:

- `BookingService.java`
- `BookingRepository.java`
- `BlockedDateRepository.java`
- `BookingServiceTest.java`
- `HostServiceTest.java`

## 테스트 지침

비즈니스 로직을 수정하기 전에는 기존 테스트를 먼저 확인합니다.

집중 테스트:

```powershell
.\gradlew.bat test --tests Project.PENBOT.Booking.Service.BookingServiceTest
.\gradlew.bat test --tests Project.PENBOT.Host.Service.HostServiceTest
.\gradlew.bat test --tests Project.PENBOT.Verify.Service.VerifyServiceTest
```

전체 검증:

```powershell
.\gradlew.bat test
```

로컬 MySQL, Redis, 시크릿 부재 때문에 테스트가 실패하면 환경 문제와 코드 문제를 구분해서 보고합니다.

## 하네스 엔지니어링 도입 방향

이 프로젝트는 위험한 동작을 중심으로 하네스 엔지니어링을 점진적으로 도입하기 좋습니다. 작게 시작하고, 각 하네스는 저장소에서 바로 실행 가능해야 합니다.

권장 순서:

1. 예약 날짜 로직 하네스
   - 겹치는 날짜, 체크아웃 날짜, 차단 날짜, 월 경계, 중복 요청을 다루는 단위 테스트와 케이스 테이블을 추가합니다.

2. 보안 하네스
   - 익명 접근, 호스트 전용 경로, 로그인 성공/실패, 금지 경로 테스트를 추가합니다.

3. 외부 API 하네스
   - CoolSMS와 OpenAI 호환 API 호출을 mock 가능한 클라이언트 뒤에 둡니다.
   - 실제 API를 호출하지 않고 timeout, 잘못된 응답, 제공자 오류, 설정 누락을 테스트합니다.

4. 프론트엔드 동작 하네스
   - 서버 측 규칙이 안정된 뒤 가벼운 브라우저 또는 DOM 테스트를 추가합니다.

5. 회귀 fixture
   - 중요한 예약 시나리오를 작고 이름 있는 fixture로 유지해, 이후 Claude Code 세션에서 관련 코드를 수정하기 전에 실행할 수 있게 합니다.

하네스를 추가할 때는 다음을 우선합니다.

- 실제 네트워크 호출보다 결정적인 테스트
- 큰 통합 환경보다 작은 fixture
- 명확한 edge case 이름
- 구현 변경 전에 실패 동작을 먼저 포착하는 테스트

## Claude Code Hook

프로젝트 로컬 Claude Code hook 설정은 `.claude/settings.json`에 있습니다.

현재 hook:

- `.claude/hooks/pre-edit-guard.ps1`
  - 실제 시크릿 값으로 보이는 편집을 차단합니다.
  - `application.properties`, `compose.yml`, `build.gradle`, `SecurityConfig.java`, `LoginFilter.java` 같은 보호 대상 파일을 수정할 때 경고합니다.

- `.claude/hooks/post-edit-check.ps1`
  - Booking, Host/Security, OpenAi, Verify 코드 수정 후 관련 하네스/테스트 실행을 알립니다.

- `.claude/hooks/stop-reminder.ps1`
  - Claude Code 응답 종료 시 변경 파일을 확인하고 필요한 검증을 리마인드합니다.

이 hook들은 의도적으로 가볍게 구성되어 있습니다. 테스트를 대체하지 않고, 위험한 검증을 잊지 않게 만드는 역할입니다.

## Claude Code 작업 규칙

이 저장소에서 작업할 때는 다음을 따릅니다.

- 수정 전 관련 서비스, 컨트롤러, 레포지토리, DTO, 테스트를 읽습니다.
- 변경 범위를 작게 유지합니다.
- 관련 없는 파일을 재포맷하지 않습니다.
- Gradle wrapper 파일은 Gradle 작업이 목적이 아닌 이상 수정하지 않습니다.
- Docker, 보안, 환경 설정은 가볍게 변경하지 않습니다.
- 수정 후 가장 좁은 관련 테스트를 먼저 실행하고, 가능하면 `.\gradlew.bat test`도 실행합니다.
- 명령 실행에 서비스나 시크릿이 필요해 로컬에서 불가능하면, 무엇이 막혔고 대신 무엇을 검증했는지 명확히 설명합니다.

## 향후 작업 시 첫 확인 명령

예약 변경:

```powershell
rg "isAvailable|getUnavailableDates|BlockedDate|Booking" src/main/java src/test/java
```

관리자/보안 변경:

```powershell
rg "SecurityFilterChain|LoginFilter|ROLE_HOST|api/host|api/admin" src/main/java src/test/java
```

AI 채팅 변경:

```powershell
rg "OpenAi|ChatService|OpenAiClient|Tool" src/main/java src/test/java
```
