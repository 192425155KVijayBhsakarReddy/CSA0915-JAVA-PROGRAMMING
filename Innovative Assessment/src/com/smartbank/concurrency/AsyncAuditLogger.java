package com.smartbank.concurrency;

import com.smartbank.model.AuditLog;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Demonstrates Thread Communication using wait() and notifyAll().
 * Background Consumer thread asynchronously logs events to memory and disk.
 */
public class AsyncAuditLogger implements Runnable {
    private static final int MAX_BUFFER_SIZE = 500;

    private final Queue<AuditLog> logQueue;
    private final List<AuditLog> persistedLogs;
    private final Object lock = new Object();
    private volatile boolean running = true;
    private Thread workerThread;
    private File logFile;

    public AsyncAuditLogger() {
        this.logQueue = new LinkedList<>();
        this.persistedLogs = new ArrayList<>();
        File dataDir = new File("data");
        if (!dataDir.exists()) dataDir.mkdirs();
        this.logFile = new File(dataDir, "audit_trail.log");
    }

    public synchronized void start() {
        if (workerThread == null || !workerThread.isAlive()) {
            running = true;
            workerThread = new Thread(this, "AsyncAuditLogger-Worker");
            workerThread.setDaemon(true);
            workerThread.setPriority(Thread.MIN_PRIORITY); // Background priority
            workerThread.start();
        }
    }

    public void stop() {
        running = false;
        synchronized (lock) {
            lock.notifyAll(); // Wake up worker to flush and terminate
        }
        if (workerThread != null) {
            try {
                workerThread.join(2000);
            } catch (InterruptedException ignored) {}
        }
    }

    /**
     * Producer method: Pushes log event into queue and notifies waiting consumer.
     */
    public void enqueueLog(AuditLog log) {
        if (log == null) return;
        synchronized (lock) {
            while (logQueue.size() >= MAX_BUFFER_SIZE) {
                try {
                    // Wait if buffer is saturated
                    lock.wait(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
            logQueue.offer(log);
            lock.notifyAll(); // Wake up consumer thread
        }
    }

    /**
     * Consumer thread loop: Waits for logs and drains to storage.
     */
    @Override
    public void run() {
        while (running || !logQueue.isEmpty()) {
            List<AuditLog> batch = new ArrayList<>();
            synchronized (lock) {
                while (logQueue.isEmpty() && running) {
                    try {
                        // Wait until producer enqueues logs
                        lock.wait();
                    } catch (InterruptedException e) {
                        if (!running) break;
                    }
                }
                while (!logQueue.isEmpty()) {
                    batch.add(logQueue.poll());
                }
                lock.notifyAll(); // Notify producers that buffer has space
            }

            if (!batch.isEmpty()) {
                writeBatch(batch);
            }
        }
    }

    private void writeBatch(List<AuditLog> batch) {
        synchronized (persistedLogs) {
            persistedLogs.addAll(batch);
        }
        if (logFile != null) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true))) {
                for (AuditLog log : batch) {
                    writer.write(log.toString());
                    writer.newLine();
                }
                writer.flush();
            } catch (IOException ignored) {}
        }
    }

    public List<AuditLog> getAllLogs() {
        synchronized (persistedLogs) {
            return new ArrayList<>(persistedLogs);
        }
    }
}
