# 🏗️ Architecture

## 🧭 전체 구조

```text
Activity / Fragment
        ↓ observe / event
ViewModel + LiveData
        ↓
Repository
  ├─ Retrofit ─ Spring Boot REST API ─ MySQL
  └─ SQLite

LocationCallback ─ ViewModel ─ Google Maps
Foreground Service ─ Background Tracking
```

MVVM과 Repository Pattern을 적용해 UI, 상태와 데이터 접근 책임을 분리했습니다.

## 🧱 계층별 책임

| Layer | Responsibility |
| --- | --- |
| Activity / Fragment | 화면 렌더링, 권한과 사용자 입력 처리 |
| ViewModel | 로그인·러닝·Battle 상태와 Timer 관리 |
| LiveData | 생명주기를 고려한 UI 상태 전달 |
| Repository | Local DB와 Remote API 접근 조정 |
| SQLite | 로그인 정보와 Local 기록 저장 |
| Retrofit | Spring Boot REST API 통신 |
| Service | Background 위치 추적 |

## 🗂️ 패키지 구조

```text
app
├── data
│   ├── local
│   ├── model
│   └── repository
├── network
├── service
├── ui
│   ├── splash
│   ├── login
│   ├── home
│   ├── battle
│   ├── community
│   └── mypage
└── utils
```

## 🔄 상태 관리 개선

초기에는 Timer, 위치와 버튼 상태를 Fragment에서 관리했습니다. 화면 회전이나 Fragment 재생성 시 값이 초기화되는 문제가 있어 ViewModel로 이동했습니다.

```text
LocationCallback / Button Event
  ↓
ViewModel State 변경
  ↓
LiveData 방출
  ↓
Fragment가 필요한 UI만 갱신
```

ViewModel은 Activity 재생성보다 오래 유지되므로 러닝의 경과 시간, 거리와 경로를 안정적으로 보존할 수 있었습니다.

## 💾 데이터 접근

ViewModel이 Retrofit이나 SQLite를 직접 호출하지 않고 Repository를 통해 접근합니다.

- Remote: 로그인, 사용자 정보, Battle 생성과 Grid 소유권 갱신
- Local: 자동 로그인 정보와 러닝·Battle 기록
- UI: 데이터 소스의 구체적인 저장 방식을 알지 않음

## 🔐 로그인 흐름

```text
Splash → SQLite 로그인 정보 확인
  ├─ 없음 → Login
  └─ 있음 → 저장된 방식으로 인증 → Main

Login → 자체 / Google / Kakao 인증
  ↓
Backend 로그인
  ↓
SQLite 저장
  ↓
Main
```

## ⚠️ 구조적 한계

- LiveData와 Fragment 중심 구조는 현재 Android 권장 Compose·Flow 구조와 차이가 있습니다.
- Battle 상대 위치가 진정한 실시간 Channel이 아닌 REST 기반 상태 갱신에 의존합니다.
