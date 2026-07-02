package com.tlu.hrm.controller;

import com.tlu.hrm.dto.request.CandidateDto;
import com.tlu.hrm.dto.response.ApiResponse;
import com.tlu.hrm.dto.search.SearchCandidateDto;
import com.tlu.hrm.enums.CandidateStatus;
import com.tlu.hrm.service.CandidateService;
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
@RequestMapping("/api/candidates")
@CrossOrigin(origins = "*")
@PreAuthorize("hasAnyAuthority('" + ROLE_ADMIN + "', '" + HR_MANAGER + "', '" + HR_RECRUITMENT + "')")
public class CandidateController {

    @Autowired
    private CandidateService candidateService;

    @PostMapping("/paging")
    public ResponseEntity<ApiResponse<Page<CandidateDto>>> pagingCandidates(
            @RequestBody(required = false) SearchCandidateDto searchDto) {
        Page<CandidateDto> result = candidateService.pagingCandidates(searchDto);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách ứng viên thành công", result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CandidateDto>> getById(@PathVariable UUID id) {
        CandidateDto dto = candidateService.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin ứng viên thành công", dto));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CandidateDto>> createCandidate(@RequestBody CandidateDto dto) {
        if (!candidateService.isValidCode(dto)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Mã ứng viên đã tồn tại", HttpStatus.BAD_REQUEST));
        }
        CandidateDto result = candidateService.saveCandidate(dto);
        return ResponseEntity.ok(ApiResponse.success("Tạo ứng viên thành công", result));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CandidateDto>> updateCandidate(@PathVariable UUID id,
            @RequestBody CandidateDto dto) {
        dto.setId(id);
        if (!candidateService.isValidCode(dto)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Mã ứng viên đã tồn tại", HttpStatus.BAD_REQUEST));
        }
        CandidateDto result = candidateService.saveCandidate(dto);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật ứng viên thành công", result));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCandidate(@PathVariable UUID id) {
        candidateService.deleteCandidate(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa ứng viên thành công", null));
    }

    @PostMapping("/delete-multiple")
    public ResponseEntity<ApiResponse<Void>> deleteMultiple(@RequestBody List<UUID> ids) {
        candidateService.deleteMultiple(ids);
        return ResponseEntity.ok(ApiResponse.success("Xóa danh sách ứng viên thành công", null));
    }

    @GetMapping("/generate-code")
    public ResponseEntity<ApiResponse<String>> generateCode() {
        String code = candidateService.generateCode();
        return ResponseEntity.ok(ApiResponse.success("Tạo mã ứng viên thành công", code));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Boolean>> updateStatus(
            @PathVariable UUID id,
            @RequestParam CandidateStatus status,
            @RequestParam(required = false) String refusalReason) {
        Boolean result = candidateService.updateStatus(id, status, refusalReason);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái ứng viên thành công", result));
    }
}
