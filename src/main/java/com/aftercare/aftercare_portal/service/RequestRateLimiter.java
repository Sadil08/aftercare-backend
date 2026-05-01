package com.aftercare.aftercare_portal.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

/**
 * General-purpose sliding-window rate limiter.
 * Used for: IP-based case submission limiting, account daily submission limits.
 *
 * Each key (e.g. an IP address or "account:{username}") has its own counter
 * with a configurable window and max count.
 */
@Service
public class RequestRateLimiter {

    private static final int IP_MAX_SUBMISSIONS         = 5;
    private static final long IP_WINDOW_MS              = 10 * 60 * 1_000L; // 10 minutes

    private static final int ACCOUNT_MAX_DAILY          = 3;
    private static final long ACCOUNT_WINDOW_MS         = 24 * 60 * 60 * 1_000L; // 24 hours

    private final ConcurrentHashMap<String, AttemptRecord> ipBucket     = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AttemptRecord> accountBucket = new ConcurrentHashMap<>();

    public void checkAndRecordIpSubmission(String ip) {
        checkAndRecord(ip, ipBucket, IP_MAX_SUBMISSIONS, IP_WINDOW_MS,
                "Too many death case submissions from this network. Try again in 10 minutes.");
    }

    public void checkAndRecordAccountSubmission(String username) {
        checkAndRecord("account:" + username, accountBucket, ACCOUNT_MAX_DAILY, ACCOUNT_WINDOW_MS,
                "Daily submission limit reached (max " + ACCOUNT_MAX_DAILY +
                " new cases per account per day). Try again tomorrow.");
    }

    private void checkAndRecord(String key, ConcurrentHashMap<String, AttemptRecord> bucket,
                                int max, long windowMs, String errorMessage) {
        AttemptRecord record = bucket.compute(key, (k, existing) -> {
            long now = System.currentTimeMillis();
            if (existing == null || (now - existing.windowStartMs()) >= windowMs) {
                return new AttemptRecord(1, now);
            }
            return new AttemptRecord(existing.count() + 1, existing.windowStartMs());
        });

        if (record.count() > max) {
            throw new SecurityException(errorMessage);
        }
    }

    private record AttemptRecord(int count, long windowStartMs) {}
}
