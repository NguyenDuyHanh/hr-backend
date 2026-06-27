package com.tlu.hrm.service.impl;

import com.tlu.hrm.dto.request.ShiftWorkDto;
import com.tlu.hrm.model.ShiftWork;
import com.tlu.hrm.repository.ShiftWorkRepository;
import com.tlu.hrm.service.ShiftWorkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ShiftWorkServiceImpl implements ShiftWorkService {

    @Autowired
    private ShiftWorkRepository shiftWorkRepository;

    @PostConstruct
    public void init() {
        initDefaultShifts();
    }

    @Override
    public Page<ShiftWorkDto> getPage(int pageIndex, int pageSize, String keyword) {
        List<ShiftWork> filteredList = shiftWorkRepository.findAll().stream()
                .filter(shift -> shift.getIsDeleted() == null || !shift.getIsDeleted())
                .filter(shift -> {
                    if (keyword != null && !keyword.isEmpty()) {
                        String kw = keyword.toLowerCase();
                        return (shift.getName() != null && shift.getName().toLowerCase().contains(kw))
                                || (shift.getCode() != null && shift.getCode().toLowerCase().contains(kw));
                    }
                    return true;
                })
                .collect(Collectors.toList());

        int total = filteredList.size();
        int pageNum = pageIndex >= 1 ? pageIndex - 1 : 0;
        int size = pageSize > 0 ? pageSize : 10;

        int fromIndex = pageNum * size;
        int toIndex = Math.min(fromIndex + size, total);

        List<ShiftWorkDto> pageContent = new java.util.ArrayList<>();
        if (fromIndex < total) {
            pageContent = filteredList.subList(fromIndex, toIndex).stream()
                    .map(ShiftWorkDto::new)
                    .collect(Collectors.toList());
        }

        return new PageImpl<>(pageContent, PageRequest.of(pageNum, size), total);
    }

    @Override
    public List<ShiftWorkDto> getAll() {
        return shiftWorkRepository.findAll().stream()
                .filter(shift -> shift.getIsDeleted() == null || !shift.getIsDeleted())
                .map(ShiftWorkDto::new)
                .collect(Collectors.toList());
    }

    @Override
    public ShiftWorkDto saveOrUpdate(ShiftWorkDto dto) {
        ShiftWork entity;
        if (dto.getId() != null) {
            entity = shiftWorkRepository.findById(dto.getId()).orElse(new ShiftWork());
        } else {
            entity = new ShiftWork();
        }

        entity.setCode(dto.getCode());
        entity.setName(dto.getName());
        entity.setStartTime(dto.getStartTime());
        entity.setEndTime(dto.getEndTime());
        entity.setWorkRatio(dto.getWorkRatio());
        entity.setDescription(dto.getDescription());

        ShiftWork saved = shiftWorkRepository.save(entity);
        return new ShiftWorkDto(saved);
    }

    @Override
    public ShiftWorkDto getById(UUID id) {
        return shiftWorkRepository.findById(id)
                .filter(shift -> shift.getIsDeleted() == null || !shift.getIsDeleted())
                .map(ShiftWorkDto::new)
                .orElse(null);
    }

    @Override
    public boolean delete(UUID id) {
        Optional<ShiftWork> optional = shiftWorkRepository.findById(id);
        if (optional.isPresent()) {
            ShiftWork entity = optional.get();
            entity.setIsDeleted(true);
            shiftWorkRepository.save(entity);
            return true;
        }
        return false;
    }

    @Override
    public void initDefaultShifts() {
        if (shiftWorkRepository.count() == 0) {
            // 1. Ca Sáng
            ShiftWork morning = new ShiftWork();
            morning.setCode("CA_SANG");
            morning.setName("Ca Sáng");
            morning.setStartTime(LocalTime.of(8, 0));
            morning.setEndTime(LocalTime.of(12, 0));
            morning.setWorkRatio(0.5);
            morning.setDescription("Ca làm việc buổi sáng");
            shiftWorkRepository.save(morning);

            // 2. Ca Chiều
            ShiftWork afternoon = new ShiftWork();
            afternoon.setCode("CA_CHIEU");
            afternoon.setName("Ca Chiều");
            afternoon.setStartTime(LocalTime.of(13, 30));
            afternoon.setEndTime(LocalTime.of(17, 30));
            afternoon.setWorkRatio(0.5);
            afternoon.setDescription("Ca làm việc buổi chiều");
            shiftWorkRepository.save(afternoon);

            // 3. Ca Cả Ngày
            ShiftWork fullDay = new ShiftWork();
            fullDay.setCode("CA_CA_NGAY");
            fullDay.setName("Ca Cả Ngày (Hành chính)");
            fullDay.setStartTime(LocalTime.of(8, 0));
            fullDay.setEndTime(LocalTime.of(17, 30));
            fullDay.setWorkRatio(1.0);
            fullDay.setDescription("Ca làm việc cả ngày hành chính, nghỉ trưa 12h00 - 13h30");
            shiftWorkRepository.save(fullDay);

            // 4. Ca Tăng Ca
            ShiftWork overtime = new ShiftWork();
            overtime.setCode("CA_OT");
            overtime.setName("Ca Tăng Ca (Overtime)");
            overtime.setStartTime(LocalTime.of(18, 0));
            overtime.setEndTime(LocalTime.of(20, 0));
            overtime.setWorkRatio(0.0);
            overtime.setDescription("Ca làm việc tăng ca tối");
            shiftWorkRepository.save(overtime);
        }
    }
}
