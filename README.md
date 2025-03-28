# Point project 동시성제어 보고서

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
│       │   ├── lock/                      # 동시성 제어
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

## ✅ 동시성 처리
목표: 같은 사용자의 포인트가 동시에 충전/사용되는 경우를 방지

### 동시성에 대한 고민
동시성 제어란 여러 작업(스레드, 사용자 등)이 동시에 동일한 자원(데이터, 메모리 등)에 접근할 때 충돌 없이 안전하게 처리되도록 조율하는 개념이라는 정도만 알고 있었고, 

기업과제에서 간단히 적용해본 경험만 있었다. 

다만, 실무에서는 동시성 제어를 깊이 고민하며 코드 작성까지 해본 경험은 아직 없었다.

(회사 프로젝트 특성상 트랜잭션이 많지 않아 동시성 이슈를 경험해본적 없다고 생각했는데, 

코치님께서 동시성문제가 발생한것은 개발자가 확인이 불가할 수 있기 때문에 나도 모르는새 발생했을수도 있다고 하셨다😅)

그래서 이번주차 주제를 학습하며 동시성에 대해 알아보았다.

### ✅ Java의 주요 동시성 제어 방식

| 방식 | 설명 |
|------|------|
| `synchronized` | 메서드 또는 블록에 대해 객체/클래스 단위로 락을 걸어 직렬화 |
| `ReentrantLock` | 명시적으로 락을 얻고 해제할 수 있는 고급 락 |
| `ConcurrentHashMap` | 맵에 대한 동시 읽기/쓰기 안전하게 처리 (세그먼트 락 구조) |
| `Atomic 클래스` | `AtomicLong`, `AtomicInteger` 등 CAS 기반 non-blocking 연산 |
| `ReadWriteLock`, `StampedLock` | 읽기 병렬, 쓰기 단독을 보장하는 락 구조 |
| `Collections.synchronizedXXX` | 기존 컬렉션을 동기화 래퍼로 감싸는 방식 |

---

### 🆚 상세 비교 및 장단점

#### 🔹 `synchronized`

- **장점**: 문법 간단, JVM이 관리해주므로 안정성 높음
- **단점**: 블로킹 기반, 경합 심할 경우 성능 저하

#### 🔹 `ReentrantLock`

- **장점**: `tryLock()`, `lockInterruptibly()` 등 세밀한 락 제어 가능
- **단점**: 명시적으로 `unlock()` 해야 해서 실수 위험 있음

#### 🔹 `ConcurrentHashMap`

- **장점**: 동시 읽기/쓰기 가능, 내부적으로 분할 락 구조
- **단점**: 복합 연산 (예: put+get+remove) 에는 주의 필요

#### 🔹 `AtomicInteger`, `AtomicLong` 등

- **장점**: 비블로킹(CAS), 단일 연산에 매우 빠름
- **단점**: 복잡한 조건 분기나 트랜잭션성 작업에는 부적합

#### 🔹 `ReadWriteLock`, `StampedLock`

- **장점**: 읽기 다수 / 쓰기 소수일 때 성능 우수
- **단점**: 구현 복잡, 교착 상태 주의 필요

---

처음에는 ConcurrentHashMap + synchronized 방식을 적용했다가, userId별로 락 객체를 생성하는데 락 객체는 메모리에 유지되기 때문에

사용량이 많아지면 캐시나 제거를 고려해야된다고 하여
WeakHashMap + synchronizedMap 조합으로 변경했다.

WeakHashMap 자체는 스레드 안전하지 않기 때문에 Collections.synchronizedMap(...) 래핑을 해주었다.

- Long userId 키가 다른 곳에서 참조되지 않으면, GC 대상이 됨
- WeakHashMap은 키가 GC 되면 자동으로 엔트리 제거
- Collections.synchronizedMap()으로 동기화 처리

### 동시성 테스트
Java 동시성 테스트를 할 때, ExecutorService와 CompletableFuture라는 병렬처리 테스트도구를 사용했다.
TDD를 공부하며 처음 들어본 테스트라 검색해보며 적용해보았다.

(GPT)
### ✅ 1. ExecutorService 기반 동시성 테스트
💡 개념
- 스레드 풀을 만들어 여러 작업을 병렬로 실행
- 동시성 상황을 시뮬레이션할 때 자주 사용
- CountDownLatch와 같이 사용해 정확한 타이밍 조절

### ✅ 2. CompletableFuture 기반 비동기 테스트
💡 개념
- 자바 8부터 도입된 비동기 프로그래밍 API 
- 작업이 끝난 후에 후속 동작(chaining) 을 설정할 수 있음 
- 테스트에서도 비동기 흐름 검증이나 병렬 처리 확인 가능

### ✅ 언제 어떤 걸 써야 할까?

| 상황 | 추천 방식 | 이유 |
|------|------------|------|
| 정확한 동시성 시뮬레이션 (락 테스트 등) | ✅ `ExecutorService` + `CountDownLatch` | 스레드 타이밍을 제어하며 레이스 컨디션 검증 가능 |
| 단순 병렬 실행 + 비동기 흐름 테스트 | ✅ `CompletableFuture` | 선언적으로 병렬 흐름 구성, 테스트 작성 간결 |
| 예외나 실패 흐름 검증 | ✅ `CompletableFuture` | `.exceptionally`, `.handle` 등 후속 처리를 통한 예외 제어 가능 |
| 수백~수천 명 동시 요청 시뮬레이션 | ✅ `ExecutorService` | 스레드 풀(`Fixed`, `Cached`)을 통해 효율적 대량 병렬 처리 가능 |

---

### ✨ `ExecutorService` vs `CompletableFuture`

| 항목 | `ExecutorService` | `CompletableFuture` |
|------|-------------------|----------------------|
| 스타일 | 명령형 | 선언형 |
| 컨트롤 | 정교한 제어 가능 (스레드 수, 대기, 락 등) | 간결한 체이닝 기반 |
| 테스트 적합도 | 동시성 충돌/락 동작 시뮬레이션 | 비동기 흐름 및 실패 처리 테스트 |
| 학습 곡선 | 낮음 (직관적) | 중간 (체이닝, 예외 흐름 이해 필요) |

---


