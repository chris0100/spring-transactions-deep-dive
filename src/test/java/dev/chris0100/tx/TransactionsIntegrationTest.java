package dev.chris0100.tx;

import dev.chris0100.tx.account.Account;
import dev.chris0100.tx.account.AccountRepository;
import dev.chris0100.tx.account.InsufficientFundsException;
import dev.chris0100.tx.account.TransferService;
import dev.chris0100.tx.proxy.CallerService;
import dev.chris0100.tx.proxy.SelfInvocationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TransactionsIntegrationTest {

    @Autowired AccountRepository accounts;
    @Autowired TransferService transfers;
    @Autowired SelfInvocationService selfInvocation;
    @Autowired CallerService caller;
    @Autowired TransactionTemplate tx;

    Long ana;
    Long luis;

    @BeforeEach
    void setUp() {
        accounts.deleteAll();
        ana = accounts.save(new Account("ana", new BigDecimal("100.00"))).getId();
        luis = accounts.save(new Account("luis", new BigDecimal("0.00"))).getId();
    }

    private BigDecimal balance(Long id) {
        return accounts.findById(id).orElseThrow().getBalance();
    }

    @Test
    void runtimeExceptionRollsBackTheWholeTransfer() {
        assertThrows(IllegalStateException.class, () -> transfers.transfer(ana, luis, new BigDecimal("500.00")));
        assertEquals(0, balance(ana).compareTo(new BigDecimal("100.00")));
        assertEquals(0, balance(luis).compareTo(BigDecimal.ZERO), "credit must be rolled back");
    }

    @Test
    void checkedExceptionCommitsByDefault() {
        assertThrows(InsufficientFundsException.class,
                () -> transfers.transferCheckedNoRollback(ana, luis, new BigDecimal("500.00")));
        assertEquals(0, balance(luis).compareTo(new BigDecimal("500.00")), "money appeared from nowhere: no rollback");
    }

    @Test
    void rollbackForFixesTheCheckedExceptionCase() {
        assertThrows(InsufficientFundsException.class,
                () -> transfers.transferCheckedWithRollback(ana, luis, new BigDecimal("500.00")));
        assertEquals(0, balance(luis).compareTo(BigDecimal.ZERO));
    }

    @Test
    void selfInvocationBypassesTheProxy() {
        assertFalse(selfInvocation.outerCallsInnerDirectly(), "this.inner() runs without a transaction");
        assertTrue(caller.callThroughProxy(), "calling through the proxy starts a transaction");
    }

    @Test
    void versionPreventsLostUpdates() {
        Account first = tx.execute(s -> accounts.findById(ana).orElseThrow());
        Account second = tx.execute(s -> accounts.findById(ana).orElseThrow());

        first.debit(new BigDecimal("10.00"));
        accounts.save(first); // version 0 -> 1

        second.debit(new BigDecimal("20.00"));
        assertThrows(ObjectOptimisticLockingFailureException.class, () -> accounts.save(second)); // stale version 0
        assertEquals(0, balance(ana).compareTo(new BigDecimal("90.00")));
    }
}
