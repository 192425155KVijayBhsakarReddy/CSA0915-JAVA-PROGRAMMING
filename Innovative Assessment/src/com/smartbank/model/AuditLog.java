package com.smartbank.model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * System and security audit log entry.
 */
public class AuditLog implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Severity { INFO, WARNING, CRITICAL, SECURITY }

    private final String logId;
    private final Date timestamp;
    private final String actor;
    private final String action;
    private final String details;
    private final Severity severity;

    public AuditLog(String actor, String action, String details, Severity severity) {
        this.logId = "LOG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.timestamp = new Date();
        this.actor = actor != null ? actor : "SYSTEM";
        this.action = action;
        this.details = details;
        this.severity = severity != null ? severity : Severity.INFO;
    }

    public String getLogId() {
        return logId;
    }

    public Date getTimestamp() {
        return new Date(timestamp.getTime());
    }

    public String getActor() {
        return actor;
    }

    public String getAction() {
        return action;
    }

    public String getDetails() {
        return details;
    }

    public Severity getSeverity() {
        return severity;
    }

    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        return String.format("[%s] [%s] [%s] Actor: %s | Action: %s | %s",
            sdf.format(timestamp), severity, logId, actor, action, details);
    }
}
