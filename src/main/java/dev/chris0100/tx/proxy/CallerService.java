package dev.chris0100.tx.proxy;

import org.springframework.stereotype.Service;

/** Calls the other bean through its Spring proxy, so @Transactional is applied. */
@Service
public class CallerService {

    private final SelfInvocationService target;

    public CallerService(SelfInvocationService target) {
        this.target = target;
    }

    public boolean callThroughProxy() {
        return target.inner();
    }
}
