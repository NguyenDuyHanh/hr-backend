package com.tlu.hrm.service.impl;

import com.tlu.hrm.dto.request.UserDto;
import com.tlu.hrm.dto.search.SearchDto;
import com.tlu.hrm.model.User;
import com.tlu.hrm.repository.StaffRepository;
import com.tlu.hrm.repository.UserRepository;
import com.tlu.hrm.service.UserService;
import com.tlu.hrm.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

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

    @Override
    public Page<UserDto> getAllUsers(SearchDto searchDto) {
        List<User> filteredList = userRepository.findAll().stream()
                .filter(user -> user.getVoided() == null || !user.getVoided())
                .filter(user -> {
                    if (searchDto != null && searchDto.getKeyword() != null && !searchDto.getKeyword().isEmpty()) {
                        String keyword = searchDto.getKeyword().toLowerCase();
                        return (user.getUsername() != null && user.getUsername().toLowerCase().contains(keyword))
                            || (user.getEmail() != null && user.getEmail().toLowerCase().contains(keyword));
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
                .filter(user -> user.getVoided() == null || !user.getVoided())
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
        userRepository.findById(id).ifPresent(user -> {
            user.setVoided(true);
            userRepository.save(user);
        });
    }

    @Override
    public boolean existsById(UUID id) {
        return userRepository.findById(id)
                .map(user -> user.getVoided() == null || !user.getVoided())
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
            user.setPassword(dto.getPassword());
        }
        user.setEmail(dto.getEmail());
        user.setActive(dto.getActive());
        user.setRoles(dto.getRoles());
        
        if (dto.getStaffId() != null) {
            staffRepository.findById(dto.getStaffId()).ifPresent(user::setStaff);
        } else {
            user.setStaff(null);
        }
        return user;
    }
}
