package com.greenwich.flowerplus.seeder;

import com.greenwich.flowerplus.common.enums.Gender;
import com.greenwich.flowerplus.common.enums.UserStatus;
import com.greenwich.flowerplus.entity.Role;
import com.greenwich.flowerplus.entity.UserAccount;
import com.greenwich.flowerplus.entity.UserProfile;
import com.greenwich.flowerplus.repository.RoleRepository;
import com.greenwich.flowerplus.repository.UserAccountRepository;
import com.greenwich.flowerplus.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserAccountRepository userAccountRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.db-seeder:false}")
    private boolean isSeederEnabled;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (!isSeederEnabled) {
            log.info("Database Seeder is DISABLED");
            return;
        }

        log.info("Starting Database Seeder...");

        // 1. Seed Roles
        Role roleCustomer = seedRole("CUSTOMER", "Standard customer role");
        Role roleShopOwner = seedRole("SHOP_OWNER", "Shop owner with full simplified admin access");
        Role roleShopStaff = seedRole("SHOP_STAFF", "Shop staff with limited admin access");

        // 2. Seed Users
        seedUser("customer@flowerplus.com", "Customer User", roleCustomer);
        seedUser("owner@flowerplus.com", "Shop Owner", roleShopOwner);
        seedUser("staff@flowerplus.com", "Shop Staff", roleShopStaff);
        
        // Optional: Seed an admin if needed, but user specifically asked for those 3.
        // seedUser("admin@flowerplus.com", "Super Admin", roleAdmin); 

        log.info("Database Seeder Completed Successfully!");
    }

    private Role seedRole(String name, String description) {
        Optional<Role> roleOpt = roleRepository.findByName(name);
        if (roleOpt.isPresent()) {
            return roleOpt.get();
        }
        Role role = Role.builder()
                .name(name)
                .description(description)
                .build();
        return roleRepository.save(role);
    }

    private void seedUser(String email, String fullName, Role role) {
        if (userAccountRepository.findByEmail(email).isPresent()) {
            log.info("User {} already exists.", email);
            return;
        }

        // Create UserAccount
        UserAccount user = UserAccount.builder()
                .username(email) // Using email as username for simplicity
                .email(email)
                .password(passwordEncoder.encode("password123")) // Default password
                .status(UserStatus.ACTIVE)
                .build();

        // Add Role
        user.addRole(role);

        // Save UserAccount (cascades to UserRoles)
        user = userAccountRepository.save(user);

        // Create UserProfile
        UserProfile profile = UserProfile.builder()
                .userId(user.getId())
                .fullName(fullName)
                .gender(Gender.OTHER)
                .firstName("Test")
                .lastName("Test")
                .birthDate(LocalDate.of(1990, 1, 1))
                .phoneNumber("0123456789")
                .build();
        
        userProfileRepository.save(profile);

        log.info("Seeded user: {}", email);
    }
}
