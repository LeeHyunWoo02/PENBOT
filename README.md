### 🚩 **펜션 예약 챗봇 프로젝트 기획 (React + Spring Boot + MySQL + Redis)**

---

### **1. 전체 시스템 흐름**

1. 손님이 펜션 홈페이지(React) 접속
2. 챗봇(React + OpenAI)으로 "예약하고 싶어요" 등 대화 시작
3. 챗봇이 날짜, 객실, 인원 등 필요한 정보 자연스럽게 질문
4. 예약 가능 여부를 Spring Boot(백엔드)에서 DB(MySQL)와 연동해 확인
5. 예약 확정 시 알림(문자/이메일) 발송
6. 관리자는 관리자 페이지에서 예약 내역과 대화 내역 확인

---

### **2. 구현 상세 기능**

### **손님(사용자)**

- **대화형 예약**
    - 챗봇이 원하는 날짜, 객실 타입, 인원 등 단계별로 안내
    - 이미 예약된 날짜/객실은 챗봇이 안내 후 다른 날짜 제안
    - 예약 내역 확인/취소/변경도 대화로 가능
- **펜션 정보 안내**
    - 위치, 체크인/체크아웃, 부대시설, 가격 등 FAQ 자동 안내
- **알림**
    - 예약/취소/변경 시 문자(SMS) 또는 이메일 자동 발송

### **관리자**

- **대시보드**
    - 전체 예약 현황(달력, 테이블)
    - 오늘/이번주 예약 건, 취소 건 실시간 확인
    - 대화 로그(FAQ, 챗봇 응답 품질 모니터링)
- **예약/블록 관리**
    - 예약 직접 추가/수정/삭제
    - 객실별/날짜별 예약 불가 처리(예: 유지보수, 휴무 등)

```
[users]             [bookings]             [blocked_dates]
┌─────────────┐     ┌───────────────┐      ┌───────────────┐
│ id (PK)     │◄──┐ │ id (PK)       │      │ id (PK)       │
│ name        │   │ │ user_id (FK)  │      │ start_date    │
│ phone       │   │ │ start_date    │      │ end_date      │
│ email       │   │ │ end_date      │      │ reason        │
│ password    │   │ │ headcount     │      └───────────────┘
│ role        │   │ │ status        │
│ company     │   │ │ created_at    │
└─────────────┘   │ └───────────────┘
                  │
                  │
                  ▼
           [chat_logs]
        ┌────────────────────────────┐
        │ id (PK)                   │
        │ booking_id (FK, nullable) │
        │ user_id (FK)              │
        │ sender                    │
        │ message                   │
        │ created_at                │
        └────────────────────────────┘

         ▲
         │
         │
   [payments]
┌───────────────┐
│ id (PK)       │
│ booking_id(FK)│
│ amount        │
│ status        │
│ method        │
│ paid_at       │
└───────────────┘

```

![PENBOT.png](../../Users/User/Downloads/PENBOT.png)

- 엔티티 설명
  - **users**: 손님/관리자 계정
  - **bookings**: 예약 (누가, 언제, 인원, 상태 등)
  - **payments**: 결제 내역 (예약별 금액, 결제방법, 상태 등)
  - **blocked_dates**: 예약불가 기간 및 사유 (점검, 휴무 등)
  - **chat_logs**: 대화 기록 (질문/응답, 타임스탬프 등)
- 관계 설명
  - **users (1) — (N) bookings**
    - 한 명의 유저가 여러 예약을 가질 수 있음
  - **bookings (1) — (N) chat_logs**
    - 하나의 예약에 여러 대화 로그가 연결될 수 있음
    - 예약과 관련 없는 채팅은 booking_id가 NULL
  - **users (1) — (N) chat_logs**
    - 한 유저가 여러 대화 로그(상담, 문의 등)를 남김
  - **bookings (1) — (1) payments**
    - 예약 1건마다 1건의 결제 정보(확장시 N도 가능)
  - **blocked_dates**
    - 독립 테이블(특정 기간 예약불가 관리, bookings와 직접적 FK 없이 별도 관리)