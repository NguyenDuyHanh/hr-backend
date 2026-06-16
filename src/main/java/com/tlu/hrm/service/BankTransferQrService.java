package com.tlu.hrm.service;

import com.tlu.hrm.dto.request.BankTransferQrRequest;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

@Service
public class BankTransferQrService {

    private static final int QR_IMAGE_SIZE_PX = 300;
    // NAPAS GUID according to VietQR standard
    private static final String NAPAS_GUID = "A000000727";
    // Service code for bank account transfer
    private static final String SERVICE_CODE_BY_ACCOUNT = "QRIBFTTA";

    /**
     * Generate bank transfer QR code as base64 PNG
     */
    public String generateQrCodeAsBase64(BankTransferQrRequest request) {
        String vietQrPayload = buildVietQrPayload(request);
        return encodePayloadToBase64PngQrCode(vietQrPayload);
    }

    /**
     * Build VietQR payload according to EMVCo standard (TLV format)
     */
    private String buildVietQrPayload(BankTransferQrRequest request) {
        StringBuilder sb = new StringBuilder();

        // Tag 00: Payload Format Indicator
        sb.append(buildTLV("00", "01"));

        // Tag 01: Point of Initiation Method (12 = Dynamic QR)
        sb.append(buildTLV("01", "12"));

        // Tag 38: Merchant Account Information — NAPAS
        // Sub-tag 00: GUID NAPAS
        String subTag00 = buildTLV("00", NAPAS_GUID);
        // Sub-tag 01: Beneficiary Information (nested TLV)
        String beneficiaryBin = buildTLV("00", request.getBankCode());      // Bank BIN
        String beneficiaryAcc = buildTLV("01", request.getAccountNumber()); // Account number
        String subTag01 = buildTLV("01", beneficiaryBin + beneficiaryAcc);

        // Sub-tag 02: Service Code
        String subTag02 = buildTLV("02", SERVICE_CODE_BY_ACCOUNT);

        sb.append(buildTLV("38", subTag00 + subTag01 + subTag02));

        // Tag 52: Merchant Category Code
        sb.append(buildTLV("52", "0000"));

        // Tag 53: Transaction Currency (704 = VND)
        sb.append(buildTLV("53", "704"));

        // Tag 54: Transaction Amount (optional)
        if (request.getAmount() != null && request.getAmount() > 0) {
            String amountStr = String.valueOf(request.getAmount().longValue());
            sb.append(buildTLV("54", amountStr));
        }

        // Tag 58: Country Code
        sb.append(buildTLV("58", "VN"));

        // Tag 62: Additional Data Field Template
        if (request.getTransferContent() != null && !request.getTransferContent().isEmpty()) {
            String content = normalizeTransferContent(request.getTransferContent());
            // Sub-tag 08: Purpose of Transaction
            String subTag08 = buildTLV("08", content);
            sb.append(buildTLV("62", subTag08));
        }

        // Tag 63: CRC — placeholder "6304" + CRC16 CCITT-FALSE
        sb.append("6304");
        String crc = calculateCRC16(sb.toString());
        sb.append(crc);

        return sb.toString();
    }

    /**
     * Build a TLV (Tag-Length-Value)
     * Format: Tag (2 chars) + Length (2 chars, zero-padded) + Value
     */
    private String buildTLV(String tag, String value) {
        return tag + String.format("%02d", value.length()) + value;
    }

    /**
     * Calculate CRC16 CCITT-FALSE (polynomial 0x1021, initial 0xFFFF)
     * According to EMVCo QR Code standard
     */
    private String calculateCRC16(String payload) {
        int crc = 0xFFFF;
        int polynomial = 0x1021;

        byte[] bytes = payload.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        for (byte b : bytes) {
            crc ^= ((b & 0xFF) << 8);
            for (int i = 0; i < 8; i++) {
                if ((crc & 0x8000) != 0) {
                    crc = ((crc << 1) ^ polynomial) & 0xFFFF;
                } else {
                    crc = (crc << 1) & 0xFFFF;
                }
            }
        }
        return String.format("%04X", crc);
    }

    /**
     * Strip accents, keep only letters, numbers, spaces, uppercase
     */
    private String normalizeTransferContent(String content) {
        return java.text.Normalizer
                .normalize(content, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase()
                .replaceAll("[^A-Z0-9 ]", "")
                .trim();
    }

    /**
     * Convert payload text into Base64 PNG image
     */
    private String encodePayloadToBase64PngQrCode(String payload) {
        try {
            BitMatrix matrix = new QRCodeWriter()
                    .encode(payload, BarcodeFormat.QR_CODE, QR_IMAGE_SIZE_PX, QR_IMAGE_SIZE_PX);
            BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "PNG", baos);
            return "data:image/png;base64," +
                    Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception ex) {
            throw new RuntimeException("Không thể tạo mã QR: " + ex.getMessage(), ex);
        }
    }
}
