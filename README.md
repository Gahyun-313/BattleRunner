# 🏃‍♀️ BattleRunner (배틀러너)

> **“달리며 영토를 점령하는 러닝 배틀 앱”**  
> 실제 달리기 데이터를 기반으로 맵을 점령하고, 다른 러너와 경쟁하세요.

![메인배너](./assets/Frame_1.png)

---

## 🎨 프로젝트 미리보기

| 메인 화면 | 배틀 지도 | 결과 화면 |
|:--:|:--:|:--:|
| ![메인](./assets/main_screen.png) | ![맵](./assets/map_grid.png) | ![결과](./assets/result_screen.png) |

---

## 🧭 소개

**BattleRunner**는 단순한 러닝 앱이 아닙니다.  
실제 GPS 데이터를 기반으로 **맵의 영토를 점령하고**,  
다른 사용자와 **실시간으로 점령 경쟁**을 벌이는 **게임형 러닝 앱**입니다.

> **“전투가 아닌 배틀, 경쟁이 아닌 운동의 즐거움.”**

---

## ✨ 핵심 기능

| 기능 | 설명 | 예시 이미지 |
|------|------|--------------|
| 🗺️ **그리드 점령 시스템** | 250m x 250m 구역 단위로 영역 점령 | ![Grid](./assets/grid_overlay.png) |
| ⚔️ **실시간 배틀 매칭** | 두 사용자가 같은 지역에서 달리며 점령 경쟁 | ![Battle](./assets/battle_start.png) |
| 🔑 **로그인 / 자동로그인** | 자체, Google, Kakao 로그인 지원 | ![Login](./assets/login_screen.png) |
| 💾 **기록 저장** | 러닝/배틀 기록을 SQLite 및 서버(MySQL)에 저장 | ![History](./assets/record_list.png) |
| 📊 **결과 리포트** | 점령률, 거리, 시간 기반 결과 리포트 표시 | ![Report](./assets/battle_report.png) |

---

## 🧩 시스템 구조

| Android App | Spring Boot Server |
|--------------|-------------------|
| Kotlin (MVVM), Compose, Retrofit, Google Maps | Spring Boot, JPA, MySQL, Swagger |
| SQLite 로컬 저장 | REST API 기반 데이터 통신 |
| 로그인/배틀/맵 관리 UI | Controller-Service-Entity 구조 |

## ⚙️ 기술 스택

| 분야 | 기술 |
|------|------|
| **Frontend (Android)** | Kotlin, Jetpack Compose, MVVM, Retrofit2, Google Maps API, SQLite |
| **Backend (Server)** | Spring Boot, JPA, MySQL, Lombok, Gradle |
| **Infra / Tools** | IntelliJ IDEA, Android Studio, Docker(MySQL), Swagger |

---

## 📱 앱 UI 플로우

![App Flow](./assets/app_flow_diagram.png)


