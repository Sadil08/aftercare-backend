package com.aftercare.aftercare_portal.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final long BLOCK_DURATION_MS = 10 * 60 * 1000L;

    private final ConcurrentHashMap<String, AttemptRecord> attempts = new ConcurrentHashMap<>();

    public void recordFailure(String username) {
        attempts.merge(username, new AttemptRecord(1, System.currentTimeMillis()),
                (existing, inc) -> new AttemptRecord(existing.count() + 1, System.currentTimeMillis()));
    }

    public void recordSuccess(String username) {
        attempts.remove(username);
    }

    public boolean isBlocked(String username) {
        AttemptRecord record = attempts.get(username);
        if (record == null || record.count() < MAX_ATTEMPTS) return false;
        if ((System.currentTimeMillis() - record.lastAttemptMs()) >= BLOCK_DURATION_MS) {
            attempts.remove(username);
            return false;
        }
        return true;
    }

    private record AttemptRecord(int count, long lastAttemptMs) {}
}
