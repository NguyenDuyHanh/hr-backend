package com.tlu.hrm.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BankTransferQrRequest {

    @NotBlank(message = "Mã ngân hàng là bắt buộc")
    private String bankCode;

    @NotBlank(message = "Số tài khoản là bắt buộc")
    @Pattern(regexp = "\\d{6,20}", message = "Số tài khoản không hợp lệ")
    private String accountNumber;

    private String accountHolderName;

    @Positive(message = "Số tiền phải là số dương")
    private Long amount;                     // nullable — tùy chọn

    @Size(max = 255, message = "Nội dung tối đa 255 ký tự")
    private String transferContent;          // nullable — tùy chọn
}
