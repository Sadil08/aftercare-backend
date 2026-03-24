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
        try {
            jdbcTemplate.execute("ALTER TABLE user_roles DROP CONSTRAINT IF EXISTS user_roles_role_check;");
            System.out.println("============== DROPPED user_roles_role_check CONSTRAINT ==============");
            
            jdbcTemplate.execute("ALTER TABLE death_cases DROP CONSTRAINT IF EXISTS death_cases_status_check;");
            System.out.println("============== DROPPED death_cases_status_check CONSTRAINT ==============");
        } catch (Exception e) {
            System.out.println("============== FAILED TO DROP CONSTRAINT: " + e.getMessage() + " ==============");
        }
    }
}
