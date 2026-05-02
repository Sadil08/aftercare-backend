package com.aftercare.aftercare_portal;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.jdbc.core.JdbcTemplate;

@Component
public class DatabaseFixer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseFixer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        fixConstraint("user_roles", "user_roles_role_check", null);
        fixConstraint("death_cases", "death_cases_status_check",
                "status IN ('PENDING_GN_REVIEW','PENDING_DOCTOR_ASSIGNMENT','PENDING_B12_MEDICAL'," +
                "'PENDING_REGISTRAR_REVIEW','CR2_ISSUED_CLOSED','REJECTED_UNNATURAL_DEATH')");
    }

    private void fixConstraint(String table, String constraint, String checkExpression) {
        try {
            jdbcTemplate.execute("ALTER TABLE " + table + " DROP CONSTRAINT IF EXISTS " + constraint + ";");
            System.out.println("=== DROPPED " + constraint + " ===");
            if (checkExpression != null) {
                jdbcTemplate.execute("ALTER TABLE " + table + " ADD CONSTRAINT " + constraint +
                        " CHECK (" + checkExpression + ");");
                System.out.println("=== ADDED " + constraint + " with all valid values ===");
            }
        } catch (Exception e) {
            System.out.println("=== FAILED to fix " + constraint + ": " + e.getMessage() + " ===");
        }
    }
}
