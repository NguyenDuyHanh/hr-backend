package com.tlu.hrm.service;

import com.tlu.hrm.dto.request.UserDto;
import com.tlu.hrm.dto.search.SearchDto;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface UserService {
    Page<UserDto> getAllUsers(SearchDto searchDto);
    List<UserDto> getAllUsersUnpaginated();
    UserDto getUserById(UUID id);
    UserDto saveUser(UserDto userDto);
    void deleteUser(UUID id);
    boolean existsById(UUID id);
    UserDto lockUser(UUID id);
    UserDto unlockUser(UUID id);
}
