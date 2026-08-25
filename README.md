# 🏃 BattleRunner

![Android](https://img.shields.io/badge/Android-Native-3DDC84?style=flat-square&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=flat-square&logo=kotlin&logoColor=white)
![Google Maps](https://img.shields.io/badge/Google_Maps-4285F4?style=flat-square&logo=googlemaps&logoColor=white)

GPS 기반 러닝 기록에 Grid 영역 점령 게임을 결합한 Native Android 애플리케이션입니다.

Google Maps에 이동 경로를 실시간으로 표시하고, 두 사용자가 같은 기준 좌표의 Grid를 점령하며 경쟁하는 러닝 경험을 구현했습니다.

<br>

## 🖼️ 주요 화면

| 로그인 | 개인 러닝 | 배틀 | 백그라운드 러닝 알림 |
| --- | --- | --- | --- |
| <img src="https://github.com/user-attachments/assets/35734923-0387-46e1-b24d-fad49a5bbcdc" width="220" alt="로그인 화면"> | <img src="https://github.com/user-attachments/assets/961a10f2-74f9-4fcf-b6c7-51eacb3aacd6" width="250" alt="개인 러닝 화면"> | <img src="https://github.com/user-attachments/assets/595e909c-0a26-4bb9-b595-574faca9b78d" width="250" alt="배틀 화면"> | <img src="https://github.com/user-attachments/assets/c494952f-2d8f-4c6f-a062-c9573f9109b6" width="180" alt="백그라운드 러닝 알림 화면"> |
| 자체·Google·Kakao 로그인 | 위치·경로·시간·거리 기록 | Grid 영역 점령 진행 | Foreground Service 알림으로 시간·거리 기록 확인 |

| 러닝 결과 | Battle 결과 | 마이페이지 | 러닝 기록 캘린더 | 러닝 기록 상세 |
| --- | --- | --- | --- | --- |
| <img src="https://github.com/user-attachments/assets/684f53a9-1996-4626-89e5-ec950417d357" width="220" alt="러닝 결과 화면"> | <img src="https://github.com/user-attachments/assets/8ca912f5-ab2a-4af7-a4db-8a185a974e76" width="220" alt="Battle 결과 화면"> | <img src="https://github.com/user-attachments/assets/2e5dc23b-b98d-421f-8cfa-87cf6f27eede" width="220" alt="마이페이지 화면"> | <img src="https://github.com/user-attachments/assets/ddc3c2fc-c272-4fb9-bb26-108238fcbe63" width="220" alt="러닝 기록 캘린더 화면"> | <img src="https://github.com/user-attachments/assets/a7200214-e136-4cf0-b6b2-ece9004f29c0" width="220" alt="러닝 기록 상세 화면"> |
| 이동 경로와 운동 결과 확인 | 점령 영역과 승패 확인 | 프로필과 러닝·Battle 기록 조회 | 날짜별 러닝 기록 선택 | 이동 경로·거리·소요 시간 확인 |

<br>

## 📖 서비스 소개

BattleRunner는 사용자의 이동 시간과 거리만 기록하는 러닝 앱에 경쟁 요소를 더한 서비스입니다. 개인 러닝에서는 GPS 경로를 기록하고, Battle Mode에서는 동일한 공간 Grid를 두 사용자가 이동하며 점령합니다.

<br>

## 📌 프로젝트 정보

| 항목 | 내용 |
| --- | --- |
| 기간 | 2024.09 ~ 2024.12 |
| 형태 | 팀 프로젝트 |
| 인원 | Android 2명 / Backend 1명 |
| 플랫폼 | Native Android |
| 담당 | 길 추천을 제외한 Android Client 전반 |
| Backend | Spring Boot REST API 연동 |

> 본 프로젝트는 지속적으로 운영하는 서비스가 아닌 일회성 시연을 목적으로 개발되었으며, 시연 종료 후 소스코드를 GitHub에 공개했습니다.

<br>

## 👨‍💻 담당 역할

- 자체·Google·Kakao 로그인과 SQLite 기반 자동 로그인
- Google Maps·FusedLocationProviderClient 기반 실시간 위치 추적
- 이동 거리·시간 계산과 Polyline 경로 렌더링
- Foreground Service를 이용한 Background 위치 추적
- 공통 시작 좌표 기반 Battle Grid 생성과 영역 점령 로직
- MVVM, ViewModel·LiveData와 Repository Pattern 적용
- Retrofit 기반 Backend API 연동
- 마이페이지와 러닝·Battle 기록 조회
- 친구 검색·추가·삭제 UI와 SQLite 연동 로직 구현 (Backend API 미구현으로 기능 미완성)

Directions API 기반 길 추천은 Android 팀원이 구현했으며 본인의 담당 범위에서 제외합니다.

<br>

## 🛠️ Tech Stack

| Category | Stack |
| --- | --- |
| Core | Kotlin, Android SDK |
| UI | XML, ViewBinding, Fragment |
| Architecture | MVVM, Repository Pattern |
| State | ViewModel, LiveData |
| Location | FusedLocationProviderClient, Foreground Service |
| Map | Google Maps SDK, Polyline, Polygon |
| Network | Retrofit2, OkHttp3, Gson |
| Local Data | SQLite |
| Authentication | Google, Kakao, 자체 로그인 |

<br>

## ✨ 주요 구현

### 실시간 러닝 기록

FusedLocationProviderClient로 위치를 받아 이전 좌표와의 거리를 누적하고 Google Maps Polyline에 경로를 추가했습니다. Timer, 거리와 경로 상태는 ViewModel에서 관리해 화면이 재생성되어도 유지했습니다.

[Location Flow 자세히 보기↗️](./docs/LOCATION_FLOW.md)

### Background 위치 추적

러닝 중 화면이 꺼지거나 앱이 Background로 이동해도 기록이 중단되지 않도록 Foreground Service와 Notification을 구성했습니다.

### Grid 영역 점령 Battle

첫 사용자의 시작 좌표를 서버에 저장하고 두 사용자가 같은 좌표를 기준으로 Grid를 생성하게 했습니다. 현재 위치가 들어온 Polygon을 찾아 소유권을 갱신하고 지도 색상과 서버 상태에 반영했습니다.

[Battle Flow 자세히 보기↗️](./docs/BATTLE_FLOW.md)

### 생명주기를 고려한 상태 관리

Fragment가 직접 관리하던 Timer, 위치와 버튼 상태를 ViewModel·LiveData로 옮겨 화면 회전과 Fragment 재생성으로 정보가 초기화되는 문제를 해결했습니다.

[아키텍처 자세히 보기↗️](./docs/ARCHITECTURE.md)

### 위치 UI 최적화

위치가 들어올 때마다 Polyline을 새로 만들지 않고 기존 객체에 좌표만 추가했습니다. 모든 위치 변화에 Camera를 이동하지 않고 일정 거리 이상 이동한 경우에만 갱신해 화면 흔들림을 줄였습니다.

<br>

## 🔧 문제 해결

- 위치 권한과 Map 초기화 순서로 현재 위치 버튼이 동작하지 않던 문제 해결
- GPS 오차가 Polyline 흔들림으로 반영되는 문제를 위치 주기 조정으로 완화
- 화면 회전 시 Timer·거리·버튼 상태가 초기화되던 문제 해결
- Fragment Transaction과 Popup 시점 충돌을 DialogFragment로 개선
- Android와 Backend DTO 타입 불일치로 발생한 JSON Parsing 오류 해결

[Troubleshooting 자세히 보기↗️](./docs/TROUBLESHOOTING.md)

<br>

## ⚠️ 현재 한계

- GPS 신호 품질은 건물·실내 환경의 영향을 받습니다.
- 커뮤니티(친구) 기능은 UI와 로컬(SQLite) 로직만 구현되어 있고 Backend API가 없어 실제로 동작하지 않습니다.

<br>

## 📚 Documentation

| Document | Description |
| --- | --- |
| [Architecture](./docs/ARCHITECTURE.md) | MVVM과 Local·Remote 데이터 구조 |
| [Location Flow](./docs/LOCATION_FLOW.md) | GPS, 지도와 Background Tracking |
| [Battle Flow](./docs/BATTLE_FLOW.md) | Grid 생성과 영역 점령 처리 |
| [Troubleshooting](./docs/TROUBLESHOOTING.md) | Maps·GPS·Lifecycle 문제 해결 |
