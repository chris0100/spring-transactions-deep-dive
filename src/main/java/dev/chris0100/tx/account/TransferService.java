package dev.chris0100.tx.account;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class TransferService {

    private final AccountRepository accounts;

    public TransferService(AccountRepository accounts) {
        this.accounts = accounts;
    }

    /** RuntimeException -> rollback (default). */
    @Transactional
    public void transfer(Long fromId, Long toId, BigDecimal amount) {
        Account from = accounts.findById(fromId).orElseThrow();
        Account to = accounts.findById(toId).orElseThrow();
        to.credit(amount);
        from.debit(amount); // throws IllegalStateException if there is not enough money
    }

    /** Checked exception WITHOUT rollbackFor: the credit already made is committed. Bug on purpose. */
    @Transactional
    public void transferCheckedNoRollback(Long fromId, Long toId, BigDecimal amount) throws InsufficientFundsException {
        doCheckedTransfer(fromId, toId, amount);
    }

    /** Same logic, but rollbackFor makes the checked exception roll back the transaction. */
    @Transactional(rollbackFor = InsufficientFundsException.class)
    public void transferCheckedWithRollback(Long fromId, Long toId, BigDecimal amount) throws InsufficientFundsException {
        doCheckedTransfer(fromId, toId, amount);
    }

    private void doCheckedTransfer(Long fromId, Long toId, BigDecimal amount) throws InsufficientFundsException {
        Account from = accounts.findById(fromId).orElseThrow();
        Account to = accounts.findById(toId).orElseThrow();
        to.credit(amount);
        if (from.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("account " + fromId + " has " + from.getBalance());
        }
        from.debit(amount);
    }
}
