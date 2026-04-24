![header](https://capsule-render.vercel.app/api?type=waving&color=gradient&customColorList=14&height=240&section=header&text=PENBOT&fontSize=72&fontColor=ffffff&animation=fadeIn&desc=펜션%20예약을%20도와주는%20AI%20챗봇%20서비스&descSize=20&descAlignY=72)

<div align="center">

[![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=spring-security&logoColor=white)](https://spring.io/projects/spring-security)
[![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white)](https://www.mysql.com/)
[![Redis](https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white)](https://redis.io/)
[![OpenAI](https://img.shields.io/badge/OpenAI-412991?style=for-the-badge&logo=openai&logoColor=white)](https://openai.com/)
[![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://www.docker.com/)
[![Nginx](https://img.shields.io/badge/Nginx-009639?style=for-the-badge&logo=nginx&logoColor=white)](https://nginx.org/)

[![Live Demo](https://img.shields.io/badge/🌐_Live_Demo-penbot.vercel.app-0F6E56?style=for-the-badge)](https://penbot.vercel.app/)

</div>

<br/>

## 📌 서비스 소개

펜션 예약을 도와주는 AI 챗봇 서비스가 들어간 펜션 사이트입니다.

- 손님은 **비회원으로** 캘린더를 통해 예약을 신청할 수 있습니다
- **AI 챗봇**은 예약 대리인이 아닌 펜션 정보 도우미로, 궁금한 사항을 24시간 안내합니다
- 관리자는 **전용 대시보드**에서 예약 승인, 날짜 차단, 예약 현황을 한눈에 관리합니다

<br/>

## 서비스 주소
https://penbot.site/

<img width="2761" height="1445" alt="image" src="https://github.com/user-attachments/assets/fd4a2e44-397f-49e1-8319-a642ad9fd65f" />

<br/>

## 🛠 기술 스택

### Backend
- **Spring Boot** : 비즈니스 로직에 집중할 수 있는 Java 기반 프레임워크
- **Spring Security** : 관리자와 사용자의 역할 기반 접근 제어(RBAC) 및 보안 강화

### Frontend
- **Thymeleaf** : 초기 로딩 속도 개선을 위한 SSR 방식 뷰 템플릿
- **Vanilla JS** : 외부 라이브러리 의존성 최소화, 가벼운 챗봇 UI 및 동적 인터랙션 구현

### Database & Cache
- **MySQL** : 예약 관리, 예약 날짜 관리 등 데이터 저장
- **Redis** : 인증번호 임시 캐싱

### Infra
- **Oracle Cloud & Docker** : 일관된 실행 환경 구축 및 컨테이너 기반 배포
- **Nginx** : 리버스 프록시 및 SSL 인증서 적용
- **GitHub Actions** : CI/CD 자동화 파이프라인

### 외부 API
- **CoolSMS** : 휴대폰 인증 알림
- **Naver Map API** : 펜션 위치 시각화
- **OpenAI** : AI 챗봇 연동

<br/>

## ✨ 주요 기능

### 🏕️ 펜션 예약
- 캘린더를 통해 날짜별 요금 및 예약 가능 여부 실시간 확인
- **비회원 예약** — 이름, 연락처만으로 예약 신청 (1~2회 방문 특성에 맞춘 UX)
- **CoolSMS** 기반 휴대폰 인증으로 예약자 본인 확인
- 예약 확인 비밀번호로 예약 내역 조회

### 🤖 AI 챗봇
- OpenAI API 연동 펜션 정보 안내 챗봇
- 역할을 **CS 응대 및 가이드로 제한** — DB 직접 접근 및 API 호출 차단
- 할루시네이션 방지를 위한 구체적인 프롬프트 엔지니어링 적용
- 예약 요청 시 관리자 연락처와 예약 신청 기능으로 유도

### 🔧 관리자 대시보드
- **Role-Based Access Control** — Spring Security 기반 HOST 권한 인가 로직
- 예약 승인 / 거절 / 취소 등 예약 상태 관리
- 특정 날짜 예약 차단 (내부 수리, 휴무 등)
- **보안 강화** — HttpOnly 쿠키, HTTPS(SSL), Secure 속성으로 세션 탈취 방어

<br/>

## 🧪 Unit Test

> 배포 후 발생할 수 있는 치명적 버그를 사전에 0건으로 차단

예약 시스템 특성상 날짜 중복, 차단된 날짜 예약 등 **데이터 무결성**이 깨지면 치명적인 비즈니스 오류가 발생합니다.  
**JUnit5 + Mockito**를 활용해 비즈니스 로직 단위 테스트를 작성하여 배포 전 논리적 오류를 사전에 차단했습니다.

| 테스트 시나리오 | 설명 |
|---|---|
| 예약 중복 방지 | 이미 예약된 날짜에 중복 요청 시 예외 발생 검증 |
| 관리자 차단 로직 | 차단 날짜 예약 시도 시 `isAvailable()` 정상 작동 확인 |
| 데이터 조회 검증 | 예약 날짜 + 차단 날짜 통합 예약 불가 리스트 누락 없이 반환 확인 |
| 인증번호 로직 | 인증번호 발송 및 검증 로직 정상 작동 검증 |

**엣지 케이스** — 예약 날짜 겹침 / 차단 날짜 충돌 / 인증번호 검증 실패 → **예외 발생 시나리오 100% 커버**

<br/>

## 🏗 아키텍처

<img width="6874" height="3728" alt="image" src="https://github.com/user-attachments/assets/672219dd-d176-4c00-9a58-b07486c0b42f" />


<br/>

## 📋 개발 과정

### 1. 프로젝트 기획
- **기능 명세서 작성** — 예약이 잘 되는 펜션 사이트 분석 후 필요 기능 정의
- **ERD 작성** — 데이터 간 관계를 고려한 정규화된 ERD 설계, 데이터 무결성 확보
- **UI/UX 설계** — Figma를 사용한 화면 설계

### 2. 기술 선택 이유

> **React → Thymeleaf + Vanilla JS 전환**  
> React의 복잡한 상태 관리와 생명주기 처리가 이 프로젝트에서 오버 엔지니어링임을 인지하고 전환했습니다.  
> 유행하는 기술보다 **프로젝트 규모에 맞는 기술**을 선택하는 것이 완성도를 높이는 길임을 배웠습니다.

> **비회원 예약 프로세스 도입**  
> 1년에 1~2회 방문하는 펜션 사이트 특성상 회원가입은 불필요한 마찰임을 파악했습니다.  
> **통상적인 관례보다 실제 사용자 패턴을 분석해 설계**하는 것이 더 나은 UX를 만든다는 것을 배웠습니다.

<br/>

## 💡 배운 점

**LLM의 한계와 보안 중심 설계**

초기 설계는 챗봇이 DB에 직접 접근해 예약을 수행하는 방식이었습니다.  
할루시네이션 현상과 보안 위험이 크다고 판단해 챗봇 역할을 CS 응대로 축소했습니다.  
생성형 AI를 시스템에 통합할 때는 **불확실성을 완벽하게 통제**할 수 있어야 하며,  
데이터 무결성과 보안을 최우선으로 시스템 역할을 분리해야 함을 배웠습니다.

**향후 발전 계획**

카카오페이 등 외부 결제 API 연동 — 현재 관리자가 입금을 직접 확인 후 수동으로 승인하는 프로세스를 자동화할 예정입니다.

<br/>

## 📬 문의

- **개발자** : [이현우](https://github.com/LeeHyunWoo02)
- **서비스** : [penbot.vercel.app](https://penbot.vercel.app/)

![footer](https://capsule-render.vercel.app/api?type=waving&color=gradient&customColorList=14&height=150&section=footer)
