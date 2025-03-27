# hhplus-tdd

## ✅ 요구사항 정의

### 🔗 API 명세

| 메서드 | URL | 설명 |
| --- | --- | --- |
| PATCH | `/point/{id}/charge` | 사용자 포인트 충전 |
| PATCH | `/point/{id}/use` | 사용자 포인트 사용 |
| GET | `/point/{id}` | 사용자 포인트 조회 |
| GET | `/point/{id}/histories` | 사용자 포인트 거래 내역 조회 |

---

### ⚙️ 기능 요구사항

- 포인트를 충전하거나 사용할 수 있어야한다.
- 잔액이 부족할 경우, 포인트 사용은 실패해야한다.
- 사용자별 포인트 거래 내역이 저장되고 조회되어야한다.
- 충전/사용 금액은 0보다 커야 하며, 유효성 검증이 필요하다.

---

## ✅ 디렉토리 구조

```

src/
├── main/
│   └── java/io/hhplus/tdd/
│       ├── database/                      # 수정 불가: 외부 저장소 시뮬레이터
│       ├── point/
│       │   ├── controller/                # REST API 컨트롤러
│       │   ├── exception/                 # 에러 코드/메시지/응답
│       │   ├── model/                     # 도메인 모델 (UserPoint, PointHistory)
│       │   ├── repository/                # 저장소 인터페이스 및 구현체
│       │   └── service/                   # 비즈니스 로직 처리
│       └── TddApplication.java            # 스프링 부트 실행 진입점
└── test/
    └── java/io/hhplus/tdd/
        └── point/
            ├── model/                     # 도메인 단위 테스트
            └── service/                   # 서비스 테스트

```

---

## ✅ 예외 처리

| 메시지 | 상황 |
| --- | --- |
| `0보다 큰 값을 입력해주세요.` | 충전/사용 금액이 0 이하인 경우 |
| `잔고가 부족합니다.` | 사용하려는 포인트가 잔고보다 큰 경우 |
|  |  |

---

## ✅ 테스트 코드

### 🔍 단위 테스트 (`/test/java/io/hhplus/tdd/point`)

### ✔ 서비스 테스트 (`PointServiceTest.java`)

- 포인트 충전/사용 성공 케이스
- 잔고 부족, 잘못된 금액 등 예외 케이스
- `Mockito`를 활용한 Mock 기반 테스트

### ✔ 모델 테스트

- `UserPointTest`: 포인트 계산, 잔고 부족 예외
- `PointHistoryTest`: 거래 내역 생성 확인
