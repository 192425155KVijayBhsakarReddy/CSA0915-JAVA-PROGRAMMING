package com.smartbank.interfaces;

import com.smartbank.model.AuditLog;

import java.util.List;

/**
 * Interface defining audit and compliance logging contract.
 */
public interface Auditable {
    void log(String actor, String action, String details, AuditLog.Severity severity);

    List<AuditLog> getLogs();

    List<AuditLog> getLogsByActor(String actor);
}
