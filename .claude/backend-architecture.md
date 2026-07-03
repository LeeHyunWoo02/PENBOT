# PENBOT 백엔드 아키텍처 가이드

이 문서는 PENBOT 저장소의 실제 코드를 분석해서 정리한 백엔드 레이어 구조와 도메인 규칙입니다.
`CLAUDE.md`가 "어떻게 작업할지"에 대한 일반 규칙이라면, 이 문서는 "지금 코드가 실제로 어떻게 동작하는지"를
파악하기 위한 참조 자료입니다. 코드 변경 전에 관련 섹션을 먼저 확인하세요.

## 1. 레이어 구조

```
Controller  → 요청/응답 DTO만 다룸. 비즈니스 로직 없음. try/catch 또는 @ExceptionHandler로 예외 → HTTP 상태코드 매핑
    ↓
Service     → 도메인 로직, 트랜잭션(@Transactional), 검증, CustomException 발생
    ↓
Repository  → Spring Data JPA 인터페이스. 메서드명 기반 쿼리(existsBy.../findBy...) 위주, JPQL/QueryDSL 없음
    ↓
Entity      → JPA 엔티티, Lombok @Builder/@Getter/@Setter

Converter   → static 메서드로 DTO ↔ Entity 변환 (Controller/Service가 아닌 별도 클래스)
```

- 컨트롤러는 서비스 메서드 호출 후 성공/실패를 판단해 `ResponseEntity`를 조립하는 역할만 합니다. 두 가지 예외 처리 스타일이 혼재합니다:
  - `@ExceptionHandler(XxxException.class)` (Booking/Host 컨트롤러 전반)
  - 컨트롤러 메서드 내부에서 직접 `try/catch` 후 `badRequest().body(...)` (`HostController.updateBooking`/`deleteBooking`, `BookingController.create`)
  - 새 엔드포인트를 추가할 때는 같은 컨트롤러 안의 기존 스타일을 따르세요. 두 스타일을 임의로 섞지 마세요.
- Converter는 인스턴스 없이 `static` 메서드만 제공합니다 (`BookingConverter`, `BlockedDateConverter`, `BookingAllConverter`). 새 변환 로직도 이 패턴을 따릅니다.

## 2. 도메인별 구조

### 2.1 Booking (`Project.PENBOT.Booking`)
- 비회원 전용 도메인입니다. 회원 엔티티/FK가 없고 `guestName`/`guestPhone`/`guestEmail` + 4자리 숫자 `password`(Integer)로 예약을 식별합니다.
- `BookStatus`: `PENDING` → `CONFIRMED` / `CANCELLED`.
- `BookingSimpleDTO`는 의도적으로 `password`를 제외합니다 — 응답 DTO를 새로 만들 때도 비밀번호는 절대 노출하지 마세요.
- 핵심 서비스: `BookingService.createBooking`, `isAvailable`, `getUnavailableDates`, `checkMyBooking`(비회원 예약 조회, 이름+전화번호+비밀번호 3중 일치).
- 컨트롤러 경로: `POST /api/bookings/`(생성), `GET /api/bookings/unavailable`(불가 날짜), `POST /api/bookings/check`(비회원 조회).

### 2.2 Host (`Project.PENBOT.Host`) — 관리자/호스트 도메인
- `Admin` 엔티티(`id`, `username`, `password`)와 `PrincipalDetailsService`(`UserDetailsService` 구현)로 세션 기반 로그인.
- `HostService`가 예약 승인/거절/삭제, 차단 날짜 생성/삭제, 예약 가능 여부 계산을 모두 담당하는 관리자용 God-service에 가깝습니다. 새 관리자 기능도 우선 여기에 추가하는 것이 기존 컨벤션입니다.
- `HostService.hasDateConflict(startDate, endDate)`(2026-07-03 이전 이름: `isAvailable`)는 겹침이 있을 때 `true`를 반환합니다. `createBlockedDate`에서 `if (hasDateConflict(...)) throw BlockedDateConflictException`처럼 사용됩니다. 네이밍이 반환값과 반대였던 문제를 리네이밍으로 해결했습니다(`bugfix-proposals.md` #5).
- `HostService.getUnavailableDates()`는 `BookStatus.CONFIRMED`뿐 아니라 `PENDING` 예약도 차단 날짜에 포함합니다. 즉 승인 대기 중인 예약도 다른 손님에게는 "예약 불가"로 보입니다 — 이 규칙을 변경하려면 의도적인 결정이 필요합니다.
- 엔드포인트: `/admin/dashboard`(뷰), `/api/host/bookings`, `/api/host/bookings/{id}`(GET/PUT/DELETE), `/api/host/blocks`(GET/POST), `/api/host/blocks/{id}`(DELETE), `/api/host/unavailable-dates`. 전부 `hasRole("HOST")`.

### 2.3 Verify (`Project.PENBOT.Verify`) — SMS 인증
- 흐름: `POST /api/verify/sendcode` → 6자리 랜덤 코드 생성 → Redis에 `sms:auth:{phone}` 키로 5분 TTL 저장 → CoolSMS로 발송 → `POST /api/verify/verifycode` → Redis 조회/일치 확인 → 성공 시 즉시 삭제(1회용).
- Redis가 인증 코드의 유일한 저장소입니다. Redis가 죽으면 인증 자체가 불가능합니다 — 로컬에서 이 도메인을 테스트하려면 `docker compose up -d redis` 필요.
- `CoolSMSUtil`은 `@PostConstruct`에서 SDK를 초기화하므로, API 키 환경변수가 없으면 애플리케이션 컨텍스트 로딩 자체가 실패할 수 있습니다.

### 2.4 OpenAi (`Project.PENBOT.OpenAi`) — AI 채팅
- 이름은 `OpenAi`지만 `application.properties`의 실제 설정(`spring.ai.openai.chat.model=gemini-1.5-flash`, completion-path가 `/v1beta/models`)은 Gemini 호환 엔드포인트를 가리킵니다. "OpenAI 호환 API"라는 표현은 실제로는 Gemini를 OpenAI 스펙 형태로 감싼 것입니다.
- `ChatService.askBot()`은 시스템 프롬프트 + 유저 메시지 2개 메시지만 보내는 단순 구조입니다. `Tool`/`UserLocation` DTO는 정의만 되어 있고 실제로는 사용되지 않습니다(항상 `null`) — 함수 호출(tool use) 기반 기능은 아직 구현되어 있지 않습니다.
- 모든 예외를 `ChatService`에서 캐치해서 사용자에게는 고정 안내 문구만 반환합니다(스택트레이스 미노출). 새로운 실패 케이스를 추가해도 이 catch-all 구조를 깨지 마세요.
- `OpenAiClient.getChatCompletion()`은 2026-07-03부터 비2xx 응답/빈 body를 `ExternalApiException`으로 명시적으로 던집니다(이전에는 주석 처리되어 있었음). `ChatService.askBot()`의 기존 catch-all이 이를 잡아 고정 안내 문구를 반환하므로 최종 사용자 경험은 이전과 동일하지만, 이제 로그에 원인이 명확히 남습니다. 여전히 `ChatService`에 대한 전용 단위 테스트는 없으므로, AI 채팅 코드를 추가로 수정할 때는 CLAUDE.md의 "timeout/잘못된 응답/제공자 오류 테스트" 하네스 방향에 맞춰 테스트를 먼저 작성하는 것을 고려하세요.

### 2.5 Payment — 삭제됨 (2026-07-03)
- `Payment`/`Method`/`PayStatus` 엔티티만 존재하고 Repository/Service/Controller가 전혀 없는 미사용 스텁이었습니다. `bugfix-proposals.md` #3에 따라 삭제했습니다.
- 결제 기능이 필요해지면 실제 요구사항(결제 수단, 상태 전이, Booking과의 연관관계)에 맞춰 새로 설계하세요 — 예전 스키마를 참고할 필요는 없습니다.

### 2.6 CustomException (`Project.PENBOT.CustomException`)
모두 `RuntimeException` 상속, 기본 메시지 + 커스텀 메시지 오버로드 패턴:
| 예외 | 기본 메시지 | 매핑 상태코드 |
|---|---|---|
| `BookingNotFoundException` | 존재하지 않는 예약입니다. | 404 |
| `BlockedDateConflictException` | 이미 예약된 날짜가 있어 차단할 수 없습니다. | 409 |
| `ForbiddenCreateBookingException` | 예약을 생성할 수 없습니다. 이미 예약된 날짜가 있습니다. | 403 |
| `ForbiddenException` | 해당 리소스에 대한 접근 권한이 없습니다. | 403 |
| `UnableBookingException` | 이미 예약이 되어있어서 예약을 할 수 없습니다. | (Service 단에서 발생, 아직 전용 핸들러 없음) |
| `ExternalApiException` | 외부 API 호출에 실패했습니다. | (2026-07-03 추가, `OpenAiClient`에서 사용, `ChatService`의 catch-all이 처리 — 전용 핸들러 없음) |

새 도메인 예외를 추가할 때는 이 패키지에 같은 패턴(기본 생성자 + 메시지 생성자)으로 추가하세요.

## 3. 인증/인가 (`Config` 패키지)

- **세션 기반 인증**, JWT 미사용(`build.gradle`에 JWT 의존성 없음). `LoginFilter`(`UsernamePasswordAuthenticationFilter` 대체)가 `POST /api/admin/login`만 가로채서 JSON body의 `username`/`password`로 인증하고, 성공 시 `HttpSession`에 `SecurityContext`를 직접 저장합니다.
- `SecurityConfig`의 인가 규칙은 **선언 순서대로 첫 매칭이 적용**됩니다:
  1. `/css/**, /images/**, /js/**, /favicon.ico` → permitAll
  2. `/api/host/**` → `hasRole("HOST")`
  3. `/admin/**` → `hasRole("HOST")`
  4. `/api/admin/login` → permitAll
  5. `/**` → permitAll ← 그 외 모든 경로는 의도적으로 공개 API로 명시됨 (2026-07-03 수정: 도달 불가능했던 `anyRequest().authenticated()` 죽은 규칙을 제거하고 `anyRequest().permitAll()`로 정리, `bugfix-proposals.md` #1 참고)
  - 결론: **`/api/host/**`와 `/admin/**`만 보호되고, 그 외 모든 경로(`/api/bookings/**` 등 포함)는 기본적으로 인증 없이 열려 있습니다.** 새 엔드포인트를 보호하고 싶다면 반드시 `/api/host/**` 규칙보다 먼저(또는 그 규칙 하위 경로로) 명시적으로 추가해야 하며, 그냥 다른 경로에 만들면 자동으로 공개됩니다.
- CSRF는 전역 비활성화되어 있습니다. CORS 설정은 코드베이스 어디에도 없습니다 — 프론트가 별도 오리진에서 호출되는 상황이 생기면 별도 CORS 설정이 필요합니다. (현재는 동일 오리진 서빙이라 미조치 상태 유지, `bugfix-proposals.md` #4 참고)
- **비밀번호 저장/비교**:
  - 관리자 로그인(`Admin`/`PrincipalDetailsService`)은 2026-07-03에 BCrypt로 전환했습니다. `SecurityConfig.passwordEncoder()`가 이제 `BCryptPasswordEncoder`를 반환하고, `PrincipalDetailsService`는 더 이상 `{noop}` 접두사를 붙이지 않습니다. 죽은 코드였던 `PasswordConfig`(중복 BCrypt 빈)는 삭제했습니다. **admin 테이블에 새로 계정을 넣을 때는 반드시 BCrypt 해시로 저장해야 합니다** — 평문을 넣으면 로그인이 실패합니다.
  - 게스트 예약 비밀번호(`Booking.password`, `Integer`)는 **여전히 평문 비교입니다.** 이 부분은 파급 범위가 커서(엔티티/DTO/레포지토리/서비스/프론트 JS/데이터 마이그레이션) 이번 수정에서 의도적으로 제외했습니다(`bugfix-proposals.md` #2b 참고). 실제로 해싱 기반으로 바꾸는 작업은 명시적으로 요청받았을 때만 진행하세요.

## 4. 외부 연동 요약

| 대상 | 위치 | 비고 |
|---|---|---|
| MySQL | `spring.datasource.*` | `ddl-auto=update` — 엔티티 필드 변경이 로컬 스키마를 자동 변경함 |
| Redis | `Verify/Service/VerifyService.java` | SMS 인증 코드 저장/TTL/1회성 삭제 전용, 다른 캐시 용도로는 아직 사용 안 함 |
| CoolSMS | `Verify/Util/CoolSMSUtil.java` | `@PostConstruct`에서 SDK 초기화, 키 누락 시 앱 구동 자체가 실패할 수 있음 |
| Gemini(=OpenAI 스펙) | `OpenAi/Client/OpenAiClient.java` | `RestTemplate` 기반, timeout 60초. 2026-07-03에 에러 응답 처리 추가(비2xx/빈 body → `ExternalApiException`) |
| Google Places | `application.properties`의 `spring.ai.places.api-key` | 코드 조사 범위에서 실제 사용처 미확인 — 사용 전 grep으로 재확인 필요 |

## 5. 알려진 구조적 이슈 (건드릴 때 주의)

2026-07-03에 `bugfix-proposals.md`를 기준으로 1/2a/3/5/6/7번을 수정했습니다. 아직 남아있는 항목(2b, 4)은 여전히
**명시적으로 요청받지 않는 한 임의로 고치지 마세요** (CLAUDE.md의 "작은 변경에 큰 리팩터링 금지" 원칙).

1. ~~`SecurityConfig`의 `/**` permitAll 규칙이 사실상 화이트리스트를 무력화함~~ → **수정 완료**: 도달 불가능하던 `anyRequest().authenticated()`를 제거하고 `anyRequest().permitAll()`로 정리(동작 변화 없음, §3 참고).
2. ~~`PasswordEncoder` 이중 정의(NoOp 실사용, BCrypt 죽은 코드)~~ → **관리자 비밀번호(2a) 수정 완료**: `SecurityConfig`가 `BCryptPasswordEncoder`를 반환하도록 변경, `PrincipalDetailsService`의 `{noop}` 접두사 제거, 죽은 코드였던 `PasswordConfig` 삭제. **게스트 예약 비밀번호(2b, `Booking.password`)는 아직 평문 그대로입니다** — 파급 범위가 커서 별도 작업으로 보류.
3. ~~`Payment` 엔티티는 Repository/Service/Controller 없는 스텁~~ → **수정 완료**: `Payment`/`Method`/`PayStatus` 3개 파일 삭제. 결제 기능이 필요해지면 새로 설계해야 합니다.
4. CORS 설정 전무 — **미조치 유지** (현재 동일 오리진 서빙이라 필요성 없음, 별도 오리진 프론트 계획이 생기면 그때 추가).
5. ~~`HostService.isAvailable()` 네이밍이 반환값과 반대 의미~~ → **수정 완료**: `hasDateConflict(startDate, endDate)`로 이름 변경, 동작은 동일.
6. ~~`OpenAiClient`가 외부 API 실패 응답을 예외로 변환하지 않음~~ → **수정 완료**: 비2xx 응답/빈 body 시 `CustomException.ExternalApiException`을 던지도록 변경. `ChatService.askBot()`의 기존 catch-all이 그대로 처리.
7. ~~`springdoc-openapi-starter-webmvc-ui`가 `build.gradle`에 버전 중복 선언되어 있음~~ → **수정 완료**: 2.0.2 중복 선언 제거, 2.8.13만 유지.

## 6. 새 기능을 추가할 때 체크리스트

1. 어느 도메인 패키지(`Booking`/`Host`/`Verify`/`OpenAi`)에 속하는지 먼저 판단하고, 해당 패키지의 기존 레이어 구조(§1)를 그대로 따릅니다. (`Payment`는 2026-07-03에 미사용 스텁을 삭제했으므로, 결제 기능이 필요하면 새로 설계해야 합니다.)
2. 응답 DTO에 비밀번호/시크릿 필드가 섞여 나가지 않는지 확인합니다(`BookingSimpleDTO` 패턴 참고).
3. 새 API 경로가 보호되어야 하는지 판단하고, 보호가 필요하면 `SecurityConfig`에 명시적으로 추가합니다 — 기본값은 "공개"입니다(§3).
4. 예약/차단 날짜 로직을 건드린다면 `HostService.getUnavailableDates()`가 `PENDING`도 포함한다는 규칙을 유지할지 먼저 확인합니다.
5. 외부 API(CoolSMS/Gemini)를 호출하는 코드는 mock 가능한 클라이언트 뒤에 두고, 실패/timeout 케이스를 테스트합니다.
6. 변경 후 `BookingServiceTest`/`HostServiceTest`/`VerifyServiceTest` 중 관련된 것을 먼저 실행하고, 가능하면 전체 테스트도 실행합니다.
