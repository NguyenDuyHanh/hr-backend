package com.tlu.hrm.controller;

import com.tlu.hrm.dto.request.BankDto;
import com.tlu.hrm.dto.response.ApiResponse;
import com.tlu.hrm.dto.search.SearchDto;
import com.tlu.hrm.service.BankService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static com.tlu.hrm.enums.RoleType.Constants.*;

@RestController
@RequestMapping("/api/banks")
@CrossOrigin(origins = "*")
@PreAuthorize("hasAnyAuthority('" + ROLE_ADMIN + "', '" + HR_MANAGER + "', '" + HR_EMPLOYEE + "')")
public class BankController {

    @Autowired
    private BankService bankService;

    @PostMapping("/paging")
    public ResponseEntity<ApiResponse<Page<BankDto>>> pagingBanks(@RequestBody(required = false) SearchDto searchDto) {
        Page<BankDto> result = bankService.pagingBanks(searchDto);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách ngân hàng thành công", result));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BankDto>>> getAllBanks() {
        List<BankDto> result = bankService.getAllBanks();
        return ResponseEntity.ok(ApiResponse.success("Lấy toàn bộ danh sách ngân hàng thành công", result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BankDto>> getById(@PathVariable UUID id) {
        BankDto dto = bankService.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin ngân hàng thành công", dto));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('" + ROLE_ADMIN + "')")
    public ResponseEntity<ApiResponse<BankDto>> createBank(@RequestBody BankDto dto) {
        if (!bankService.isValidCode(dto)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Mã ngân hàng đã tồn tại", HttpStatus.BAD_REQUEST));
        }
        BankDto result = bankService.saveBank(dto);
        return ResponseEntity.ok(ApiResponse.success("Tạo ngân hàng thành công", result));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('" + ROLE_ADMIN + "')")
    public ResponseEntity<ApiResponse<BankDto>> updateBank(@PathVariable UUID id, @RequestBody BankDto dto) {
        dto.setId(id);
        if (!bankService.isValidCode(dto)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Mã ngân hàng đã tồn tại", HttpStatus.BAD_REQUEST));
        }
        BankDto result = bankService.saveBank(dto);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật ngân hàng thành công", result));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('" + ROLE_ADMIN + "')")
    public ResponseEntity<ApiResponse<Void>> deleteBank(@PathVariable UUID id) {
        bankService.deleteBank(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa ngân hàng thành công", null));
    }
}
