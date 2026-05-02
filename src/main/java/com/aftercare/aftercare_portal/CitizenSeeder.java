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
        // ── Officials (NICs must match DataSeeder) ──────────────────────────────────
        seed("197012345678", "Dr. Chaminda Jayawardena", "චාමින්ද ජයවර්ධන",
                LocalDate.of(1970, 6, 15), Gender.MALE,
                "Sri Lankan", "Sinhalese",
                "No.5 Temple Rd, Kandy", "Kandy", "Gangawata Korale", "Kandy North",
                "Medical Officer", "Married");

        seed("197587654321", "Dr. Priyantha Wijesinghe", "ප්‍රියන්ත විජේසිංහ",
                LocalDate.of(1975, 3, 22), Gender.MALE,
                "Sri Lankan", "Sinhalese",
                "No.12 Galle Rd, Colombo 03", "Colombo", "Colombo", "Colombo 3",
                "Medical Officer", "Married");

        seed("200012345678", "Nimal Perera", "නිමල් පෙරේරා",
                LocalDate.of(2000, 8, 10), Gender.MALE,
                "Sri Lankan", "Sinhalese",
                "No.3 Peradeniya Rd, Kandy", "Kandy", "Gangawata Korale", "Kandy South",
                "Government Officer", "Unmarried");

        seed("199887654321", "Kamal Silva", "කමල් සිල්වා",
                LocalDate.of(1998, 1, 5), Gender.MALE,
                "Sri Lankan", "Sinhalese",
                "No.44 Havelock Rd, Colombo 05", "Colombo", "Colombo", "Colombo 5",
                "Government Officer", "Unmarried");

        seed("198512349876", "Sunil Fernando", "සුනිල් ප්‍රනාන්දු",
                LocalDate.of(1985, 11, 20), Gender.MALE,
                "Sri Lankan", "Sinhalese",
                "No.8 Station Rd, Kandy", "Kandy", "Gangawata Korale", "Kandy Central",
                "Government Officer", "Married");

        seed("199056781234", "Anura Bandara", "අනුර බංඩාර",
                LocalDate.of(1990, 4, 30), Gender.MALE,
                "Sri Lankan", "Sinhalese",
                "No.21 Union Pl, Colombo 02", "Colombo", "Colombo", "Colombo 2",
                "Government Officer", "Married");

        seed("198011111111", "Mahaiyawa Cemetery Officer", "මහාඉයාව සුසාන භූමි නිලධාරී",
                LocalDate.of(1980, 3, 1), Gender.MALE,
                "Sri Lankan", "Sinhalese",
                "No.1 Cemetery Rd, Kandy", "Kandy", "Gangawata Korale", "Mahaiyawa",
                "Cemetery Officer", "Married");

        seed("198022222222", "Kanatte Cemetery Officer", "කනත්ත සුසාන භූමි නිලධාරී",
                LocalDate.of(1980, 6, 1), Gender.MALE,
                "Sri Lankan", "Sinhalese",
                "No.1 Kanatte Rd, Colombo 05", "Colombo", "Colombo", "Borella",
                "Cemetery Officer", "Married");

        // ── Test family member (account holder) ────────────────────────────────────
        seed("750201012V", "Sadil E", "සාදිල් ඊ",
                LocalDate.of(1975, 2, 1), Gender.MALE,
                "Sri Lankan", "Sinhalese",
                "No.10 Main St, Kandy", "Kandy", "Gangawata Korale", "Kandy Central",
                "Software Engineer", "Unmarried");

        // ── Test deceased citizens (for form pre-fill demos) ────────────────────────

        seed("550123456V", "Sunil Perera", "සුනිල් පෙරේරා",
                LocalDate.of(1955, 3, 15), Gender.MALE,
                "Sri Lankan", "Sinhalese",
                "14 Permanent Home, Kandy", "Kandy", "Gangawata Korale", "Kandy Central",
                "Retired Teacher", "Married");

        seed("450101001V", "Karunawathi Bandara", "කරුණාවතී බංඩාර",
                LocalDate.of(1945, 1, 1), Gender.FEMALE,
                "Sri Lankan", "Sinhalese",
                "No.5 Lake View Rd, Kandy", "Kandy", "Udunuwara", "Udunuwara North",
                "Housewife", "Widowed");

        seed("480501002V", "Aiyathurai Shanmuganathan", "ஐயத்துரை சண்முகநாதன்",
                LocalDate.of(1948, 5, 1), Gender.MALE,
                "Sri Lankan", "Tamil",
                "No.22 Temple Lane, Colombo 07", "Colombo", "Colombo", "Colombo 7",
                "Retired Accountant", "Married");

        seed("520615003V", "Fathima Rizna", "ෆාතිමා රිස්නා",
                LocalDate.of(1952, 6, 15), Gender.FEMALE,
                "Sri Lankan", "Muslim",
                "No.88 Baseline Rd, Colombo 08", "Colombo", "Colombo", "Colombo 8",
                "Retired Teacher", "Married");

        seed("380202004V", "Piyadasa Wijerathne", "පියදාස විජේරත්න",
                LocalDate.of(1938, 2, 2), Gender.MALE,
                "Sri Lankan", "Sinhalese",
                "No.3 Rajapihilla Rd, Kandy", "Kandy", "Gangawata Korale", "Kandy South",
                "Retired Farmer", "Widowed");

        seed("601010005V", "Nilmini Rajapaksha", "නිල්මිනී රාජපක්ෂ",
                LocalDate.of(1960, 10, 10), Gender.FEMALE,
                "Sri Lankan", "Sinhalese",
                "No.45 Flower Rd, Colombo 03", "Colombo", "Colombo", "Colombo 3",
                "Retired Nurse", "Married");

        seed("420830006V", "Sirisena Gunawardena", "සිරිසේන ගුණවර්ධන",
                LocalDate.of(1942, 8, 30), Gender.MALE,
                "Sri Lankan", "Sinhalese",
                "No.7 High Level Rd, Nugegoda", "Colombo", "Maharagama", "Nugegoda",
                "Retired Engineer", "Married");

        seed("550723007V", "Kamala Wickramasinghe", "කමලා වික්‍රමසිංහ",
                LocalDate.of(1955, 7, 23), Gender.FEMALE,
                "Sri Lankan", "Sinhalese",
                "No.12 Kandy Rd, Peradeniya", "Kandy", "Gangawata Korale", "Peradeniya",
                "Retired Lecturer", "Married");

        // ── Legacy seeds (kept for test compatibility) ──────────────────────────────
        seedLegacy("901234567V", "Test Family Member",  LocalDate.of(1990, 1, 1), Gender.MALE, "No.10 Main St, Kandy");
        seedLegacy("801234567V", "Test GN Officer",     LocalDate.of(1980, 5, 15), Gender.MALE, "No.7 Hill St, Kandy");
        seedLegacy("850101001V", "Prasad Rathnayake",   LocalDate.of(1985, 1, 1), Gender.MALE, "No.15 Bogambara Rd, Kandy");
        seedLegacy("750201002V", "Kamala Dissanayake",  LocalDate.of(1975, 2, 1), Gender.FEMALE, "No.22 Lewella Rd, Kandy");
    }

    private void seed(String nic, String fullName, String fullNameSinhala,
                      LocalDate dob, Gender gender,
                      String nationality, String ethnicity,
                      String address, String district,
                      String division, String gnDivision,
                      String occupation, String maritalStatus) {
        if (!citizenRepository.existsByNic(nic)) {
            citizenRepository.save(new Citizen(nic, fullName, fullNameSinhala,
                    dob, gender, nationality, ethnicity,
                    address, district, division, gnDivision,
                    occupation, maritalStatus));
        }
    }

    private void seedLegacy(String nic, String fullName, LocalDate dob, Gender gender, String address) {
        if (!citizenRepository.existsByNic(nic)) {
            citizenRepository.save(new Citizen(nic, fullName, dob, gender, address));
        }
    }
}
