# 🔧 Troubleshooting

## 📍 GPS 경로가 흔들리는 문제

### 문제

실제 이동 경로와 다르게 Polyline이 흔들리고 작은 위치 변화가 모두 경로에 반영됐습니다.

### 원인과 해결

GPS는 건물과 실내 환경의 영향을 받으며 Update 주기가 너무 짧으면 작은 오차까지 표시됩니다. FusedLocationProviderClient를 적용하고 위치 Update 주기를 조정해 여러 위치 Source를 활용하면서 불필요한 변화를 줄였습니다.

## 🔄 화면 회전 시 러닝 정보 초기화

### 문제

화면 회전 후 Timer, 거리와 버튼 상태가 초기화됐습니다.

### 원인과 해결

운동 상태를 Fragment 내부에서 관리해 Activity 재생성과 함께 사라졌습니다. Timer, 경로, 거리와 러닝 여부를 ViewModel로 이동하고 LiveData로 UI를 갱신해 재생성 후에도 상태를 유지했습니다.

## 🗺️ Google Maps Camera 과도한 이동

### 문제

모든 위치 Update마다 Camera가 이동해 사용자가 지도를 직접 확인하기 어렵고 화면이 흔들렸습니다.

### 해결

이전 Camera 위치와 현재 위치의 거리를 비교해 일정 거리 이상 이동한 경우에만 Camera를 갱신했습니다. 위치 데이터의 즉시성보다 지도 사용성을 우선한 결정입니다.

## 🎯 현재 위치 버튼이 동작하지 않는 문제

위치 권한 요청, GoogleMap 초기화와 LocationCallback 순서가 맞지 않았고 Overlay가 기본 버튼 입력을 가로채는 경우가 있었습니다. Map 초기화 후에만 버튼을 활성화하고 Custom 현재 위치 버튼과 Camera 이동 로직으로 교체했습니다.

## ♻️ 러닝 결과 Popup의 Lifecycle 충돌

러닝 종료와 Fragment Transaction이 동시에 진행되면서 Popup이 나타나지 않거나 화면이 겹쳤습니다. 일반 Popup과 혼합 Context 사용을 제거하고 DialogFragment를 FragmentManager 생명주기에 맞춰 표시했습니다.

## 🔗 Android·Backend DTO 타입 불일치

Backend의 숫자 필드를 Android에서 String으로 선언해 JSON Parsing 오류가 발생했습니다. API 응답 명세와 Server DTO를 대조해 타입과 Nullable 여부를 동일하게 수정했습니다.

## 🔐 자동 로그인 미유지

로그인 상태를 메모리에만 저장해 앱 종료 후 사라졌습니다. 로그인 성공 결과를 SQLite에 저장하고 Splash에서 조회해 로그인 화면과 Main을 분기했습니다.
