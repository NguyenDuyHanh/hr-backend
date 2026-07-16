package com.tlu.hrm.service.impl;

import com.tlu.hrm.dto.request.RoleDto;
import com.tlu.hrm.dto.search.SearchDto;
import com.tlu.hrm.exception.ResourceNotFoundException;
import com.tlu.hrm.model.Role;
import com.tlu.hrm.repository.RoleRepository;
import com.tlu.hrm.repository.UserRoleRepository;
import com.tlu.hrm.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Override
    public Page<RoleDto> pagingRoles(SearchDto searchDto) {
        List<Role> list = roleRepository.findAll().stream()
                .filter(r -> r.getIsDeleted() == null || !r.getIsDeleted())
                .filter(r -> {
                    if (searchDto != null && searchDto.getKeyword() != null && !searchDto.getKeyword().isEmpty()) {
                        String kw = searchDto.getKeyword().toLowerCase();
                        return (r.getName() != null && r.getName().toLowerCase().contains(kw))
                                || (r.getDescription() != null && r.getDescription().toLowerCase().contains(kw));
                    }
                    return true;
                })
                .sorted((a, b) -> {
                    if (a.getCreateDate() != null && b.getCreateDate() != null) {
                        return b.getCreateDate().compareTo(a.getCreateDate());
                    }
                    return 0;
                })
                .collect(Collectors.toList());

        int total = list.size();
        int pageIndex = 0;
        int pageSize = 10;

        if (searchDto != null) {
            pageIndex = searchDto.getPageIndex() >= 1 ? searchDto.getPageIndex() - 1 : 0;
            pageSize = searchDto.getPageSize() > 0 ? searchDto.getPageSize() : 10;
        }

        int fromIndex = pageIndex * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);

        List<RoleDto> pageContent = new ArrayList<>();
        if (fromIndex < total) {
            pageContent = list.subList(fromIndex, toIndex).stream()
                    .map(RoleDto::new)
                    .collect(Collectors.toList());
        }

        return new PageImpl<>(pageContent, PageRequest.of(pageIndex, pageSize), total);
    }

    @Override
    public List<RoleDto> getAllRoles() {
        return roleRepository.findAll().stream()
                .filter(r -> r.getIsDeleted() == null || !r.getIsDeleted())
                .map(RoleDto::new)
                .collect(Collectors.toList());
    }

    @Override
    public RoleDto getById(UUID id) {
        Role entity = roleRepository.findById(id)
                .filter(r -> r.getIsDeleted() == null || !r.getIsDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vai trò với ID: " + id));
        return new RoleDto(entity);
    }

    @Override
    public RoleDto saveRole(RoleDto dto) {
        Role entity;
        String upperName = dto.getName().trim().toUpperCase();
        
        if (dto.getId() != null) {
            entity = roleRepository.findById(dto.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vai trò với ID: " + dto.getId()));
        } else {
            // Kiểm tra xem đã có bản ghi nào trùng tên (bao gồm cả đã soft delete) chưa
            Optional<Role> existingOpt = roleRepository.findByName(upperName);
            if (existingOpt.isPresent()) {
                entity = existingOpt.get(); // Sử dụng lại bản ghi cũ để khôi phục
            } else {
                entity = new Role();
            }
        }

        entity.setName(upperName);
        entity.setDescription(dto.getDescription());
        entity.setIsDeleted(false);

        Role saved = roleRepository.save(entity);
        return new RoleDto(saved);
    }

    private void checkRoleConstraints(UUID id) {
        if (userRoleRepository.existsByRoleId(id)) {
            throw new IllegalArgumentException("Không thể xóa vì vai trò này đang được gán cho người dùng trong hệ thống.");
        }
    }

    @Override
    public void deleteRole(UUID id) {
        Role entity = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vai trò với ID: " + id));
        checkRoleConstraints(id);
        entity.setIsDeleted(true);
        roleRepository.save(entity);
    }

    @Override
    public void deleteMultiple(List<UUID> ids) {
        if (ids != null) {
            for (UUID id : ids) {
                checkRoleConstraints(id);
            }
            for (UUID id : ids) {
                roleRepository.findById(id).ifPresent(entity -> {
                    entity.setIsDeleted(true);
                    roleRepository.save(entity);
                });
            }
        }
    }

    @Override
    public boolean isValidName(RoleDto dto) {
        if (dto == null || dto.getName() == null || dto.getName().trim().isEmpty()) {
            return false;
        }
        String name = dto.getName().trim().toUpperCase();
        Optional<Role> opt = roleRepository.findByName(name);
        if (opt.isPresent()) {
            if (dto.getId() == null) {
                // Cho phép lưu nếu vai trò đã bị soft delete (isDeleted = true), hệ thống sẽ khôi phục vai trò đó
                Role existing = opt.get();
                return existing.getIsDeleted() != null && existing.getIsDeleted();
            }
            return opt.get().getId().equals(dto.getId());
        }
        return true;
    }
}
