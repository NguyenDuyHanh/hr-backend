package com.tlu.hrm.controller;

import com.tlu.hrm.dto.request.BankTransferQrRequest;
import com.tlu.hrm.dto.response.ApiResponse;
import com.tlu.hrm.dto.response.BankTransferQrResponse;
import com.tlu.hrm.service.BankTransferQrService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bank-transfer/qr")
@Validated
@CrossOrigin(origins = "*")
public class BankTransferQrController {

    @Autowired
    private BankTransferQrService bankTransferQrService;

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<BankTransferQrResponse>> generateBankTransferQrCode(
            @Valid @RequestBody BankTransferQrRequest request) {

        String qrImageBase64 = bankTransferQrService.generateQrCodeAsBase64(request);
        BankTransferQrResponse response = new BankTransferQrResponse(qrImageBase64);
        return ResponseEntity.ok(ApiResponse.success("Tạo mã QR chuyển khoản thành công", response));
    }
}
