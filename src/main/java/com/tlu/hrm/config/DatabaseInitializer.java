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

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // 1. Seed Roles from CSV
        Role adminRole = null;
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
                        Role seeded = seedRole(name, description);
                        if ("ROLE_ADMIN".equals(name)) {
                            adminRole = seeded;
                        }
                    }
                }
            }
        } else {
            System.err.println("Setup file setup/roles.csv not found!");
            // Fallback seeding just in case
            adminRole = seedRole("ROLE_ADMIN", "Quản trị viên hệ thống");
            seedRole("HR_MANAGER", "Quản lý nhân sự");
            seedRole("HR_USER", "Nhân viên nhân sự");
        }

        // 2. Seed default admin user if not exists
        if (userRepository.findByUsername("admin").isEmpty()) {
            if (adminRole == null) {
                adminRole = roleRepository.findByName("ROLE_ADMIN").orElseGet(() -> seedRole("ROLE_ADMIN", "Quản trị viên hệ thống"));
            }
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword("admin"); // plain text password as observed in UserServiceImpl
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
