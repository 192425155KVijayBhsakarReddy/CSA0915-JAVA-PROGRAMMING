package com.smartbank.interfaces;

import com.smartbank.exceptions.BankException;

import java.io.File;
import java.util.Collection;

/**
 * Generic interface for persistence engines (File Streams / Serialization / Database).
 */
public interface PersistenceEngine<T> {
    void save(T entity) throws BankException;

    void saveAll(Collection<T> entities) throws BankException;

    void exportToFile(File targetFile) throws BankException;

    void importFromFile(File sourceFile) throws BankException;
}
