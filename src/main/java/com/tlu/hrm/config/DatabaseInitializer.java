package com.tlu.hrm.config;

import com.tlu.hrm.model.Role;
import com.tlu.hrm.model.User;
import com.tlu.hrm.model.UserRole;
import com.tlu.hrm.repository.RoleRepository;
import com.tlu.hrm.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        seedRoles();
        seedDefaultAdmin();
    }

    private void seedRoles() throws Exception {
        if (roleRepository.count() > 0) {
            return;
        }

        ClassPathResource resource = new ClassPathResource("setup/roles.csv");
        if (resource.exists()) {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                boolean isHeader = true;
                while ((line = reader.readLine()) != null) {
                    if (isHeader) {
                        isHeader = false;
                        continue;
                    }
                    if (line.trim().isEmpty()) {
                        continue;
                    }
                    String[] parts = line.split(",", 2);
                    if (parts.length >= 2) {
                        String name = parts[0].trim();
                        String description = parts[1].trim();
                        seedRole(name, description);
                    }
                }
            }
        } else {
            System.err.println("Setup file setup/roles.csv not found!");
            // Fallback seeding just in case
            seedRole("ROLE_ADMIN", "Quản trị viên hệ thống");
            seedRole("HR_MANAGER", "Quản lý nhân sự");
            seedRole("HR_USER", "Nhân viên nhân sự");
        }
    }

    private void seedDefaultAdmin() {
        if (userRepository.count() > 0) {
            return;
        }

        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> seedRole("ROLE_ADMIN", "Quản trị viên hệ thống"));
        
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin")); // Encrypted default password
        admin.setEmail("admin@hrm.com");
        admin.setActive(true);
        admin.setVoided(false);
        admin.setUserRoles(new HashSet<>());

        UserRole userRole = new UserRole();
        userRole.setUser(admin);
        userRole.setRole(adminRole);
        admin.getUserRoles().add(userRole);

        userRepository.save(admin);
        System.out.println("Default admin user (username: admin, password: admin) created successfully.");
    }

    private Role seedRole(String name, String description) {
        return roleRepository.findByName(name).orElseGet(() -> {
            Role role = new Role();
            role.setName(name);
            role.setDescription(description);
            role.setVoided(false);
            Role saved = roleRepository.save(role);
            System.out.println("Role " + name + " seeded successfully.");
            return saved;
        });
    }
}
