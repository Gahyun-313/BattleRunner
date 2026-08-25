# 📍 Location Flow

## 🏃 개인 러닝 흐름

```text
러닝 시작
  ↓
위치 권한·GPS 확인
  ↓
FusedLocationProviderClient
  ↓ LocationCallback
현재 좌표 수신
  ├─ 이전 좌표와 거리 계산
  ├─ ViewModel 누적 거리·경로 갱신
  ├─ 기존 Polyline에 좌표 추가
  └─ 조건 충족 시 Camera 이동
  ↓
LiveData로 시간·거리·경로 표시
```

## 📡 위치 데이터 처리

새 좌표가 들어오면 Android Location API로 이전 좌표와의 거리를 계산하고 총 거리에 누적합니다. 경로 좌표 목록은 ViewModel에 보관해 Fragment가 재생성되어도 유지합니다.

## 🗺️ 지도 렌더링

초기에는 매 위치 변경마다 Polyline을 새로 생성했습니다. 러닝 시간이 길어질수록 객체 생성이 반복되는 구조를 개선해 기존 Polyline에 새 좌표만 추가했습니다.

Camera도 모든 위치 변화에 따라 이동시키지 않고 사용자가 충분히 이동했을 때만 갱신해 GPS의 작은 흔들림이 화면 흔들림으로 이어지는 것을 줄였습니다.

## 🔄 Background Tracking

```text
러닝 시작
  ↓
Foreground Service 시작
  ↓ 지속 Notification 표시
화면 OFF / 앱 Background
  ↓
Location Update 유지
  ↓
러닝 종료 시 Service와 Update 종료
```

Android의 Background 실행 제한을 고려해 Foreground Service로 사용자가 인지할 수 있는 Notification을 표시하면서 위치 추적을 유지했습니다.

## 🎯 GPS 오차 대응

GPS는 실내, 고층 건물과 날씨에 따라 오차가 생길 수 있습니다. FusedLocationProviderClient로 GPS·Wi-Fi·기지국 정보를 종합하고 위치 Update 주기를 조정해 작은 오차가 모두 경로에 기록되는 현상을 완화했습니다.

## ♻️ 생명주기

위치 상태와 Timer는 Fragment가 아니라 ViewModel에서 관리합니다. 화면이 다시 만들어지면 기존 LiveData를 다시 Observe해 현재 러닝 상태를 복원합니다. 실제 Background Tracking의 생명주기는 Foreground Service가 담당합니다.
