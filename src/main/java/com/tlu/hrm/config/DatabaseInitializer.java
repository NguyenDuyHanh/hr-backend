package com.tlu.hrm.config;

import com.tlu.hrm.enums.RoleType;
import com.tlu.hrm.model.Role;
import com.tlu.hrm.model.User;
import com.tlu.hrm.model.UserRole;
import com.tlu.hrm.model.Department;
import com.tlu.hrm.model.Position;
import com.tlu.hrm.repository.RoleRepository;
import com.tlu.hrm.repository.UserRepository;
import com.tlu.hrm.repository.DepartmentRepository;
import com.tlu.hrm.repository.PositionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Optional;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Value("${app.admin.username}")
    private String adminUsername;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        seedRoles();
        seedDefaultAdmin();
        seedDepartments();
        seedPositions();
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
            seedRole(RoleType.ROLE_ADMIN.getName(), RoleType.ROLE_ADMIN.getDescription());
            seedRole(RoleType.HR_MANAGER.getName(), RoleType.HR_MANAGER.getDescription());
            seedRole(RoleType.HR_EMPLOYEE.getName(), RoleType.HR_EMPLOYEE.getDescription());
        }
    }

    private void seedDefaultAdmin() {
        if (userRepository.count() > 0) {
            return;
        }

        Role adminRole = roleRepository.findByName(RoleType.ROLE_ADMIN.getName())
                .orElseGet(() -> seedRole(RoleType.ROLE_ADMIN.getName(), RoleType.ROLE_ADMIN.getDescription()));

        User admin = new User();
        admin.setUsername(adminUsername);
        admin.setPassword(passwordEncoder.encode(adminPassword)); // Encrypted default password
        admin.setEmail(adminEmail);
        admin.setActive(true);
        admin.setVoided(false);
        admin.setUserRoles(new HashSet<>());

        UserRole userRole = new UserRole();
        userRole.setUser(admin);
        userRole.setRole(adminRole);
        admin.getUserRoles().add(userRole);

        userRepository.save(admin);
        System.out.println("Default admin user (username: " + adminUsername + ") created successfully.");
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

    private void seedDepartments() throws Exception {
        ClassPathResource resource = new ClassPathResource("setup/departments.csv");
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
                    String[] parts = line.split(",", 3);
                    if (parts.length >= 2) {
                        String code = parts[0].trim();
                        String name = parts[1].trim();
                        String description = parts.length >= 3 ? parts[2].trim() : "";
                        
                        Department dept = departmentRepository.findByCode(code)
                                .orElseGet(() -> {
                                    Department newDept = new Department();
                                    newDept.setCode(code);
                                    return newDept;
                                });
                        dept.setName(name);
                        dept.setDescription(description);
                        dept.setVoided(false);
                        departmentRepository.save(dept);
                        System.out.println("Department " + name + " (" + code + ") seeded successfully.");
                    }
                }
            }
        }
    }

    private void seedPositions() throws Exception {
        ClassPathResource resource = new ClassPathResource("setup/positions.csv");
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
                    String[] parts = line.split(",", 4);
                    if (parts.length >= 2) {
                        String code = parts[0].trim();
                        String name = parts[1].trim();
                        String description = parts.length >= 3 ? parts[2].trim() : "";
                        String deptCode = parts.length >= 4 ? parts[3].trim() : "";
                        
                        Position pos = positionRepository.findByCode(code)
                                .orElseGet(() -> {
                                    Position newPos = new Position();
                                    newPos.setCode(code);
                                    return newPos;
                                });
                        pos.setName(name);
                        pos.setDescription(description);
                        pos.setVoided(false);
                        
                        if (!deptCode.isEmpty()) {
                            departmentRepository.findByCode(deptCode).ifPresent(pos::setDepartment);
                        }
                        
                        positionRepository.save(pos);
                        System.out.println("Position " + name + " (" + code + ") seeded successfully.");
                    }
                }
            }
        }
    }
}
