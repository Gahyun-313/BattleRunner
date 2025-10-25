# 🏃‍♀️ 배틀러너

> **“땅따먹기 기반 러닝 배틀 앱”**  
> 혼자서! 둘이서! 러너를 위한 러닝 앱

![메인배너이미지](https://private-user-images.githubusercontent.com/78289372/505643772-b6ecae53-d7bd-41ad-a7b3-ca8c1864d539.PNG?jwt=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJnaXRodWIuY29tIiwiYXVkIjoicmF3LmdpdGh1YnVzZXJjb250ZW50LmNvbSIsImtleSI6ImtleTUiLCJleHAiOjE3NjE0MDcwMDEsIm5iZiI6MTc2MTQwNjcwMSwicGF0aCI6Ii83ODI4OTM3Mi81MDU2NDM3NzItYjZlY2FlNTMtZDdiZC00MWFkLWE3YjMtY2E4YzE4NjRkNTM5LlBORz9YLUFtei1BbGdvcml0aG09QVdTNC1ITUFDLVNIQTI1NiZYLUFtei1DcmVkZW50aWFsPUFLSUFWQ09EWUxTQTUzUFFLNFpBJTJGMjAyNTEwMjUlMkZ1cy1lYXN0LTElMkZzMyUyRmF3czRfcmVxdWVzdCZYLUFtei1EYXRlPTIwMjUxMDI1VDE1MzgyMVomWC1BbXotRXhwaXJlcz0zMDAmWC1BbXotU2lnbmF0dXJlPTg0ZmRiMDQ3OWQyYWVkODhiYTk1NDVhYTdjOWE1ZTYwMDcyYzlhOGM0NzJiNjQ4Y2I5NGZlOTUwZmI0MzQ4MjAmWC1BbXotU2lnbmVkSGVhZGVycz1ob3N0In0.XQsM_nRPXRUERcuVVZiCOc8OiDB_Qh0_2UoZqXdUSog)

<br>

## ⚙️ 기술 스택

### 📱 Android (Client)

| 구분 | 기술 | 설명 |
|------|------|------|
| **언어 / 구조** | `Kotlin`, `MVVM`, `ViewModel`, `LiveData`, `ViewBinding` | 구조적 상태 관리 및 UI 자동 갱신 |
| **지도 / 위치 추적** | `Google Maps SDK`, `FusedLocationProviderClient`, `Polyline`, `Marker` | GPS 기반 실시간 위치 추적 및 경로 표시 |
| **네트워크 통신** | `Retrofit2`, `OkHttp3`, `Gson`, `Coroutine` | 서버와 비동기 REST API 통신 |
| **로컬 데이터 저장** | `SQLite`, `DBHelper`, `SharedPreferences` | 자동 로그인 / 러닝 기록 로컬 저장 |
| **백그라운드 서비스** | `Foreground Service`, `NotificationManager` | 러닝 중 거리·시간 실시간 표시 |
| **기타** | `Logcat`, `Gradle`, `Google Play Services` | 디버깅, 빌드, 권한 관리 |

### 🌐 공통 / 협업 환경

| 구분 | 도구 | 설명 |
|------|------|------|
| **버전 관리** | `Git`, `GitHub` | 브랜치 전략 및 포트폴리오 공개 |
| **테스트 / 디버깅** | `Postman`, `Swagger UI`, `ADB Logcat` | API 검증 및 통신 점검 |
| **디자인 / 문서화** | `Figma`, `draw.io`, `Excalidraw` | UI 디자인 및 시스템 구조 다이어그램 제작 |

<br>
    
## ✨ 핵심 기능

| 기능 | 설명 | 
|------|------|
| 🗺️ **그리드 점령 시스템** | 250m x 250m 구역 단위로 영역 점령 | ![Grid](./assets/grid_overlay.png) |
| ⚔️ **실시간 배틀 매칭** | 두 사용자가 같은 지역에서 달리며 점령 경쟁 | ![Battle](./assets/battle_start.png) |
| 🔑 **로그인 / 자동로그인** | 자체, Google, Kakao 로그인 지원 | ![Login](./assets/login_screen.png) |
| 💾 **기록 저장** | 러닝/배틀 기록을 SQLite 및 서버(MySQL)에 저장 | ![History](./assets/record_list.png) |
| 📊 **결과 리포트** | 점령률, 거리, 시간 기반 결과 리포트 표시 | ![Report](./assets/battle_report.png) |

  <br>
  
![로그인](https://private-user-images.githubusercontent.com/78289372/505643778-5766ce82-5597-4344-a38c-dc48d33e6ebb.PNG?jwt=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJnaXRodWIuY29tIiwiYXVkIjoicmF3LmdpdGh1YnVzZXJjb250ZW50LmNvbSIsImtleSI6ImtleTUiLCJleHAiOjE3NjE0MDc4MDcsIm5iZiI6MTc2MTQwNzUwNywicGF0aCI6Ii83ODI4OTM3Mi81MDU2NDM3NzgtNTc2NmNlODItNTU5Ny00MzQ0LWEzOGMtZGM0OGQzM2U2ZWJiLlBORz9YLUFtei1BbGdvcml0aG09QVdTNC1ITUFDLVNIQTI1NiZYLUFtei1DcmVkZW50aWFsPUFLSUFWQ09EWUxTQTUzUFFLNFpBJTJGMjAyNTEwMjUlMkZ1cy1lYXN0LTElMkZzMyUyRmF3czRfcmVxdWVzdCZYLUFtei1EYXRlPTIwMjUxMDI1VDE1NTE0N1omWC1BbXotRXhwaXJlcz0zMDAmWC1BbXotU2lnbmF0dXJlPTkzNDZmNGIzMjk2MTg4ZTA4NjVkODhkMGFhZmRhZDk1MzBjMzA3NjQ0ODA0MWU2ZjQwYWM1OGEwYTUyYzRjYzEmWC1BbXotU2lnbmVkSGVhZGVycz1ob3N0In0.CjM8m7ZNyKAcXwL_bkaIQQEUj_cR4zkm6xnibKdEp2I)
![개인러닝](https://private-user-images.githubusercontent.com/78289372/505643775-eceda034-64df-4eb2-b182-8f2da822be96.PNG?jwt=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJnaXRodWIuY29tIiwiYXVkIjoicmF3LmdpdGh1YnVzZXJjb250ZW50LmNvbSIsImtleSI6ImtleTUiLCJleHAiOjE3NjE0MDc4MDcsIm5iZiI6MTc2MTQwNzUwNywicGF0aCI6Ii83ODI4OTM3Mi81MDU2NDM3NzUtZWNlZGEwMzQtNjRkZi00ZWIyLWIxODItOGYyZGE4MjJiZTk2LlBORz9YLUFtei1BbGdvcml0aG09QVdTNC1ITUFDLVNIQTI1NiZYLUFtei1DcmVkZW50aWFsPUFLSUFWQ09EWUxTQTUzUFFLNFpBJTJGMjAyNTEwMjUlMkZ1cy1lYXN0LTElMkZzMyUyRmF3czRfcmVxdWVzdCZYLUFtei1EYXRlPTIwMjUxMDI1VDE1NTE0N1omWC1BbXotRXhwaXJlcz0zMDAmWC1BbXotU2lnbmF0dXJlPWI5NmMxMzdhMzZlNDUxMWFmY2JkMzM1NWYzODk1NjVjOWU0M2IyNjk1ZWFiYjQ5ZDMwMGE3YzIxZTBkNmE3ZmUmWC1BbXotU2lnbmVkSGVhZGVycz1ob3N0In0.ZSUwc7UWKorlDOtIptmmplai0d6DnJFQy7bQsrheY8k)
![배틀러닝](https://private-user-images.githubusercontent.com/78289372/505643770-59340b30-575d-4abe-868a-7fafa8622eca.PNG?jwt=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJnaXRodWIuY29tIiwiYXVkIjoicmF3LmdpdGh1YnVzZXJjb250ZW50LmNvbSIsImtleSI6ImtleTUiLCJleHAiOjE3NjE0MDc4MDcsIm5iZiI6MTc2MTQwNzUwNywicGF0aCI6Ii83ODI4OTM3Mi81MDU2NDM3NzAtNTkzNDBiMzAtNTc1ZC00YWJlLTg2OGEtN2ZhZmE4NjIyZWNhLlBORz9YLUFtei1BbGdvcml0aG09QVdTNC1ITUFDLVNIQTI1NiZYLUFtei1DcmVkZW50aWFsPUFLSUFWQ09EWUxTQTUzUFFLNFpBJTJGMjAyNTEwMjUlMkZ1cy1lYXN0LTElMkZzMyUyRmF3czRfcmVxdWVzdCZYLUFtei1EYXRlPTIwMjUxMDI1VDE1NTE0N1omWC1BbXotRXhwaXJlcz0zMDAmWC1BbXotU2lnbmF0dXJlPWNjOTdmNmMxMDc5ZWQwMTQ5NDY4YWFjM2FlYmQ5MzcxZTM3NDM1MTQxNTk2ZWVlMzFkY2FjMTJhYzUxOWZkMDcmWC1BbXotU2lnbmVkSGVhZGVycz1ob3N0In0.nUJfkp3QeKJ_YXwKSG5VOQIjZVXyWaXwLbk8b_eKys8)
![마이페이지](https://private-user-images.githubusercontent.com/78289372/505643771-d51197f9-f676-46b9-868f-a70a455ba153.PNG?jwt=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJnaXRodWIuY29tIiwiYXVkIjoicmF3LmdpdGh1YnVzZXJjb250ZW50LmNvbSIsImtleSI6ImtleTUiLCJleHAiOjE3NjE0MDgwNTQsIm5iZiI6MTc2MTQwNzc1NCwicGF0aCI6Ii83ODI4OTM3Mi81MDU2NDM3NzEtZDUxMTk3ZjktZjY3Ni00NmI5LTg2OGYtYTcwYTQ1NWJhMTUzLlBORz9YLUFtei1BbGdvcml0aG09QVdTNC1ITUFDLVNIQTI1NiZYLUFtei1DcmVkZW50aWFsPUFLSUFWQ09EWUxTQTUzUFFLNFpBJTJGMjAyNTEwMjUlMkZ1cy1lYXN0LTElMkZzMyUyRmF3czRfcmVxdWVzdCZYLUFtei1EYXRlPTIwMjUxMDI1VDE1NTU1NFomWC1BbXotRXhwaXJlcz0zMDAmWC1BbXotU2lnbmF0dXJlPWU1OTg5ZDk3OGE4MWQzZGU0ZTRkYzc4Njg3MTkwNjBkZGE2ZTVmNWI5YjUyMjk3NGM1MjI0NTI0Mzg1MDY0NDkmWC1BbXotU2lnbmVkSGVhZGVycz1ob3N0In0.RN51Nq87Qbz7ZiDAg6Inbj6omEB8pRfIezTUIOqtgts)


<br>

## 🧩 시스템 구조

| Android App | Spring Boot Server |
|--------------|-------------------|
| Kotlin (MVVM), XML, Retrofit, Google Maps | Spring Boot, JPA, MySQL, Swagger |
| SQLite 로컬 저장 | REST API 기반 데이터 통신 |
| 로그인/배틀/맵 관리 UI | Controller-Service-Entity 구조 |


<br>

## Problem Solving Example

| 현재 위치 버튼 비활성화 이슈 | 러닝 결과 화면 팝업화 |
|-------|-------|
| <img src="https://private-user-images.githubusercontent.com/78289372/505643769-5a547d57-a190-48a5-8669-b96c31cbbf77.PNG?jwt=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJnaXRodWIuY29tIiwiYXVkIjoicmF3LmdpdGh1YnVzZXJjb250ZW50LmNvbSIsImtleSI6ImtleTUiLCJleHAiOjE3NjE0MDc1MzMsIm5iZiI6MTc2MTQwNzIzMywicGF0aCI6Ii83ODI4OTM3Mi81MDU2NDM3NjktNWE1NDdkNTctYTE5MC00OGE1LTg2NjktYjk2YzMxY2JiZjc3LlBORz9YLUFtei1BbGdvcml0aG09QVdTNC1ITUFDLVNIQTI1NiZYLUFtei1DcmVkZW50aWFsPUFLSUFWQ09EWUxTQTUzUFFLNFpBJTJGMjAyNTEwMjUlMkZ1cy1lYXN0LTElMkZzMyUyRmF3czRfcmVxdWVzdCZYLUFtei1EYXRlPTIwMjUxMDI1VDE1NDcxM1omWC1BbXotRXhwaXJlcz0zMDAmWC1BbXotU2lnbmF0dXJlPTkwZDlmZTVmZDUyNzdjZmY5OWM0ZDc5ZTgwY2Y3ODNhYjhkMzMzNGFiM2ZiOWJhODgwZjg4Yjc1YjZkNjg0ZjcmWC1BbXotU2lnbmVkSGVhZGVycz1ob3N0In0.b8H2Di1CJ0Uu6CybmWQnwWlJpSqUZLdEUUKiKx3-Uo4" width="500" /> | <img src="https://private-user-images.githubusercontent.com/78289372/505643774-65f2daca-2733-49e6-81ce-7e73b4c02646.PNG?jwt=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJnaXRodWIuY29tIiwiYXVkIjoicmF3LmdpdGh1YnVzZXJjb250ZW50LmNvbSIsImtleSI6ImtleTUiLCJleHAiOjE3NjE0MDc1MzMsIm5iZiI6MTc2MTQwNzIzMywicGF0aCI6Ii83ODI4OTM3Mi81MDU2NDM3NzQtNjVmMmRhY2EtMjczMy00OWU2LTgxY2UtN2U3M2I0YzAyNjQ2LlBORz9YLUFtei1BbGdvcml0aG09QVdTNC1ITUFDLVNIQTI1NiZYLUFtei1DcmVkZW50aWFsPUFLSUFWQ09EWUxTQTUzUFFLNFpBJTJGMjAyNTEwMjUlMkZ1cy1lYXN0LTElMkZzMyUyRmF3czRfcmVxdWVzdCZYLUFtei1EYXRlPTIwMjUxMDI1VDE1NDcxM1omWC1BbXotRXhwaXJlcz0zMDAmWC1BbXotU2lnbmF0dXJlPTZhOTM4MjVjODk2ZGYxZjQ1MjA3MzQ5MzhhNjQ0Njk5M2M5ZjQ3OWRhYTQ5NmFjZDljNzI2YjNhODNmZTk0MzgmWC1BbXotU2lnbmVkSGVhZGVycz1ob3N0In0.6cY87OBF0aoOZJJO-enRyneuAngxdBY07jrBuuCPsMg" width="500" /> |

| 서버와의 데이터 구조 불일치 | UI 개선 |
|-------|-------|
| <img src="https://private-user-images.githubusercontent.com/78289372/505643773-177364de-9bd6-443c-a5fe-40a546b318e0.PNG?jwt=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJnaXRodWIuY29tIiwiYXVkIjoicmF3LmdpdGh1YnVzZXJjb250ZW50LmNvbSIsImtleSI6ImtleTUiLCJleHAiOjE3NjE0MDc4MDcsIm5iZiI6MTc2MTQwNzUwNywicGF0aCI6Ii83ODI4OTM3Mi81MDU2NDM3NzMtMTc3MzY0ZGUtOWJkNi00NDNjLWE1ZmUtNDBhNTQ2YjMxOGUwLlBORz9YLUFtei1BbGdvcml0aG09QVdTNC1ITUFDLVNIQTI1NiZYLUFtei1DcmVkZW50aWFsPUFLSUFWQ09EWUxTQTUzUFFLNFpBJTJGMjAyNTEwMjUlMkZ1cy1lYXN0LTElMkZzMyUyRmF3czRfcmVxdWVzdCZYLUFtei1EYXRlPTIwMjUxMDI1VDE1NTE0N1omWC1BbXotRXhwaXJlcz0zMDAmWC1BbXotU2lnbmF0dXJlPTJjNWE1ZjcwMDIzMGI5OWNmNTcxYmI4NTk5NWU3NzRlNzdlMjI4NmUyOTljOGY1NzY4MDljNjk4NWE5NzFlODUmWC1BbXotU2lnbmVkSGVhZGVycz1ob3N0In0.uX7mY3yRI5EO-S62TvwP__3sKRi8gs2-ldbEp_yOo7c" width="500" /> | <img src="https://private-user-images.githubusercontent.com/78289372/505643776-99e91dcd-78e6-44c6-9477-125eebf1c370.PNG?jwt=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJnaXRodWIuY29tIiwiYXVkIjoicmF3LmdpdGh1YnVzZXJjb250ZW50LmNvbSIsImtleSI6ImtleTUiLCJleHAiOjE3NjE0MDc4MDcsIm5iZiI6MTc2MTQwNzUwNywicGF0aCI6Ii83ODI4OTM3Mi81MDU2NDM3NzYtOTllOTFkY2QtNzhlNi00NGM2LTk0NzctMTI1ZWViZjFjMzcwLlBORz9YLUFtei1BbGdvcml0aG09QVdTNC1ITUFDLVNIQTI1NiZYLUFtei1DcmVkZW50aWFsPUFLSUFWQ09EWUxTQTUzUFFLNFpBJTJGMjAyNTEwMjUlMkZ1cy1lYXN0LTElMkZzMyUyRmF3czRfcmVxdWVzdCZYLUFtei1EYXRlPTIwMjUxMDI1VDE1NTE0N1omWC1BbXotRXhwaXJlcz0zMDAmWC1BbXotU2lnbmF0dXJlPTVhYmJiZDhkNjVkNDY4YTI4OWE3YTI5ZWY0NjE0YTIzNmRmMjdlMDNmOTI4MjhkMTlmY2M0OGE4OTk4NzM0ZGQmWC1BbXotU2lnbmVkSGVhZGVycz1ob3N0In0.Dtoi4R_-RN2vi1YQZtecx7TWsY49Xejj_SKjyKsG-rI" width="500" /> |

<br>

## ✨ 팀원 

| 김가현 | 김세현 | 황유석 |
|-------|-------|-------|
| FE, 기획, 디자인, BE | BE | FE |

<br>

## 담당 업무 요약

![담당 업무 요약](https://private-user-images.githubusercontent.com/78289372/505643779-ae42723f-e86e-48d9-b15f-ee2cbaa71823.PNG?jwt=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJnaXRodWIuY29tIiwiYXVkIjoicmF3LmdpdGh1YnVzZXJjb250ZW50LmNvbSIsImtleSI6ImtleTUiLCJleHAiOjE3NjE0MDc4MDcsIm5iZiI6MTc2MTQwNzUwNywicGF0aCI6Ii83ODI4OTM3Mi81MDU2NDM3NzktYWU0MjcyM2YtZTg2ZS00OGQ5LWIxNWYtZWUyY2JhYTcxODIzLlBORz9YLUFtei1BbGdvcml0aG09QVdTNC1ITUFDLVNIQTI1NiZYLUFtei1DcmVkZW50aWFsPUFLSUFWQ09EWUxTQTUzUFFLNFpBJTJGMjAyNTEwMjUlMkZ1cy1lYXN0LTElMkZzMyUyRmF3czRfcmVxdWVzdCZYLUFtei1EYXRlPTIwMjUxMDI1VDE1NTE0N1omWC1BbXotRXhwaXJlcz0zMDAmWC1BbXotU2lnbmF0dXJlPTFiM2E2ZjQ5YjU0N2IwODQ1MTBmOTQ3OTg2ZGQ1OWQ0NjMxN2UwYTU5MjQwNGU0ZDFkOTQ0ODU5MjBiZDRlMWQmWC1BbXotU2lnbmVkSGVhZGVycz1ob3N0In0.3eXBW6BlaGUS-muDKjN98xwW1X69lrLtAKU3NoaFMec)

