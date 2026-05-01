package com.aftercare.aftercare_portal;

import com.aftercare.aftercare_portal.entity.Citizen;
import com.aftercare.aftercare_portal.enums.Gender;
import com.aftercare.aftercare_portal.repository.CitizenRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Seeds the citizens table with mock government identity records.
 * In production this table would be populated from the national NIC registry.
 * All seeded officials and test family members are registered here as alive.
 */
@Component
@Order(3)
public class CitizenSeeder implements CommandLineRunner {

    private final CitizenRepository citizenRepository;

    public CitizenSeeder(CitizenRepository citizenRepository) {
        this.citizenRepository = citizenRepository;
    }

    @Override
    public void run(String... args) {
        // Officials — NICs must match exactly what DataSeeder seeds into users table
        seed("197012345678", "Dr. Chaminda Jayawardena", LocalDate.of(1970, 6, 15), Gender.MALE, "No.5 Temple Rd, Kandy");
        seed("197587654321", "Dr. Priyantha Wijesinghe", LocalDate.of(1975, 3, 22), Gender.MALE, "No.12 Galle Rd, Colombo 03");
        seed("200012345678", "Nimal Perera",              LocalDate.of(2000, 8, 10), Gender.MALE, "No.3 Peradeniya Rd, Kandy");
        seed("199887654321", "Kamal Silva",               LocalDate.of(1998, 1, 5),  Gender.MALE, "No.44 Havelock Rd, Colombo 05");
        seed("198512349876", "Sunil Fernando",            LocalDate.of(1985, 11, 20), Gender.MALE, "No.8 Station Rd, Kandy");
        seed("199056781234", "Anura Bandara",             LocalDate.of(1990, 4, 30), Gender.MALE, "No.21 Union Pl, Colombo 02");

        // Test family members and potential deceased (for demo purposes)
        seed("901234567V",   "Test Family Member",        LocalDate.of(1990, 1, 1),  Gender.MALE, "No.10 Main St, Kandy");
        seed("801234567V",   "Test GN Officer",           LocalDate.of(1980, 5, 15), Gender.MALE, "No.7 Hill St, Kandy");
        seed("850101001V",   "Prasad Rathnayake",         LocalDate.of(1985, 1, 1),  Gender.MALE, "No.15 Bogambara Rd, Kandy");
        seed("750201002V",   "Kamala Dissanayake",        LocalDate.of(1975, 2, 1),  Gender.FEMALE, "No.22 Lewella Rd, Kandy");
    }

    private void seed(String nic, String fullName, LocalDate dob, Gender gender, String address) {
        if (!citizenRepository.existsByNic(nic)) {
            citizenRepository.save(new Citizen(nic, fullName, dob, gender, address));
        }
    }
}
