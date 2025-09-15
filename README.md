# 펜션 예약 챗봇 프로젝트 (PENBOT)

**기술 스택:**  
- **Backend:** Spring Boot  
- **Frontend:** React  
- **Database:** MySQL  
- **Cache:** Redis  
- **AI:** OpenAI, Gemini

---

## 프로젝트 소개

PENBOT은 펜션 예약 과정을 자동화하고, 손님 및 관리자 모두에게 편리한 챗봇 서비스를 제공하는 프로젝트입니다. 손님은 자연어 챗봇을 통해 예약을 진행하고, 관리자는 예약 관리, 차단일 관리, 대화 내역 확인 등 다양한 기능을 손쉽게 사용할 수 있습니다.

---

## 전체 시스템 흐름

1. 손님이 펜션 홈페이지(React)에 접속합니다.
2. 챗봇(React + OpenAI)과 "예약하고 싶어요" 등 자연스러운 대화로 예약을 시작합니다.
3. 챗봇이 날짜, 객실, 인원 등 정보를 질문합니다.
4. Spring Boot 백엔드가 DB(MySQL)와 연동하여 예약 가능 여부를 확인합니다.
5. 예약 확정 시 알림(문자/이메일)을 발송합니다.
6. 관리자는 관리자 페이지에서 예약 내역과 대화 내역을 확인할 수 있습니다.

---

## 주요 기능

### 손님(사용자)
- 챗봇을 통한 예약 진행 (날짜, 객실, 인원 등)
- 예약 가능 여부 실시간 확인
- 예약 내역 및 결제 정보 확인
- 예약 확정/취소/변경

### 관리자
- 예약 전체/상세/삭제/상태 변경 관리
- 예약 불가 기간(차단일) 등록 및 삭제
- 손님/관리자 계정 관리 (조회/상세/삭제)
- 대화 기록 조회 및 분석
- 결제 내역 확인 및 상태 관리

---

## 주요 엔티티

- **users**: 손님/관리자 계정 정보
- **bookings**: 예약 정보 (누가, 언제, 인원, 상태 등)
- **payments**: 결제 내역 (예약별 금액, 결제방법, 상태 등)
- **blocked_dates**: 예약불가 기간 및 사유 (점검, 휴무 등)
- **chat_logs**: 대화 기록 (질문/응답, 타임스탬프 등)

---

## 디렉토리 구조 (예시)

```
src/
├── main/
│   └── java/
│       └── Project/
│           └── PENBOT/
│               ├── Booking/          # 예약 관련 API, DTO, Service
│               ├── ChatAPI/         # 챗봇(Gemini/OpenAI) 관련 API
│               ├── Host/            # 관리자(호스트) 기능 API, Service, DTO
│               └── ...              # 기타 공통/설정 코드
├── resources/
│   └── application.yml              # 환경설정
└── ...
```

---

## API 샘플

- **예약 전체 조회:** `GET /api/host/bookings`
- **예약 상세 조회:** `GET /api/host/bookings/{bookingId}`
- **예약 삭제:** `DELETE /api/host/bookings/{bookingId}`
- **유저 목록/상세/삭제:** `GET /api/host/users`, `GET /api/host/users/{userId}`, `DELETE /api/host/users/{userId}`
- **차단일 관리:** `POST/DELETE /api/host/blocked-dates`

**챗봇 API:**  
- `POST /api/gemini/chat` : 자연어로 예약 또는 주변 맛집 안내 등

---

## 설치/실행 방법

1. **백엔드**  
   - `./gradlew build`  
   - `java -jar build/libs/penbot.jar`
2. **프론트엔드**  
   - `npm install`  
   - `npm start`
3. **DB/Redis**  
   - MySQL/Redis 설치 후 환경변수 설정

---
## 사이트 
https://penbot.vercel.app/

## 시연 영상 
https://github.com/user-attachments/assets/8e922195-5e27-4807-a5f4-c62a230adfca

---
## 기타

- 문의: [이현우](https://github.com/LeeHyunWoo02)
