# 버그성 이슈 수정 제안서

`backend-architecture.md`에서 정리한 7가지 구조적 이슈에 대한 구체적인 수정 방안입니다.
**이 문서는 제안일 뿐이며, 아직 어떤 코드도 수정하지 않았습니다.** 각 항목을 검토한 뒤 진행 여부를 알려주시면
그때 실제 코드 수정을 실행합니다. 항목별로 독립적이므로 일부만 승인하고 나머지는 보류해도 됩니다.

## 요약

| # | 이슈 | 실제 위험도 | 제안 | 결정 필요 |
|---|---|---|---|---|
| 1 | SecurityConfig 화이트리스트 순서 | 낮음 (현재도 host/admin은 보호됨, dead code 정리 성격) | 죽은 규칙 제거 + 주석 명시 | 아니오 (바로 진행 가능) |
| 2a | 관리자 비밀번호 평문(NoOp) | 높음 | BCrypt로 전환 | **예** — 기존 DB의 admin 비밀번호를 재해싱해야 함 |
| 2b | 게스트 예약 비밀번호 평문(Integer PIN) | 중간 | BCrypt로 전환 (스키마 변경 수반) | **예** — 이번 라운드 포함 여부 |
| 3 | Payment 미사용 스텁 | 없음 (죽은 코드일 뿐) | 유지 또는 삭제 | **예** — 어느 쪽을 원하는지 |
| 4 | CORS 설정 없음 | 없음 (동일 오리진 서빙 중) | 변경 안 함 권장 | **예** — 별도 오리진 프론트 계획 있는지 |
| 5 | `HostService.isAvailable()` 네이밍 반전 | 없음 (가독성 문제) | 이름 변경(`hasDateConflict`) | 아니오 (바로 진행 가능) |
| 6 | `OpenAiClient` 에러 처리 주석 처리 | 낮음 (최종 사용자 결과는 동일, 디버깅만 어려움) | 커스텀 예외로 명시적 처리 | 아니오 (바로 진행 가능) |
| 7 | `build.gradle` springdoc 버전 중복 선언 | 없음 (Gradle이 자동으로 최신 버전 채택 중) | 중복 줄 제거 | 아니오 (바로 진행 가능) |

---

## 1. SecurityConfig 화이트리스트 순서 (dead code)

**현재 코드** (`Config/SecurityConfig.java:53-60`):
```java
.authorizeHttpRequests((auth) -> auth
        .requestMatchers("/css/**", "/images/**", "/js/**", "/favicon.ico").permitAll()
        .requestMatchers("/api/host/**").hasRole("HOST")
        .requestMatchers("/admin/**").hasRole("HOST")
        .requestMatchers("/api/admin/login").permitAll()
        .requestMatchers("/**").permitAll()
        .anyRequest().authenticated()
);
```

**실제 동작**: `/api/host/**`, `/admin/**`는 더 구체적인 규칙이 먼저 등록되어 있어 지금도 정상적으로 `hasRole("HOST")`로 보호됩니다. 문제는 그 다음 `.requestMatchers("/**").permitAll()`이 이미 모든 요청과 매칭되기 때문에, 마지막 줄 `.anyRequest().authenticated()`는 **영원히 실행되지 않는 죽은 규칙**입니다. 이 줄은 "명시 안 된 경로는 인증 필요"라는 의도를 암시하지만 실제로는 그렇지 않아 오해를 유발합니다.

**제안 수정**:
```java
.authorizeHttpRequests((auth) -> auth
        .requestMatchers("/css/**", "/images/**", "/js/**", "/favicon.ico").permitAll()
        .requestMatchers("/api/host/**").hasRole("HOST")
        .requestMatchers("/admin/**").hasRole("HOST")
        .requestMatchers("/api/admin/login").permitAll()
        // 위에 명시되지 않은 모든 경로(예약/조회/채팅/정적 페이지)는 의도적으로 공개 API
        .anyRequest().permitAll()
);
```
동작은 100% 동일하고(모든 응답 코드 그대로), 죽은 줄만 정리됩니다.

**대안 (권장하지 않음, 참고용)**: "기본 차단 + 명시적 허용" 방식으로 뒤집는 방법도 있지만(`anyRequest().authenticated()`를 실제로 살리고 공개 경로를 전부 나열), 지금 공개되어야 하는 경로가 많고(`/`, `/reserve`, `/reservationInfo`, `/reservationCheck`, `/directions`, `/roomInfo`, `/api/bookings/**`, `/api/verify/**`, `/api/penbot/**`, `/swagger` 등) 하나라도 빠뜨리면 실제 사용자 기능이 깨집니다. 이번 요청 범위(버그 정리)에는 과합니다. 필요하면 별도로 진행 여부를 논의하시죠.

**검증 계획**: 수정 후 `/admin/dashboard`, `/api/host/bookings` 등에 비로그인 상태로 접근 시 여전히 401/403(리다이렉트)이 뜨는지, `/reserve` 등 공개 페이지는 계속 열리는지 수동 확인.

---

## 2a. 관리자 비밀번호 평문 비교

**현재 코드**:
- `Config/SecurityConfig.java:29-32` — 실제 사용되는 `PasswordEncoder` 빈이 `NoOpPasswordEncoder`(평문 비교)
- `Config/PasswordConfig.java` — `BCryptPasswordEncoder` 빈을 정의하지만 어디서도 주입되지 않는 죽은 코드
- `Host/Service/PrincipalDetailsService.java:28` — `.password("{noop}" + admin.getPassword())`로 평문 비밀번호를 그대로 감싸서 반환

**제안 수정**:
1. `SecurityConfig.passwordEncoder()`가 `NoOpPasswordEncoder.getInstance()` 대신 `new BCryptPasswordEncoder()`를 반환하도록 변경.
2. `PasswordConfig.java` 삭제 (같은 타입의 빈을 두 곳에서 정의할 필요 없음 — 1번으로 대체됨).
3. `PrincipalDetailsService.loadUserByUsername`에서 `"{noop}" +` 접두사 제거: `.password(admin.getPassword())`.

**⚠️ 반드시 함께 처리해야 하는 것 — DB 마이그레이션**:
코드만 바꾸면 **기존 관리자 계정으로 로그인이 즉시 실패합니다.** `admin` 테이블의 `password` 컬럼에는 지금 평문 값이 들어있는데, BCrypt 인코더는 이를 해시로 취급해 비교하므로 절대 일치하지 않습니다. 배포 전에 반드시 다음 중 하나를 해야 합니다.

- (a) 알고 있는 관리자 평문 비밀번호를 `new BCryptPasswordEncoder().encode("원래비밀번호")`로 해시를 생성해 `UPDATE admin SET password = '<생성된 해시>' WHERE username = '...'`로 직접 갱신
- (b) 1회성 마이그레이션 코드(`CommandLineRunner` 등)를 임시로 추가해 평문으로 보이는 admin 비밀번호를 감지해 자동으로 재해싱한 뒤, 마이그레이션 코드를 다시 제거

이 단계는 실제 운영 중인 관리자 계정 자격 증명과 직결되어 있어 **코드 리뷰만으로는 대신 처리할 수 없습니다.** 진행을 승인하실 때 (a)/(b) 중 어느 방식을 원하시는지, 그리고 관리자 계정이 로컬 DB에만 있는지 운영 DB에도 있는지 알려주세요.

**테스트**: 현재 로그인 성공/실패에 대한 테스트가 전혀 없습니다. `PrincipalDetailsServiceTest` 또는 `LoginFilter` 통합 테스트를 신규 작성해 "정상 비밀번호로 로그인 성공", "틀린 비밀번호로 401" 케이스를 커버하는 것을 권장합니다(CLAUDE.md의 보안 하네스 도입 방향과도 일치).

---

## 2b. 게스트 예약 비밀번호(4자리 PIN) 평문 비교

**현재 코드**: `Booking/Entity/Booking.java:42-43`의 `Integer password` 필드가 생성(`BookingConverter.toEntity`)과 조회(`BookingRepository.findByGuestNameAndGuestPhoneAndPassword`) 양쪽에서 평문 그대로 저장/비교됩니다.

**제안 수정 (범위가 가장 큼)**:
1. `Booking.password` 타입을 `Integer` → `String`(BCrypt 해시 저장)으로 변경
2. `BookingConverter.toEntity`: 저장 시 `passwordEncoder.encode(String.valueOf(requestDTO.getPassword()))`로 해시 생성 (converter가 static 메서드라 인코더 주입이 필요 — `BookingService`에서 해시까지 처리하고 `Booking.builder().password(hashed)`를 완성하는 방식으로 구조 조정 필요)
3. `BookingRepository.findByGuestNameAndGuestPhoneAndPassword` 삭제 — 해시는 매번 salt가 달라 DB에서 직접 값으로 필터링 불가능. 대신 `findByGuestNameAndGuestPhone(name, phone)`로 후보를 가져온 뒤, `BookingService.checkMyBooking`에서 각 후보마다 `passwordEncoder.matches(rawPassword, booking.getPassword())`로 순차 비교
4. `BookingRequestDTO`/`BookingLookupRequestDTO`의 `password` 필드는 입력 형식(4자리 숫자)은 그대로 유지 가능 — 서비스 계층에서만 인코딩/매칭 처리
5. **기존 예약 데이터 마이그레이션**: `ddl-auto=update`라 컬럼 타입이 `Integer`(정수) → `String`(문자열)으로 바뀌면 기존에 저장된 예약 레코드의 비밀번호 값을 자동으로 변환해주지 않습니다. 이미 예약이 있는 DB라면 별도 마이그레이션(기존 정수 값을 해시로 재저장하거나, 컬럼을 새로 만들고 데이터 이관)이 필요합니다.

**권장**: 이 항목은 엔티티/DTO 3곳 + 레포지토리 + 서비스 + 컨버터 + 프론트 JS(`booking.js`, `booking-check.js`) + 데이터 마이그레이션까지 걸쳐 있어 파급 범위가 큽니다. **2a(관리자 비밀번호)와 분리해서 별도 작업으로 진행하는 것을 권장**합니다. 이번 라운드에 포함하고 싶으시면 말씀해주세요.

---

## 3. Payment 엔티티 스텁

**현재 상태**: `Payment/Entity/{Payment,Method,PayStatus}.java` 3개 파일만 존재하고 Repository/Service/Controller가 전혀 없습니다. 컴파일이나 실행에 영향을 주지 않는 고립된 코드입니다 — 엄밀히는 "버그"가 아니라 미완성 스캐폴딩입니다.

**옵션**:
- **A (기본값, 변경 없음)**: 그대로 둔다. 향후 결제 기능 설계 시 참고용 스켈레톤으로 남김.
- **B**: 사용 계획이 없으면 3개 파일을 삭제해 죽은 코드 정리.

결제 기능을 만들 계획이 있는지에 따라 결정이 달라지므로, 승인 시 A/B 중 원하시는 쪽을 알려주세요.

---

## 4. CORS 설정 없음

**현재 상태**: 코드베이스 어디에도 `CorsConfigurationSource`/`@CrossOrigin` 설정이 없습니다. 다만 현재 프론트엔드(Thymeleaf)가 백엔드와 완전히 동일 오리진에서 서빙되고 있어 지금 당장 문제가 되는 상황은 아닙니다.

**제안**: 이번 라운드에서는 변경하지 않는 것을 권장합니다. CORS를 섣불리 열면(특히 `allowedOrigins("*")` + 자격 증명 허용 조합) 오히려 새로운 보안 취약점이 될 수 있습니다. 별도 도메인/포트에서 이 API를 호출할 구체적인 계획(예: 모바일 앱, 별도 SPA)이 생기면 그때 필요한 도메인만 허용하는 설정을 추가하는 것이 안전합니다.

승인 시 "지금은 변경 안 함"으로 처리하겠습니다. 만약 이미 별도 오리진에서 호출 중인 프론트가 있다면 알려주세요.

---

## 5. `HostService.isAvailable()` 네이밍 반전

**현재 코드** (`Host/Service/HostService.java:164-169`):
```java
public boolean isAvailable(LocalDate startDate, LocalDate endDate){
    boolean isBooked = bookingRepository.existsByStartDateLessThanEqualAndEndDateGreaterThanEqual(endDate, startDate);
    boolean isBlocked = blockedDateRepository.existsByStartDateLessThanEqualAndEndDateGreaterThanEqual(endDate, startDate);
    return (isBooked || isBlocked);
}
```
호출부 (`createBlockedDate`, L119): `if (isAvailable(...)) { throw new BlockedDateConflictException(); }` — 이름은 "예약 가능"이지만 실제로는 "겹침이 있어서 불가능"할 때 `true`를 반환합니다.

**제안 수정**: 이름만 `hasDateConflict`로 변경하고 동작은 그대로 유지합니다.
```java
public boolean hasDateConflict(LocalDate startDate, LocalDate endDate){
    boolean isBooked = bookingRepository.existsByStartDateLessThanEqualAndEndDateGreaterThanEqual(endDate, startDate);
    boolean isBlocked = blockedDateRepository.existsByStartDateLessThanEqualAndEndDateGreaterThanEqual(endDate, startDate);
    return (isBooked || isBlocked);
}
```
호출부도 `if (hasDateConflict(requestDTO.getEndDate(), requestDTO.getStartDate())) { throw new BlockedDateConflictException(); }`로 변경.

`HostServiceTest.java`를 확인한 결과 이 메서드를 직접 참조하는 테스트는 없어(간접적으로 `createBlockedDate` 테스트를 통해서만 커버) 이름 변경으로 인한 테스트 수정은 불필요할 것으로 보이나, 수정 후 `HostServiceTest` 전체를 실행해 회귀가 없는지 확인합니다.

**참고 (이번 항목과 별개, 참고용)**: `Booking/Service/BookingService.java`의 `isAvailable(BookingRequestDTO)`도 비슷한 문제가 있습니다 — 겹침이 있으면 예외를 던지고, 없으면 항상 `true`만 반환해서 실질적으로 `false`를 반환하는 경로가 없습니다. 즉 `createBooking()`의 `if (!isAvailable(requestDTO))` 분기는 도달 불가능한 죽은 코드입니다. 동작에 문제는 없지만(예외가 먼저 던져지므로) 원하시면 이것도 같이 정리할 수 있습니다. 이번 7개 이슈 목록에는 없었던 항목이라 별도로 말씀 주시면 진행하겠습니다.

---

## 6. `OpenAiClient` 에러 처리 주석 처리

**현재 코드** (`OpenAi/Client/OpenAiClient.java:35-49`):
```java
public OpenAiResponse getChatCompletion(OpenAiRequest requestDto) throws JsonProcessingException {
    ...
    ResponseEntity<OpenAiResponse> res = restTemplate.postForEntity(
            APIURL, entity, OpenAiResponse.class);
//        if (!res.getStatusCode().is2xxSuccessful() || res.getBody() == null) {
//            throw new JsonProcessingException();
//        }
    return res.getBody();
}
```
**원인 추정**: `JsonProcessingException`은 추상 클래스라 `new JsonProcessingException()`으로 직접 생성할 수 없어(컴파일 에러) 주석 처리로 우회한 것으로 보입니다.

**현재 실질 영향**: 외부 API가 실패 응답을 반환해도 예외 없이 `null`이 그대로 리턴되고, 이를 호출하는 `ChatService.askBot()`에서 `response.getChoices().get(0)...` 부분에서 `NullPointerException`이 발생 → 이미 있는 `catch (Exception e)`에 잡혀 결국 사용자에게는 동일한 고정 안내 문구가 나갑니다. 즉 **최종 사용자 경험은 지금도 동일**하지만, 원인이 불분명한 NPE로 로그에 남아 디버깅이 어렵습니다.

**제안 수정**:
1. `CustomException` 패키지에 새 예외 추가 (`ExternalApiException`), 기존 예외들과 동일한 패턴(기본 생성자 + 메시지 생성자, `RuntimeException` 상속):
```java
package Project.PENBOT.CustomException;

public class ExternalApiException extends RuntimeException {
    public ExternalApiException() {
        super("외부 API 호출에 실패했습니다.");
    }
    public ExternalApiException(String message) {
        super(message);
    }
}
```
2. `OpenAiClient.getChatCompletion`의 주석을 실제 코드로 교체하고 메서드 시그니처의 `throws JsonProcessingException`도 제거(더 이상 어떤 코드도 이 checked exception을 던지지 않으므로):
```java
public OpenAiResponse getChatCompletion(OpenAiRequest requestDto) {
    ...
    ResponseEntity<OpenAiResponse> res = restTemplate.postForEntity(
            APIURL, entity, OpenAiResponse.class);

    if (!res.getStatusCode().is2xxSuccessful() || res.getBody() == null) {
        throw new ExternalApiException("AI 응답을 가져오지 못했습니다.");
    }
    return res.getBody();
}
```
`ChatService.askBot()`은 이미 `catch (Exception e)`로 모든 예외를 잡고 있어 별도 수정이 필요 없습니다.

**테스트**: `ChatService`에 대한 기존 단위 테스트가 없습니다. `OpenAiClient`를 mock해서 (a) 정상 응답 시 파싱된 답변 반환, (b) 실패 응답(4xx/5xx 또는 null body) 시 고정 안내 문구 반환을 검증하는 테스트를 신규 작성하는 것을 권장합니다(CLAUDE.md의 외부 API 하네스 도입 방향과 일치).

---

## 7. `build.gradle` springdoc 버전 중복 선언

**현재 코드** (`build.gradle:28,30`):
```groovy
implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.13'// openAi
...
implementation 'net.nurigo:sdk:4.3.0' // coolSMS API
implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.0.2'	//Swagger
```
같은 모듈(`springdoc-openapi-starter-webmvc-ui`)이 서로 다른 버전(2.8.13, 2.0.2)으로 두 번 선언되어 있습니다. Gradle의 기본 버전 충돌 해결 전략(최신 버전 우선)에 따라 실제로는 이미 2.8.13이 적용되고 있어 **지금 당장 빌드/런타임 오류는 없습니다.** 다만 두 번 선언 자체가 혼란스럽고, 향후 누군가 2.0.2 줄을 "실제 사용 버전"으로 착각해 손댈 위험이 있습니다.

**제안 수정**: 중복 줄 제거, 최신 버전(2.8.13)만 유지:
```groovy
implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.13' // Swagger UI
```

**검증**: `.\gradlew.bat build` 후 `/swagger` 페이지가 정상적으로 뜨는지 확인.

---

## 진행 방식 제안

즉시 진행해도 안전한 항목: **1, 5, 6, 7** (동작 변화 없거나 순수 개선, 결정 필요 없음)
사용자 결정이 필요한 항목: **2a, 2b, 3, 4**

원하시면 "1,5,6,7만 먼저 진행" 또는 "2a도 포함, DB는 로컬만 있고 (a) 방식으로" 같은 식으로 알려주시면 그에 맞춰 실행하겠습니다.
