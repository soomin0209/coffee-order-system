# ☕ Coffee Order System
CH 6 실전 - K사 서버 개발 과제 

---

## 설계 내용
### ERD
<img width="1040" height="979" alt="Coffee-Mock" src="https://github.com/user-attachments/assets/bf22a0b8-7266-42fd-8204-01fae8ced8f7" />

### API 명세서
| 기능명 | API Path | http method |
| --- | --- | --- |
| 커피 메뉴 목록 조회 | `/api/menus` | `GET` |
| 포인트 충전 | `/api/points` | `POST` |
| 커피 주문 생성 | `/api/orders` | `POST` |
| 주문 결제 | `/api/payments/{orderId}` | `POST` |
| 인기 메뉴 목록 조회 | `/api/menus/ranking` | `GET` |

---

## 설계의 의도
### 포인트 테이블 분리
포인트 잔액은 users의 point_balance로 관리하고, 모든 충전/차감 이력은 points 테이블에 기록한다. 
정합성을 위해 포인트 충전/차감 시에는 SUM으로 계산 후 검증한다.

### 주문 메뉴 정보 스냅샷
order_items의 menu_name, price에 주문 시점의 메뉴 이름과 가격을 저장해, 이후에 가격이 변경되어도 결제 금액을 유지한다.

### 포인트와 주문의 연관관계
points의 order_id는 nullable로 설계하였다.

- `CHARGE` : order_id = null
- `USE` : order_id 연결

### 인기 메뉴 집계
별도 집계 테이블 없이 Redis ZSET을 활용한다. 
주문 완료 시 해당 menuId의 score를 ZINCRBY로 증가시키고, 조회 시 ZREVRANGE로 상위 3개를 조회한다.

---

## 선택한 문제해결 전략 및 이 선택을 위해 분석한 내용
### 1. 동시성 문제 - 비관적락
#### 분석
다수의 서버 인스턴스가 동시에 동일한 사용자의 포인트를 처리할 경우 아래와 같은 상황이 발생할 수 있다.

```
인스턴스 A : 잔액 SUM 조회 → 10,000P 확인
인스턴스 B : 잔액 SUM 조회 → 10,000P 확인
인스턴스 A : 5,000P 차감 트랜잭션 insert
인스턴스 B : 5,000P 차감 트랜잭션 insert ← 실제 잔액은 0P여야 하는데 중복 차감 발생
```

포인트는 실제 결제 수단으로 사용되므로 데이터 정합성이 매우 중요하다.
따라서 충돌 발생 후 재시도를 전제로 하는 낙관적락은 적절하지 않다고 판단하였다.
또한 현재 시스템은 단일 데이터베이스를 사용하는 구조이기 때문에, 분산 락까지 도입하는 것은 과도한 설계라고 보았다.

| **낙관적 락** | **분산 락** |
| --- | --- |
| 다중 서버 환경에서 충돌 빈도 증가로 재시도 폭발 | 네트워크 비용 및 락 관리 복잡도 증가, 장애 시 복구 처리 필요 |

#### 선택
- 비관적락

사용자 포인트 조회 시 `SELECT ... FOR UPDATE`를 사용하여 동일 사용자에 대한 요청을 직렬화하였다.
이를 통해 다중 인스턴스 환경에서도 데이터 정합성을 보장할 수 있다.

```
포인트 충전 요청
  → 비관적 락 획득 (user_id 기준)
  → points SUM 계산
  → points insert (충전)
  → users.point_balance 업데이트
  → 트랜잭션 커밋

커피 주문 요청
  → 비관적 락 획득 (user_id 기준)
  → poins SUM으로 잔액 검증
  → points insert (차감)
  → users.point_balance 업데이트
  → 주문 생성
  → 트랜잭션 커밋
```

### 2. 주문 내역 실시간 전송 - Kafka
#### 분석
주문 완료 후 데이터 수집 플랫폼으로 주문 정보를 전송해야 한다. 
이를 동기로 처리할 경우 아래 문제가 발생한다.

```
1. 플랫폼 전송 실패 시 주문 자체가 실패하는 사이드 이펙트 발생
2. 플랫폼 응답 지연이 주문 응답 속도에 직접 영향
3. 다수 서버 인스턴스에서 동시에 전송 시 순서 보장 불가
```

#### 선택
- Kafka 비동기 이벤트

주문 완료 후 Kafka Topic에 이벤트를 produce하고, Consumer가 데이터 수집 플랫폼으로 전달한다.
주문 처리와 데이터 전송이 느슨하게 결합되어 전송 실패가 주문 결과에 영향을 주지 않는다.
다수 인스턴스에서 동시에 produce해도 Kafka가 메시지 순서와 유실을 보장한다.

```
주문 완료
  → Kafka Topic produce (payment-completed)
      { userId, menuId, amount, paidAt }
  → Consumer → 데이터 수집 플랫폼 전달 (Mock)
```

### 3. 인기 메뉴 조회 - Kafka + Redis ZSET
#### 분석
인기 메뉴를 조회할 때마다 payments 테이블 전체 집계하는 방식은 주문이 쌓수록 쿼리 성능이 저하된다.
또 다중 서버 환경에서 동시에 집계 쿼리가 실행되면 DB 부하가 가중된다.

#### 선택
- Kafka + Redis ZSET

주문 완료 시 Kafka 이벤트를 발행하고, Consumer가 이를 처리하여 Redis ZSET에 반영한다.

```
주문 완료 → Kafka Topic produce (payment-completed)

Consumer → ZINCRBY "menu:ranking:{date}" 1 {menuId}

인기 메뉴 조회 → ZREVRANGE "menu:ranking:last7days" 0 2
```

이를 통해 주문 처리와 집계 처리를 분리하고, DB 부하 없이 빠른 조회가 가능하도록 설계하였다. 
또한, Kafka를 통해 이벤트 기반으로 집계를 수행함으로써 다중 인스턴스 환경에서도 일관된 데이터 처리가 가능하도록 하였다.

---

## 기술적 선택 이유

| 비관적락 | Redis ZSET | Kafka |
| --- | --- | --- |
| 결제 데이터의 강한 정합성을 보장하고, 다중 인스턴스 환경에서도 안전한 동시성 제어 가능 | 매번 집계 쿼리를 실행하지 않고, score를 증가시켜 빠르게 조회 가능 | 주문 완료 후 데이터 수집 플랫폼 전송을 주문 처리와 분리하여, 전송 실패가 주문 결과에 영향을 주지 않도록 비동기 처리 가능 |

