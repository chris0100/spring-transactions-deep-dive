# Spring Transactions Deep Dive

The `@Transactional` and JPA traps that show up in production and in interviews, each one proven by an integration test (Spring Boot 3 + H2).

| Topic | Where | What the test proves |
|-------|-------|----------------------|
| Rollback on runtime exceptions | `TransferService.transfer` | A failed transfer leaves both balances untouched |
| Checked exceptions do **not** roll back by default | `TransferService.transferCheckedNoRollback` vs `transferCheckedWithRollback` | Without `rollbackFor`, the debit is committed |
| Self-invocation skips the proxy | `SelfInvocationService` | Calling `this.inner()` runs without a transaction |
| Lost update / optimistic locking | `Account.@Version` | The second concurrent update fails with `ObjectOptimisticLockingFailureException` |
| N+1 queries | `CustomerRepository.findAllWithOrders` | `JOIN FETCH` loads everything in 1 query instead of 1 + N |

## Run

```bash
mvn test
```

## Interview notes

- `@Transactional` works through a **proxy**: it is ignored on `private` methods and on calls from the same class (`this.method()`).
- Default rollback: `RuntimeException` and `Error` only. For checked exceptions use `rollbackFor = Exception.class`.
- `REQUIRES_NEW` suspends the current transaction and opens a new one (useful for audit logs that must survive a rollback).
- `readOnly = true` lets Hibernate skip dirty checking and lets the driver route to a replica.
- Lost update: two transactions read the same row and both write. Fix with `@Version` (optimistic, return 409 to the client), an atomic `UPDATE ... SET balance = balance - ?`, or `SELECT ... FOR UPDATE` in a short transaction.
- N+1: don't switch to `EAGER` globally. Use `JOIN FETCH` or `@EntityGraph` for the specific use case, and paginate ids first if you fetch collections.
- Keep transactions short: no HTTP calls or message publishing inside them (use the outbox pattern or `@TransactionalEventListener(AFTER_COMMIT)`).
