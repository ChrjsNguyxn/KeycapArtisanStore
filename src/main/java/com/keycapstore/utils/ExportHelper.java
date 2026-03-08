package com.keycapstore.utils;

import java.io.File;
import java.io.FileOutputStream;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.filechooser.FileNameExtensionFilter;

// Import POI (Excel)
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

// Import iText (PDF) - Import cụ thể để tránh xung đột với POI và AWT
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.BaseFont;

public class ExportHelper {

    // 1. HÀM XUẤT EXCEL
    public static void exportTableToExcel(JTable table, String filePath) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Data");

            // --- STYLE CHO HEADER ---
            CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.SEA_GREEN.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Row headerRow = sheet.createRow(0);

            // Lấy tiêu đề cột
            for (int i = 0; i < table.getColumnCount(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(table.getColumnName(i));
                cell.setCellStyle(headerStyle); // Áp dụng style
            }

            // Lấy dữ liệu từng dòng
            for (int i = 0; i < table.getRowCount(); i++) {
                Row row = sheet.createRow(i + 1);
                for (int j = 0; j < table.getColumnCount(); j++) {
                    Cell cell = row.createCell(j);
                    Object value = table.getValueAt(i, j);
                    if (value != null) {
                        // --- XỬ LÝ SỐ THÔNG MINH ---
                        if (value instanceof Number) {
                            cell.setCellValue(((Number) value).doubleValue());
                        } else {
                            String sValue = value.toString();
                            try {
                                // Thử chuyển đổi các chuỗi tiền tệ/số về dạng số
                                String cleanedValue = sValue.replace("₫", "").replace(",", "").trim();
                                double numericValue = Double.parseDouble(cleanedValue);
                                cell.setCellValue(numericValue);
                            } catch (NumberFormatException e) {
                                // Nếu không phải số thì giữ nguyên là String (Tên, Ngày,...)
                                cell.setCellValue(sValue);
                            }
                        }
                    }
                }
            }

            try (FileOutputStream out = new FileOutputStream(new File(filePath))) {
                workbook.write(out);
            }

            JOptionPane.showMessageDialog(null, "✅ Xuất Excel thành công tại:\n" + filePath);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "❌ Lỗi xuất Excel: " + e.getMessage());
        }
    }

    // 2. HÀM XUẤT PDF - In Hóa Đơn
    public static void exportBillToPDF(JTable table, String billId, String totalMoney, String filePath) {
        try {
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            // --- FONT TIẾNG VIỆT (QUAN TRỌNG) ---
            // Yêu cầu: Đặt file font (VD: Arial.ttf) vào thư mục resources/fonts của dự án
            com.itextpdf.text.Font titleFont;
            com.itextpdf.text.Font textFont;
            com.itextpdf.text.Font boldFont;
            try {
                BaseFont bf = BaseFont.createFont("fonts/Arial.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                titleFont = new com.itextpdf.text.Font(bf, 18, com.itextpdf.text.Font.BOLD, BaseColor.BLACK);
                textFont = new com.itextpdf.text.Font(bf, 12, com.itextpdf.text.Font.NORMAL, BaseColor.BLACK);
                boldFont = new com.itextpdf.text.Font(bf, 12, com.itextpdf.text.Font.BOLD, BaseColor.BLACK);
            } catch (Exception e) {
                System.err.println("Không tìm thấy font. Sử dụng font mặc định (có thể lỗi tiếng Việt).");
                titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLACK);
                textFont = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.BLACK);
                boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK);
            }

            // Tiêu đề bill
            Paragraph title = new Paragraph("KEYFORGE ARTISAN STORE - INVOICE", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            document.add(new Paragraph(" "));
            document.add(new Paragraph("Ma Hoa Don: " + billId, textFont));
            document.add(new Paragraph("Ngay Tao: " + java.time.LocalDate.now(), textFont));
            document.add(new Paragraph("--------------------------------------------------"));
            document.add(new Paragraph(" ")); // Dòng trống

            // Tạo bảng PDF
            PdfPTable pdfTable = new PdfPTable(table.getColumnCount());
            pdfTable.setWidthPercentage(100);

            // Thêm Header
            for (int i = 0; i < table.getColumnCount(); i++) {
                PdfPCell cell = new PdfPCell(new Phrase(table.getColumnName(i), boldFont));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                cell.setPadding(5);
                pdfTable.addCell(cell);
            }

            // Thêm Data
            for (int i = 0; i < table.getRowCount(); i++) {
                for (int j = 0; j < table.getColumnCount(); j++) {
                    Object value = table.getValueAt(i, j);
                    String cellValue = (value != null) ? value.toString() : "";

                    PdfPCell cell = new PdfPCell(new Phrase(cellValue, textFont));
                    // Căn lề cho các cột số (giả định cột SL, Giá, Tiền ở cuối)
                    if (j >= table.getColumnCount() - 3) {
                        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    } else {
                        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                    }
                    cell.setPadding(5);
                    pdfTable.addCell(cell);
                }
            }

            document.add(pdfTable);
            document.add(new Paragraph(" "));

            Paragraph totalPara = new Paragraph("TONG TIEN: " + totalMoney + " VND", titleFont);
            totalPara.setAlignment(Element.ALIGN_RIGHT);
            document.add(totalPara);

            document.close();
            JOptionPane.showMessageDialog(null, "✅ Đã in Hóa Đơn PDF tại:\n" + filePath);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "❌ Lỗi in PDF: " + e.getMessage());
        }
    }

    // 3. HÀM CHỌN VỊ TRÍ LƯU FILE (Save As Dialog)
    public static String promptSaveLocation(java.awt.Component parent, String defaultName, String extension,
            String description) {
        javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
        fileChooser.setDialogTitle("Chọn nơi lưu file " + description);

        // FIX: Đặt thư mục mặc định là Desktop/Home để tránh lỗi ShellFolder (This PC)
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        fileChooser.setSelectedFile(new File(defaultName));

        // Bộ lọc file (Ví dụ: chỉ hiện .xlsx hoặc .pdf)
        fileChooser.setFileFilter(new FileNameExtensionFilter(description + " (*." + extension + ")", extension));

        int userSelection = fileChooser.showSaveDialog(parent);

        if (userSelection == javax.swing.JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            // FIX: Lấy đường dẫn tuyệt đối trực tiếp từ file được chọn.
            // Tuyệt đối không dùng getCurrentDirectory() + getName() để tránh lỗi
            // ShellFolder.
            String path = fileToSave.getAbsolutePath();

            // FIX: Chặn lỗi nếu người dùng chọn This PC/Quick Access mà không vào thư mục
            // cụ thể
            if (path.contains("ShellFolder")) {
                JOptionPane.showMessageDialog(parent, "Vui lòng chọn một ổ đĩa hoặc thư mục cụ thể (VD: Desktop, Ổ D)!",
                        "Lỗi vị trí lưu", JOptionPane.WARNING_MESSAGE);
                return null;
            }

            if (!path.toLowerCase().endsWith("." + extension)) {
                path += "." + extension;
            }
            return path;
        }
        return null; // Người dùng bấm Cancel
    }
}