package com.aftercare.aftercare_portal;

import com.aftercare.aftercare_portal.entity.Sector;
import com.aftercare.aftercare_portal.entity.User;
import com.aftercare.aftercare_portal.enums.Role;
import com.aftercare.aftercare_portal.repository.SectorRepository;
import com.aftercare.aftercare_portal.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Order(2) // Run after DatabaseFixer
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final SectorRepository sectorRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, SectorRepository sectorRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.sectorRepository = sectorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // ── Ensure sectors exist ──
        Sector kandySector = sectorRepository.findByCode("KANDY-01")
                .orElseGet(() -> sectorRepository.save(new Sector("KANDY-01", "Kandy Division 01", "Kandy")));

        Sector colomboSector = sectorRepository.findByCode("COLOMBO-01")
                .orElseGet(() -> sectorRepository.save(new Sector("COLOMBO-01", "Colombo Division 01", "Colombo")));

        // ── Seed Doctor users ──
        seedUser("doc_kandy01", "doc.kandy01@gov.lk", "Dr. Chaminda Jayawardena",
                "Doc@12345", "0761234567", "197012345678",
                Role.DOCTOR, kandySector);

        seedUser("doc_colombo01", "doc.colombo01@gov.lk", "Dr. Priyantha Wijesinghe",
                "Doc@12345", "0769876543", "197587654321",
                Role.DOCTOR, colomboSector);

        // ── Seed Grama Niladhari users ──
        seedUser("gn_kandy01", "gn.kandy01@gov.lk", "Nimal Perera",
                "Gn@12345", "0771234567", "200012345678",
                Role.GRAMA_NILADHARI, kandySector);

        seedUser("gn_colombo01", "gn.colombo01@gov.lk", "Kamal Silva",
                "Gn@12345", "0779876543", "199887654321",
                Role.GRAMA_NILADHARI, colomboSector);

        // ── Seed Registrar users ──
        seedUser("reg_kandy01", "reg.kandy01@gov.lk", "Sunil Fernando",
                "Reg@12345", "0712345678", "198512349876",
                Role.REGISTRAR, kandySector);

        seedUser("reg_colombo01", "reg.colombo01@gov.lk", "Anura Bandara",
                "Reg@12345", "0718765432", "199056781234",
                Role.REGISTRAR, colomboSector);

        // ── Seed Cemetery users ──
        seedUser("cem_kandy01", "cem.kandy01@gov.lk", "Mahaiyawa Cemetery Owner",
                "Cem@12345", "0721111111", "198011111111",
                Role.CEMETERY, kandySector);

        seedUser("cem_colombo01", "cem.colombo01@gov.lk", "Kanatte Cemetery Owner",
                "Cem@12345", "0722222222", "198022222222",
                Role.CEMETERY, colomboSector);
    }

    private void seedUser(String username, String email, String fullName,
                          String rawPassword, String phone, String nicNo,
                          Role role, Sector sector) {
        if (userRepository.existsByUsername(username)) {
            User existing = userRepository.findByUsername(username).orElse(null);
            if (existing != null) {
                boolean changed = false;
                if (!existing.getRoles().contains(role)) {
                    existing.grantRole(role); // also auto-assigns doctorId if role == DOCTOR
                    changed = true;
                    System.out.println("============== REPAIRED USER ROLE: " + username + " (" + role + ") ==============");
                }
                if (existing.getSector() == null && sector != null) {
                    existing.assignSector(sector);
                    changed = true;
                    System.out.println("============== ASSIGNED SECTOR to existing user: " + username + " -> " + sector.getCode() + " ==============");
                }
                // Repair: ensure existing doctors have a doctorId
                if (role == com.aftercare.aftercare_portal.enums.Role.DOCTOR && existing.getDoctorId() == null) {
                    existing.grantRole(role); // triggers doctorId generation via grantRole logic
                    changed = true;
                    System.out.println("============== ASSIGNED DOCTOR ID to " + username + ": " + existing.getDoctorId() + " ==============");
                }
                if (changed) {
                    userRepository.save(existing);
                } else {
                    System.out.println("============== USER ALREADY EXISTS (intact): " + username
                            + (existing.getDoctorId() != null ? " | Doctor ID: " + existing.getDoctorId() : "") + " ==============");
                }
            }
            return;
        }

        User user = new User(username, email, fullName,
                passwordEncoder.encode(rawPassword), phone, nicNo);
        user.grantRole(role); // auto-assigns doctorId if role == DOCTOR
        user.assignSector(sector);
        userRepository.save(user);

        String doctorInfo = user.getDoctorId() != null ? " | Doctor ID: " + user.getDoctorId() : "";
        System.out.println("============== SEEDED USER: " + username + " (" + role + ")" + doctorInfo + " ==============");
    }
}
