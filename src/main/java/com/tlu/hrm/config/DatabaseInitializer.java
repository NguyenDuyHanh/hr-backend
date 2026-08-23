package com.tlu.hrm.config;

import com.tlu.hrm.enums.RoleType;
import com.tlu.hrm.model.Role;
import com.tlu.hrm.model.User;
import com.tlu.hrm.model.UserRole;
import com.tlu.hrm.model.Department;
import com.tlu.hrm.model.Position;
import com.tlu.hrm.model.Ethnic;
import com.tlu.hrm.model.Bank;
import com.tlu.hrm.model.AdministrativeUnit;
import com.tlu.hrm.repository.RoleRepository;
import com.tlu.hrm.repository.UserRepository;
import com.tlu.hrm.repository.DepartmentRepository;
import com.tlu.hrm.repository.PositionRepository;
import com.tlu.hrm.repository.EthnicRepository;
import com.tlu.hrm.repository.BankRepository;
import com.tlu.hrm.repository.AdministrativeUnitRepository;
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
    private EthnicRepository ethnicRepository;

    @Autowired
    private BankRepository bankRepository;

    @Autowired
    private AdministrativeUnitRepository administrativeUnitRepository;

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
        seedEthnics();
        seedBanks();
        seedAdministrativeUnits();
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
        admin.setActive(true);
        admin.setIsDeleted(false);
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
            role.setIsDeleted(false);
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
                        dept.setIsDeleted(false);
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
                        pos.setIsDeleted(false);

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

    private void seedEthnics() throws Exception {
        if (ethnicRepository.count() > 0) {
            return;
        }

        ClassPathResource resource = new ClassPathResource("setup/ethnics.csv");
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
                        String name = parts[1].trim().replaceAll("^\"|\"$", "");
                        String description = parts.length >= 3 ? parts[2].trim().replaceAll("^\"|\"$", "") : "";

                        com.tlu.hrm.model.Ethnic ethnic = ethnicRepository.findByCode(code).orElseGet(() -> {
                            com.tlu.hrm.model.Ethnic e = new com.tlu.hrm.model.Ethnic();
                            e.setCode(code);
                            return e;
                        });
                        ethnic.setName(name);
                        ethnic.setDescription(description);
                        ethnic.setIsDeleted(false);
                        ethnicRepository.save(ethnic);
                    }
                }
                System.out.println("Ethnics seeded successfully.");
            }
        }
    }

    private void seedBanks() throws Exception {
        if (bankRepository.count() > 0) {
            return;
        }

        ClassPathResource resource = new ClassPathResource("setup/bank.csv");
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
                    String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                    if (parts.length >= 5) {
                        String name = parts[1].trim().replaceAll("^\"|\"$", "");
                        String code = parts[2].trim().replaceAll("^\"|\"$", "");
                        String bin = parts[3].trim().replaceAll("^\"|\"$", "");
                        String shortName = parts[4].trim().replaceAll("^\"|\"$", "");
                        String logo = parts.length > 5 ? parts[5].trim().replaceAll("^\"|\"$", "") : "";
                        String swiftCode = parts.length > 11 ? parts[11].trim().replaceAll("^\"|\"$", "") : "";

                        com.tlu.hrm.model.Bank bank = bankRepository.findByCode(code).orElseGet(() -> {
                            com.tlu.hrm.model.Bank b = new com.tlu.hrm.model.Bank();
                            b.setCode(code);
                            return b;
                        });
                        bank.setName(name);
                        bank.setShortName(shortName);
                        bank.setBin(bin);
                        bank.setLogo(logo);
                        bank.setSwiftCode(swiftCode);
                        bank.setIsDeleted(false);
                        bankRepository.save(bank);
                    }
                }
                System.out.println("Banks seeded successfully.");
            }
        }
    }

    private void seedAdministrativeUnits() throws Exception {
        if (administrativeUnitRepository.count() > 0) {
            return;
        }

        ClassPathResource resource = new ClassPathResource("setup/administrative_units.csv");
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
                    String[] parts = line.split(",", -1);
                    if (parts.length >= 7) {
                        String code = parts[0].trim();
                        String name = parts[1].trim();
                        String codename = parts[2].trim();
                        String divisionType = parts[3].trim();
                        String shortCodename = parts[4].trim();
                        String phoneCode = parts[5].trim();
                        int level = 1;
                        try {
                            level = Integer.parseInt(parts[6].trim());
                        } catch (Exception ignored) {
                        }
                        String parentCode = parts.length >= 8 ? parts[7].trim() : "";

                        final int unitLevel = level;
                        final String unitCode = code;
                        AdministrativeUnit unit = administrativeUnitRepository.findByCodeAndLevel(code, level).orElseGet(() -> {
                            AdministrativeUnit u = new AdministrativeUnit();
                            u.setCode(unitCode);
                            u.setLevel(unitLevel);
                            return u;
                        });
                        unit.setName(name);
                        unit.setCodename(codename);
                        unit.setDivisionType(divisionType);
                        unit.setShortCodename(shortCodename);
                        unit.setPhoneCode(phoneCode);
                        unit.setLevel(level);
                        unit.setIsDeleted(false);

                        if (level == 1) {
                            unit.setParent(null);
                            unit.setParentCode(null);
                        } else if (!parentCode.isEmpty()) {
                            unit.setParentCode(parentCode);
                            Optional<AdministrativeUnit> parentOpt = administrativeUnitRepository.findByCodeAndLevel(parentCode, level - 1);
                            if (parentOpt.isPresent()) {
                                unit.setParent(parentOpt.get());
                            } else {
                                unit.setParent(null);
                            }
                        } else {
                            unit.setParent(null);
                            unit.setParentCode(null);
                        }

                        administrativeUnitRepository.save(unit);
                    }
                }
                System.out.println("Administrative Units seeded successfully.");
            }
        } else {
            System.err.println("Setup file setup/administrative_units.csv not found!");
        }
    }
}

