package com.tlu.hrm.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Generic Excel Utility class for the HRM system.
 * Handles spreadsheet creation, formatting, borders, font setups,
 * and column auto-sizing with custom Vietnamese padding.
 */
public class ExcelUtil {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Functional interface representing the writer that populates a single row with data.
     *
     * @param <T> DTO or Entity model type.
     */
    @FunctionalInterface
    public interface RowWriter<T> {
        void write(T item, Row row, CellStyle defaultStyle, CellStyle centerStyle, int rowIndex);
    }

    /**
     * Exports a list of objects to an Excel spreadsheet byte array.
     *
     * @param sheetName name of the worksheet
     * @param headers   list of column header texts
     * @param dataList  the records to write
     * @param writer    functional writer implementation mapping item fields to cells
     * @param <T>       the record model type
     * @return byte[] representation of the generated Excel workbook
     */
    public static <T> byte[] exportToExcel(
            String sheetName,
            List<String> headers,
            List<T> dataList,
            RowWriter<T> writer
    ) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet(sheetName);

            // 1. Generate Styles
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle centerStyle = createCenterDataStyle(workbook);

            // 2. Write Header Row
            Row headerRow = sheet.createRow(0);
            headerRow.setHeightInPoints(28);
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers.get(i));
                cell.setCellStyle(headerStyle);
            }

            // 3. Write Data Rows
            for (int i = 0; i < dataList.size(); i++) {
                Row row = sheet.createRow(i + 1);
                // row index starts at 1, matching the visually friendly sequence number (STT)
                writer.write(dataList.get(i), row, dataStyle, centerStyle, i + 1);
            }

            // 4. Auto-size Columns dynamically
            for (int i = 0; i < headers.size(); i++) {
                sheet.autoSizeColumn(i);
                int currentWidth = sheet.getColumnWidth(i);
                // Add padding since Apache POI auto-size struggles slightly with Vietnamese accented characters
                sheet.setColumnWidth(i, Math.min(currentWidth + 512, 256 * 50)); // Max cap at ~50 characters
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Lỗi tạo file Excel: " + e.getMessage(), e);
        }
    }

    /**
     * Helper to write a value into a specific column index of a Row.
     * Automatically formats LocalDate, Numbers, and Booleans.
     *
     * @param row      current sheet Row
     * @param colIndex 0-indexed column
     * @param value    object value to set
     * @param style    style to apply to the cell
     */
    public static void writeCell(Row row, int colIndex, Object value, CellStyle style) {
        Cell cell = row.createCell(colIndex);
        cell.setCellStyle(style);
        if (value == null) {
            cell.setCellValue("");
        } else if (value instanceof Number num) {
            cell.setCellValue(num.doubleValue());
        } else if (value instanceof Boolean bool) {
            cell.setCellValue(bool ? "Có" : "Không");
        } else if (value instanceof LocalDate localDate) {
            cell.setCellValue(localDate.format(DATE_FORMATTER));
        } else {
            cell.setCellValue(value.toString());
        }
    }

    /**
     * Header style: Bold Times New Roman, centered alignment, thin borders, with MUI Primary Main (#1976D2) background and white text.
     */
    public static CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontName("Times New Roman");
        font.setFontHeightInPoints((short) 11);
        font.setColor(IndexedColors.WHITE.getIndex()); // White text
        style.setFont(font);

        // Set background color to MUI Primary Main (#1976D2)
        if (style instanceof org.apache.poi.xssf.usermodel.XSSFCellStyle xssfStyle) {
            org.apache.poi.xssf.usermodel.XSSFColor primaryColor = 
                new org.apache.poi.xssf.usermodel.XSSFColor(new java.awt.Color(25, 118, 210), null);
            xssfStyle.setFillForegroundColor(primaryColor);
        } else {
            style.setFillForegroundColor(IndexedColors.CORNFLOWER_BLUE.getIndex()); // Fallback
        }
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        applyBorders(style);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    /**
     * Data style: Standard Times New Roman, vertical center alignment, thin borders.
     */
    public static CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontName("Times New Roman");
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        applyBorders(style);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    /**
     * Center data style: Standard Times New Roman, centered alignment, vertical center alignment, thin borders.
     */
    public static CellStyle createCenterDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontName("Times New Roman");
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        applyBorders(style);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private static void applyBorders(CellStyle style) {
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }
}
