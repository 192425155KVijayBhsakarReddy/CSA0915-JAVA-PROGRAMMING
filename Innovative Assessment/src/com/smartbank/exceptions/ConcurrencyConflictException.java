package com.smartbank.exceptions;

public class ConcurrencyConflictException extends BankException {
    private final String resourceId;
    private final String threadName;

    public ConcurrencyConflictException(String resourceId, String threadName) {
        super("CONCURRENCY_LOCK_TIMEOUT", String.format(
            "Thread '%s' could not acquire exclusive transaction lock on resource '%s' within timeout window.",
            threadName, resourceId
        ));
        this.resourceId = resourceId;
        this.threadName = threadName;
    }

    public String getResourceId() {
        return resourceId;
    }

    public String getThreadName() {
        return threadName;
    }
}
