package org.wangyang.util;

import org.springframework.aop.framework.AopContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TransactionWrapper {
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void runInNewTransactionThenThrow(Runnable task) {
        task.run();
        throw new RuntimeException();
    }

    public Runnable rollbackInNewTransaction(Runnable task) {
        TransactionWrapper proxy = (TransactionWrapper) AopContext.currentProxy();
        return () -> {
            try {
                proxy.runInNewTransactionThenThrow(task);
            } catch (RuntimeException ignored) {}
        };
    }
}
