package com.tlu.hrm.service.impl;

import com.tlu.hrm.dto.request.UserDto;
import com.tlu.hrm.dto.search.SearchDto;
import com.tlu.hrm.model.User;
import com.tlu.hrm.model.Role;
import com.tlu.hrm.model.UserRole;
import com.tlu.hrm.repository.StaffRepository;
import com.tlu.hrm.repository.UserRepository;
import com.tlu.hrm.service.UserService;
import com.tlu.hrm.exception.ResourceNotFoundException;
import com.tlu.hrm.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private SecurityUtils securityUtils;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Override
    public Page<UserDto> getAllUsers(SearchDto searchDto) {
        List<User> filteredList = userRepository.findAll().stream()
                .filter(user -> user.getIsDeleted() == null || !user.getIsDeleted())
                .filter(user -> {
                    if (searchDto != null) {
                        // 1. Keyword search (username, email, staff display name, staff code)
                        if (searchDto.getKeyword() != null && !searchDto.getKeyword().isEmpty()) {
                            String keyword = searchDto.getKeyword().toLowerCase();
                            boolean matches = (user.getUsername() != null
                                    && user.getUsername().toLowerCase().contains(keyword))
                                    || (user.getStaff() != null && ((user.getStaff().getDisplayName() != null
                                            && user.getStaff().getDisplayName().toLowerCase().contains(keyword))
                                            || (user.getStaff().getStaffCode() != null && user.getStaff().getStaffCode()
                                                    .toLowerCase().contains(keyword))));
                            if (!matches)
                                return false;
                        }
                        // 2. Active status filter
                        if (searchDto.getActive() != null) {
                            if (user.getActive() == null || !user.getActive().equals(searchDto.getActive())) {
                                return false;
                            }
                        }
                        // 3. Department filter (linked via staff)
                        if (searchDto.getDepartmentId() != null) {
                            if (user.getStaff() == null || user.getStaff().getDepartment() == null
                                    || !user.getStaff().getDepartment().getId().equals(searchDto.getDepartmentId())) {
                                return false;
                            }
                        }
                        // 4. Position filter (linked via staff)
                        if (searchDto.getPositionId() != null) {
                            if (user.getStaff() == null || user.getStaff().getPosition() == null
                                    || !user.getStaff().getPosition().getId().equals(searchDto.getPositionId())) {
                                return false;
                            }
                        }
                        // 5. Role filter
                        if (searchDto.getRoleId() != null) {
                            boolean hasRole = user.getUserRoles() != null && user.getUserRoles().stream()
                                    .anyMatch(ur -> ur.getRole() != null
                                            && ur.getRole().getId().equals(searchDto.getRoleId()));
                            if (!hasRole)
                                return false;
                        }
                    }
                    return true;
                })
                .collect(Collectors.toList());

        int total = filteredList.size();
        int pageNum = 0;
        int size = 10;

        if (searchDto != null) {
            pageNum = searchDto.getPageIndex() >= 1 ? searchDto.getPageIndex() - 1 : 0;
            size = searchDto.getPageSize() > 0 ? searchDto.getPageSize() : 10;
        }

        int fromIndex = pageNum * size;
        int toIndex = Math.min(fromIndex + size, total);

        List<UserDto> pageContent = new java.util.ArrayList<>();
        if (fromIndex < total) {
            pageContent = filteredList.subList(fromIndex, toIndex).stream()
                    .map(UserDto::new)
                    .collect(Collectors.toList());
        }

        return new PageImpl<>(pageContent, PageRequest.of(pageNum, size), total);
    }

    @Override
    public List<UserDto> getAllUsersUnpaginated() {
        return userRepository.findAll().stream()
                .filter(user -> user.getIsDeleted() == null || !user.getIsDeleted())
                .map(UserDto::new)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto getUserById(UUID id) {
        Optional<UserDto> user = userRepository.findById(id).map(UserDto::new);
        if (!user.isPresent()) {
            throw new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + id);
        }
        return user.get();
    }

    @Override
    public UserDto saveUser(UserDto userDto) {
        User user = toEntity(userDto);
        User savedUser = userRepository.save(user);
        return new UserDto(savedUser);
    }

    @Override
    public void deleteUser(UUID id) {
        User currentUser = securityUtils.getCurrentUser();
        if (currentUser != null && currentUser.getId().equals(id)) {
            throw new IllegalArgumentException("Bạn không thể tự xóa tài khoản của chính mình");
        }
        userRepository.findById(id).ifPresent(user -> {
            user.setIsDeleted(true);
            userRepository.save(user);
        });
    }

    @Override
    public boolean existsById(UUID id) {
        return userRepository.findById(id)
                .map(user -> user.getIsDeleted() == null || !user.getIsDeleted())
                .orElse(false);
    }

    private User toEntity(UserDto dto) {
        User user;
        if (dto.getId() != null) {
            user = userRepository.findById(dto.getId()).orElse(new User());
        } else {
            user = new User();
        }
        user.setUsername(dto.getUsername());
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            // Encode password only if it is a new user or if password has changed
            if (user.getId() == null || !dto.getPassword().equals(user.getPassword())) {
                user.setPassword(passwordEncoder.encode(dto.getPassword()));
            }
        }
        user.setActive(dto.getActive());
        if (user.getUserRoles() == null) {
            user.setUserRoles(new HashSet<>());
        } else {
            user.getUserRoles().clear();
        }
        if (dto.getRoles() != null) {
            for (Role role : dto.getRoles()) {
                UserRole userRole = new UserRole();
                userRole.setUser(user);
                userRole.setRole(role);
                user.getUserRoles().add(userRole);
            }
        }

        if (dto.getStaffId() != null) {
            staffRepository.findById(dto.getStaffId()).ifPresent(user::setStaff);
        } else {
            user.setStaff(null);
        }
        return user;
    }

    @Override
    public UserDto lockUser(UUID id) {
        User currentUser = securityUtils.getCurrentUser();
        if (currentUser != null && currentUser.getId().equals(id)) {
            throw new IllegalArgumentException("Không thể tự khóa tài khoản của chính mình");
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + id));
        user.setActive(false);
        User saved = userRepository.save(user);
        return new UserDto(saved);
    }

    @Override
    public UserDto unlockUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + id));
        user.setActive(true);
        User saved = userRepository.save(user);
        return new UserDto(saved);
    }
}
