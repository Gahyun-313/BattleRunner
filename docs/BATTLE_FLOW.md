# ⚔️ Battle Flow

## 🔄 전체 흐름

```text
Battle 시작
  ↓
상대 사용자 Matching
  ↓
공통 시작 좌표 생성·조회
  ↓
동일한 Row·Column Grid 생성
  ↓
GPS 위치 추적
  ↓
현재 위치가 포함된 Polygon 탐색
  ↓
Grid 소유권 갱신·서버 전송
  ↓
지도 색상 변경
  ↓
점령 수를 기반으로 결과 계산
```

## 📍 공통 좌표계

각 기기의 현재 위치를 기준으로 Grid를 만들면 서로 다른 좌표계가 생성됩니다. 첫 사용자의 시작 좌표를 서버에 저장하고 두 번째 사용자가 같은 좌표를 받아 Grid를 생성하도록 했습니다.

두 사용자는 같은 Row·Column과 Grid 크기를 사용하므로 동일한 `gridId`가 같은 지도 영역을 의미합니다.

## 🗺️ Grid 생성

기준 좌표 주변을 고정 크기의 사각형 Polygon으로 나누고 다음 식으로 고유 ID를 만듭니다.

```text
gridId = row × columnCount + column
```

ID를 Google Maps Polygon의 Tag에 저장해 위치 판정 결과를 소유권 데이터와 연결합니다.

## 🚩 영역 점령

LocationCallback에서 받은 현재 좌표가 어떤 Polygon 내부에 있는지 판별합니다. 이전 소유자와 다른 Grid에 진입한 경우에만 다음 처리를 수행합니다.

1. Local `ownershipMap`의 소유자 변경
2. Polygon Fill Color 변경
3. Backend에 Battle ID, Grid ID와 사용자 ID 전송

동일 Grid 안에서 발생하는 반복 위치 Update는 소유권 변경 요청을 다시 보내지 않습니다.

## 🏆 결과

러닝 종료 시 이동 거리, 시간, 사용자별 점령 Grid 수를 이용해 결과를 표시합니다. Grid 점령 수를 통해 경쟁 결과를 직관적으로 비교할 수 있게 했습니다.

## ⚠️ 현재 한계

- WebSocket 기반 상대 위치·점령 정보 Push를 적용하지 않았습니다.
- 여러 기기의 REST 갱신 시점 차이로 화면 반영에 지연이 생길 수 있습니다.
- GPS 오차가 Grid 경계에서 잦은 진입·이탈을 만들 수 있습니다.
- 서버 권위 기반의 충돌 해결과 Replay 기능은 구현하지 않았습니다.
