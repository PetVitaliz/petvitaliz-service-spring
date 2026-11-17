package com.petvitaliz.service;

import com.petvitaliz.exception.ApiException;
import org.springframework.dao.DataIntegrityViolationException;

final class PersistenceGuard {

    private PersistenceGuard() {
    }

    static void deleteOrConflict(Runnable deleteAndFlush, String conflictMessage) {
        try {
            deleteAndFlush.run();
        } catch (DataIntegrityViolationException e) {
            throw ApiException.conflict(conflictMessage);
        }
    }

    static void requirePositiveId(Integer id, ApiException error) {
        if (id == null || id <= 0) {
            throw error;
        }
    }
}
