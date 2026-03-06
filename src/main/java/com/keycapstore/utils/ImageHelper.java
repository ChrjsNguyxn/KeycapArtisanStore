package com.keycapstore.utils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class ImageHelper {

    // Đường dẫn lưu ảnh (Tạo thư mục images trong thư mục chạy của dự án)
    private static final String IMAGE_DIR = "product_images/";

    static {
        File dir = new File(IMAGE_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * Lưu ảnh từ file gốc vào thư mục dự án, có resize để tối ưu
     * 
     * @param originalFile File ảnh gốc
     * @return Tên file mới đã lưu
     */
    public static String saveImage(File originalFile) throws IOException {
        String ext = getFileExtension(originalFile);
        String newName = UUID.randomUUID().toString() + "." + ext;
        File destFile = new File(IMAGE_DIR + newName);

        // Đọc ảnh gốc
        BufferedImage originalImage = ImageIO.read(originalFile);

        // Resize thông minh: Max width/height = 800px (Đủ nét cho UI, nhẹ cho DB)
        int targetWidth = 800;
        int targetHeight = 800;

        // Tính toán tỉ lệ để không bị méo ảnh
        double ratio = Math.min((double) targetWidth / originalImage.getWidth(),
                (double) targetHeight / originalImage.getHeight());

        // Nếu ảnh nhỏ hơn target thì giữ nguyên, không phóng to (tránh vỡ)
        if (ratio >= 1) {
            Files.copy(originalFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } else {
            int w = (int) (originalImage.getWidth() * ratio);
            int h = (int) (originalImage.getHeight() * ratio);

            BufferedImage resized = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = resized.createGraphics();

            // CẤU HÌNH CHẤT LƯỢNG CAO NHẤT
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.drawImage(originalImage, 0, 0, w, h, null);
            g2.dispose();

            ImageIO.write(resized, ext, destFile);
        }

        return newName;
    }

    /**
     * Tạo ImageIcon từ tên file để hiển thị lên UI (Thumbnail)
     */
    public static ImageIcon loadResizedIcon(String imageName, int w, int h) {
        if (imageName == null || imageName.isEmpty())
            return null;
        try {
            File f = new File(IMAGE_DIR + imageName);
            if (!f.exists())
                return null;

            ImageIcon icon = new ImageIcon(f.getAbsolutePath());
            Image img = icon.getImage();
            // Scale smooth để hiển thị thumbnail đẹp
            Image newImg = img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
            return new ImageIcon(newImg);
        } catch (Exception e) {
            return null;
        }
    }

    private static String getFileExtension(File file) {
        String name = file.getName();
        int lastIndexOf = name.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return "jpg"; // Default
        }
        return name.substring(lastIndexOf + 1);
    }
}
