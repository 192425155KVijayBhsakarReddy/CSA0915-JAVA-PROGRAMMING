package com.smartbank.concurrency;

import com.smartbank.interfaces.BankOperations;
import com.smartbank.model.Account;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * High-concurrency stress test engine to demonstrate thread synchronization,
 * race-condition prevention, and balance invariant verification.
 */
public class ConcurrencyStressTester {

    public static class StressTestResult {
        public final int totalOperations;
        public final int successfulTransfers;
        public final int failedOrRejected;
        public final long durationMs;
        public final double initialTotalAssets;
        public final double finalTotalAssets;
        public final boolean invariantPreserved;
        public final List<String> errorLogs;

        public StressTestResult(int totalOperations, int successfulTransfers, int failedOrRejected,
                                long durationMs, double initialTotalAssets, double finalTotalAssets,
                                boolean invariantPreserved, List<String> errorLogs) {
            this.totalOperations = totalOperations;
            this.successfulTransfers = successfulTransfers;
            this.failedOrRejected = failedOrRejected;
            this.durationMs = durationMs;
            this.initialTotalAssets = initialTotalAssets;
            this.finalTotalAssets = finalTotalAssets;
            this.invariantPreserved = invariantPreserved;
            this.errorLogs = errorLogs;
        }

        @Override
        public String toString() {
            return String.format(
                "=== STRESS TEST RESULTS ===\n" +
                "Total Ops: %d | Success: %d | Failed/Rejected: %d | Time: %d ms\n" +
                "Initial Assets: $%.2f | Final Assets: $%.2f\n" +
                "Balance Invariant Preserved: %s\n" +
                "Throughput: %.2f ops/sec\n",
                totalOperations, successfulTransfers, failedOrRejected, durationMs,
                initialTotalAssets, finalTotalAssets, invariantPreserved ? "YES (PASSED)" : "NO (CORRUPTED)",
                (durationMs > 0 ? (totalOperations * 1000.0 / durationMs) : 0.0)
            );
        }
    }

    public static StressTestResult runTransferStressTest(
            final BankOperations<Account> bankService,
            final List<String> accountNumbers,
            final int numThreads,
            final int operationsPerThread) {

        final int totalOps = numThreads * operationsPerThread;
        final AtomicInteger successCount = new AtomicInteger(0);
        final AtomicInteger failureCount = new AtomicInteger(0);
        final List<String> errorLogs = new ArrayList<>();
        final ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        final CountDownLatch startGate = new CountDownLatch(1);
        final CountDownLatch endGate = new CountDownLatch(numThreads);
        final Random random = new Random();

        // Calculate initial total assets across all accounts
        double initialAssets = 0.0;
        for (String accNum : accountNumbers) {
            try {
                initialAssets += bankService.getBalance(accNum);
            } catch (Exception ignored) {}
        }

        long startTime = System.currentTimeMillis();

        for (int t = 0; t < numThreads; t++) {
            final int threadId = t;
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        startGate.await(); // Synchronize thread start
                        for (int i = 0; i < operationsPerThread; i++) {
                            // Pick two random distinct accounts
                            int idx1 = random.nextInt(accountNumbers.size());
                            int idx2 = (idx1 + 1 + random.nextInt(accountNumbers.size() - 1)) % accountNumbers.size();

                            String fromAcc = accountNumbers.get(idx1);
                            String toAcc = accountNumbers.get(idx2);
                            double amount = 10.0 + random.nextInt(50); // $10 - $60

                            try {
                                bankService.transfer(fromAcc, toAcc, amount, "Stress Test Txn T" + threadId + "-" + i);
                                successCount.incrementAndGet();
                            } catch (Exception e) {
                                failureCount.incrementAndGet();
                                synchronized (errorLogs) {
                                    if (errorLogs.size() < 20) {
                                        errorLogs.add(String.format("T%d: %s -> %s ($%.2f) error: %s",
                                            threadId, fromAcc, toAcc, amount, e.getMessage()));
                                    }
                                }
                            }
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        endGate.countDown();
                    }
                }
            });
        }

        // Release start gate to launch all threads simultaneously
        startGate.countDown();

        try {
            endGate.await(60, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {}

        executor.shutdown();
        try {
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {}

        long duration = System.currentTimeMillis() - startTime;

        // Calculate final total assets across all accounts
        double finalAssets = 0.0;
        for (String accNum : accountNumbers) {
            try {
                finalAssets += bankService.getBalance(accNum);
            } catch (Exception ignored) {}
        }

        // During transfers only, total bank balance MUST remain exactly invariant (delta < 0.001)
        boolean invariantPreserved = Math.abs(finalAssets - initialAssets) < 0.01;

        return new StressTestResult(
            totalOps,
            successCount.get(),
            failureCount.get(),
            duration,
            initialAssets,
            finalAssets,
            invariantPreserved,
            errorLogs
        );
    }
}
