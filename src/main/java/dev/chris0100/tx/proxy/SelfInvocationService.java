package dev.chris0100.tx.proxy;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class SelfInvocationService {

    /** Not transactional. Calls inner() through "this", so the proxy is bypassed. */
    public boolean outerCallsInnerDirectly() {
        return inner();
    }

    @Transactional
    public boolean inner() {
        return TransactionSynchronizationManager.isActualTransactionActive();
    }
}
