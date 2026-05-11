package com.tlu.hrm.controller;

import com.tlu.hrm.dto.request.StaffDto;
import com.tlu.hrm.service.StaffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/staffs")
@CrossOrigin(origins = "*")
public class StaffController {

    @Autowired
    private StaffService staffService;

    @GetMapping
    public List<StaffDto> getAllStaffs() {
        return staffService.getAllStaffs();
    }

    @GetMapping("/generate-staff-code")
    public ResponseEntity<String> generateStaffCode() {
        return ResponseEntity.ok(staffService.generateStaffCode());
    }

    @PostMapping
    public StaffDto createStaff(@RequestBody StaffDto staffDto) {
        return staffService.saveStaff(staffDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StaffDto> getStaffById(@PathVariable UUID id) {
        return staffService.getStaffById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<StaffDto> updateStaff(@PathVariable UUID id, @RequestBody StaffDto staffDto) {
        if (!staffService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        staffDto.setId(id);
        return ResponseEntity.ok(staffService.saveStaff(staffDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStaff(@PathVariable UUID id) {
        if (staffService.existsById(id)) {
            staffService.deleteStaff(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
