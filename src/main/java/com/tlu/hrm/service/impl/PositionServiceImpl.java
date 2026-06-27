package com.tlu.hrm.service.impl;

import com.tlu.hrm.dto.request.PositionDto;
import com.tlu.hrm.dto.search.SearchDto;
import com.tlu.hrm.exception.ResourceNotFoundException;
import com.tlu.hrm.model.Department;
import com.tlu.hrm.model.Position;
import com.tlu.hrm.repository.DepartmentRepository;
import com.tlu.hrm.repository.PositionRepository;
import com.tlu.hrm.repository.StaffRepository;
import com.tlu.hrm.service.PositionService;
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
public class PositionServiceImpl implements PositionService {

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private StaffRepository staffRepository;

    @Override
    public Page<PositionDto> pagingPositions(SearchDto searchDto) {
        List<Position> list = positionRepository.findAll().stream()
                .filter(p -> p.getIsDeleted() == null || !p.getIsDeleted())
                .filter(p -> {
                    if (searchDto != null) {
                        if (searchDto.getKeyword() != null && !searchDto.getKeyword().isEmpty()) {
                            String kw = searchDto.getKeyword().toLowerCase();
                            boolean matchesKeyword = (p.getName() != null && p.getName().toLowerCase().contains(kw))
                                    || (p.getCode() != null && p.getCode().toLowerCase().contains(kw))
                                    || (p.getDescription() != null && p.getDescription().toLowerCase().contains(kw));
                            if (!matchesKeyword)
                                return false;
                        }
                        if (searchDto.getDepartmentId() != null) {
                            if (p.getDepartment() == null
                                    || !searchDto.getDepartmentId().equals(p.getDepartment().getId())) {
                                return false;
                            }
                        }
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

        List<PositionDto> pageContent = new ArrayList<>();
        if (fromIndex < total) {
            pageContent = list.subList(fromIndex, toIndex).stream()
                    .map(PositionDto::new)
                    .collect(Collectors.toList());
        }

        return new PageImpl<>(pageContent, PageRequest.of(pageIndex, pageSize), total);
    }

    @Override
    public List<PositionDto> getAllPositions() {
        return positionRepository.findAll().stream()
                .filter(p -> p.getIsDeleted() == null || !p.getIsDeleted())
                .map(PositionDto::new)
                .collect(Collectors.toList());
    }

    @Override
    public PositionDto getById(UUID id) {
        Position entity = positionRepository.findById(id)
                .filter(p -> p.getIsDeleted() == null || !p.getIsDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chức danh với ID: " + id));
        return new PositionDto(entity);
    }

    @Override
    public PositionDto savePosition(PositionDto dto) {
        Position entity;
        if (dto.getId() != null) {
            entity = positionRepository.findById(dto.getId())
                    .orElseThrow(
                            () -> new ResourceNotFoundException("Không tìm thấy chức danh với ID: " + dto.getId()));
        } else {
            entity = new Position();
            if (dto.getCode() == null || dto.getCode().trim().isEmpty()) {
                entity.setCode(generateCode());
            } else {
                entity.setCode(dto.getCode().trim().toUpperCase());
            }
        }

        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setIsDeleted(false);

        if (dto.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Không tìm thấy phòng ban với ID: " + dto.getDepartmentId()));
            entity.setDepartment(dept);
        } else {
            entity.setDepartment(null);
        }

        Position saved = positionRepository.save(entity);
        return new PositionDto(saved);
    }

    private void checkPositionConstraints(UUID id) {
        if (staffRepository.existsActiveStaffByPositionId(id)) {
            throw new IllegalArgumentException("Không thể xóa vì đang có nhân viên đang đảm nhận vị trí này.");
        }
    }

    @Override
    public void deletePosition(UUID id) {
        Position entity = positionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chức danh với ID: " + id));
        checkPositionConstraints(id);
        entity.setIsDeleted(true);
        positionRepository.save(entity);
    }

    @Override
    public void deleteMultiple(List<UUID> ids) {
        if (ids != null) {
            for (UUID id : ids) {
                checkPositionConstraints(id);
            }
            for (UUID id : ids) {
                positionRepository.findById(id).ifPresent(entity -> {
                    entity.setIsDeleted(true);
                    positionRepository.save(entity);
                });
            }
        }
    }

    @Override
    public boolean isValidCode(PositionDto dto) {
        if (dto == null || dto.getCode() == null || dto.getCode().trim().isEmpty()) {
            return false;
        }
        String code = dto.getCode().trim().toUpperCase();
        Optional<Position> opt = positionRepository.findByCode(code);
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
        List<Position> all = positionRepository.findAll();
        String prefix = "VT";
        int maxNum = 0;
        for (Position p : all) {
            String c = p.getCode();
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
