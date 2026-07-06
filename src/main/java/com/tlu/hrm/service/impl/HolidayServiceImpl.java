package com.tlu.hrm.service.impl;

import com.tlu.hrm.dto.request.HolidayDto;
import com.tlu.hrm.model.Holiday;
import com.tlu.hrm.repository.HolidayRepository;
import com.tlu.hrm.service.HolidayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class HolidayServiceImpl implements HolidayService {

    @Autowired
    private HolidayRepository holidayRepository;

    @Override
    public Page<HolidayDto> getPage(com.tlu.hrm.dto.search.HolidaySearchRequest request) {
        int pageIndex = request != null ? request.getPageIndex() : 1;
        int pageSize = request != null ? request.getPageSize() : 10;
        String keyword = request != null ? request.getKeyword() : "";
        Integer year = request != null ? request.getYear() : null;
        LocalDate fromDate = request != null ? request.getFromDate() : null;
        LocalDate toDate = request != null ? request.getToDate() : null;

        List<Holiday> filteredList = holidayRepository.findAllActive().stream()
                .filter(h -> {
                    if (keyword != null && !keyword.trim().isEmpty()) {
                        String kw = keyword.trim().toLowerCase();
                        boolean matches = (h.getName() != null && h.getName().toLowerCase().contains(kw))
                                || (h.getCode() != null && h.getCode().toLowerCase().contains(kw));
                        if (!matches) return false;
                    }
                    if (year != null) {
                        if (!year.equals(h.getYear())) return false;
                    }
                    if (fromDate != null) {
                        if (h.getStartDate().isBefore(fromDate)) return false;
                    }
                    if (toDate != null) {
                        if (h.getEndDate().isAfter(toDate)) return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());

        int total = filteredList.size();
        int pageNum = pageIndex >= 1 ? pageIndex - 1 : 0;
        int size = pageSize > 0 ? pageSize : 10;

        int fromIndex = pageNum * size;
        int toIndex = Math.min(fromIndex + size, total);

        List<HolidayDto> pageContent = new java.util.ArrayList<>();
        if (fromIndex < total) {
            pageContent = filteredList.subList(fromIndex, toIndex).stream()
                    .map(HolidayDto::new)
                    .collect(Collectors.toList());
        }

        return new PageImpl<>(pageContent, PageRequest.of(pageNum, size), total);
    }

    @Override
    public List<HolidayDto> getAll() {
        return holidayRepository.findAllActive().stream()
                .map(HolidayDto::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<HolidayDto> getByYear(Integer year) {
        return holidayRepository.findByYear(year).stream()
                .map(HolidayDto::new)
                .collect(Collectors.toList());
    }

    @Override
    public HolidayDto saveOrUpdate(HolidayDto dto) {
        // 1. Kiểm tra trùng mã ngày lễ
        if (dto.getCode() == null || dto.getCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã ngày lễ không được để trống");
        }
        Optional<Holiday> duplicateCode = holidayRepository.findActiveByCode(dto.getCode().trim(), dto.getId());
        if (duplicateCode.isPresent()) {
            throw new IllegalArgumentException("Mã ngày lễ '" + dto.getCode() + "' đã tồn tại trong hệ thống");
        }

        // 2. Kiểm tra khoảng ngày hợp lệ
        if (dto.getStartDate() == null || dto.getEndDate() == null) {
            throw new IllegalArgumentException("Ngày bắt đầu và ngày kết thúc không được để trống");
        }
        if (dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new IllegalArgumentException("Ngày kết thúc phải lớn hơn hoặc bằng ngày bắt đầu");
        }

        // 3. Kiểm tra trùng lấn khoảng ngày nghỉ với ngày lễ khác
        List<Holiday> overlapping = holidayRepository.findOverlappingHolidays(dto.getStartDate(), dto.getEndDate(), dto.getId());
        if (!overlapping.isEmpty()) {
            Holiday first = overlapping.get(0);
            throw new IllegalArgumentException("Thời gian nghỉ lễ bị trùng lặp với ngày lễ '" + first.getName() 
                    + "' (" + first.getStartDate() + " đến " + first.getEndDate() + ")");
        }

        Holiday entity;
        if (dto.getId() != null) {
            entity = holidayRepository.findById(dto.getId()).orElse(new Holiday());
        } else {
            entity = new Holiday();
        }

        entity.setCode(dto.getCode().trim());
        entity.setName(dto.getName());
        entity.setStartDate(dto.getStartDate());
        entity.setEndDate(dto.getEndDate());
        entity.setYear(dto.getYear());
        entity.setDescription(dto.getDescription());

        // Tự tính totalDays từ startDate và endDate
        int days = (int) ChronoUnit.DAYS.between(dto.getStartDate(), dto.getEndDate()) + 1;
        entity.setTotalDays(days);

        Holiday saved = holidayRepository.save(entity);
        return new HolidayDto(saved);
    }

    @Override
    public HolidayDto getById(UUID id) {
        return holidayRepository.findById(id)
                .filter(h -> h.getIsDeleted() == null || !h.getIsDeleted())
                .map(HolidayDto::new)
                .orElse(null);
    }

    @Override
    public boolean delete(UUID id) {
        Optional<Holiday> optional = holidayRepository.findById(id);
        if (optional.isPresent()) {
            Holiday entity = optional.get();
            entity.setIsDeleted(true);
            holidayRepository.save(entity);
            return true;
        }
        return false;
    }

    @Override
    public boolean isHoliday(LocalDate date) {
        if (date == null) {
            return false;
        }
        return !holidayRepository.findHolidaysContainingDate(date).isEmpty();
    }

    @Override
    public List<HolidayDto> getHolidaysInRange(LocalDate start, LocalDate end) {
        return holidayRepository.findHolidaysInRange(start, end).stream()
                .map(HolidayDto::new)
                .collect(Collectors.toList());
    }
}
