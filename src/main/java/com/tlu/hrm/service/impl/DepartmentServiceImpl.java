package com.tlu.hrm.service.impl;

import com.tlu.hrm.dto.request.DepartmentDto;
import com.tlu.hrm.dto.search.SearchDto;
import com.tlu.hrm.exception.ResourceNotFoundException;
import com.tlu.hrm.model.Department;
import com.tlu.hrm.repository.DepartmentRepository;
import com.tlu.hrm.repository.PositionRepository;
import com.tlu.hrm.repository.StaffRepository;
import com.tlu.hrm.service.DepartmentService;
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
public class DepartmentServiceImpl implements DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private StaffRepository staffRepository;

    @Override
    public Page<DepartmentDto> pagingDepartments(SearchDto searchDto) {
        List<Department> list = departmentRepository.findAll().stream()
                .filter(d -> d.getIsDeleted() == null || !d.getIsDeleted())
                .filter(d -> {
                    if (searchDto != null && searchDto.getKeyword() != null && !searchDto.getKeyword().isEmpty()) {
                        String kw = searchDto.getKeyword().toLowerCase();
                        return (d.getName() != null && d.getName().toLowerCase().contains(kw))
                                || (d.getCode() != null && d.getCode().toLowerCase().contains(kw))
                                || (d.getDescription() != null && d.getDescription().toLowerCase().contains(kw));
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

        List<DepartmentDto> pageContent = new ArrayList<>();
        if (fromIndex < total) {
            pageContent = list.subList(fromIndex, toIndex).stream()
                    .map(DepartmentDto::new)
                    .collect(Collectors.toList());
        }

        return new PageImpl<>(pageContent, PageRequest.of(pageIndex, pageSize), total);
    }

    @Override
    public List<DepartmentDto> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .filter(d -> d.getIsDeleted() == null || !d.getIsDeleted())
                .map(DepartmentDto::new)
                .collect(Collectors.toList());
    }

    @Override
    public DepartmentDto getById(UUID id) {
        Department entity = departmentRepository.findById(id)
                .filter(d -> d.getIsDeleted() == null || !d.getIsDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng ban với ID: " + id));
        return new DepartmentDto(entity);
    }

    @Override
    public DepartmentDto saveDepartment(DepartmentDto dto) {
        Department entity;
        if (dto.getId() != null) {
            entity = departmentRepository.findById(dto.getId())
                    .orElseThrow(
                            () -> new ResourceNotFoundException("Không tìm thấy phòng ban với ID: " + dto.getId()));
        } else {
            entity = new Department();
            if (dto.getCode() == null || dto.getCode().trim().isEmpty()) {
                entity.setCode(generateCode());
            } else {
                entity.setCode(dto.getCode().trim().toUpperCase());
            }
        }

        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setIsDeleted(false);

        Department saved = departmentRepository.save(entity);
        return new DepartmentDto(saved);
    }

    private void checkDepartmentConstraints(UUID id) {
        if (positionRepository.existsActivePositionsByDepartmentId(id)) {
            throw new IllegalArgumentException("Không thể xóa vì đang có vị trí/chức danh trực thuộc phòng ban này.");
        }
        if (staffRepository.existsActiveStaffByDepartmentId(id)) {
            throw new IllegalArgumentException("Không thể xóa vì đang có nhân viên đang trực thuộc phòng ban này.");
        }
    }

    @Override
    public void deleteDepartment(UUID id) {
        Department entity = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng ban với ID: " + id));
        checkDepartmentConstraints(id);
        entity.setIsDeleted(true);
        departmentRepository.save(entity);
    }

    @Override
    public void deleteMultiple(List<UUID> ids) {
        if (ids != null) {
            for (UUID id : ids) {
                checkDepartmentConstraints(id);
            }
            for (UUID id : ids) {
                departmentRepository.findById(id).ifPresent(entity -> {
                    entity.setIsDeleted(true);
                    departmentRepository.save(entity);
                });
            }
        }
    }

    @Override
    public boolean isValidCode(DepartmentDto dto) {
        if (dto == null || dto.getCode() == null || dto.getCode().trim().isEmpty()) {
            return false;
        }
        String code = dto.getCode().trim().toUpperCase();
        Optional<Department> opt = departmentRepository.findByCode(code);
        if (opt.isPresent()) {
            if (dto.getId() == null) {
                return false;
            }
            return opt.get().getId().equals(dto.getId());
        }
        return true;
    }

    @Override
    public String generateCode() {
        List<Department> all = departmentRepository.findAll();
        String prefix = "PB";
        int maxNum = 0;
        for (Department d : all) {
            String c = d.getCode();
            if (c != null && c.startsWith(prefix)) {
                try {
                    int num = Integer.parseInt(c.substring(prefix.length()));
                    if (num > maxNum) {
                        maxNum = num;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return prefix + String.format("%02d", maxNum + 1);
    }
}
