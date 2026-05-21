package com.cy.modules.qms.controller;

import com.cy.modules.qms.entity.QmsAttachment;
import com.cy.modules.qms.service.QmsAttachmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @Description: QMS Attachment Controller
 * @Author: BMad
 * @Date: 2026-02-25
 */
@Slf4j
@Tag(name = "QMS - Tệp đính kèm (Attachment)")
@RestController
@RequestMapping("/qms/attachment")
public class QmsAttachmentController extends JeecgController<QmsAttachment, QmsAttachmentService> {

    @Autowired
    private QmsAttachmentService attachmentService;

    /** Ngưỡng dung lượng ảnh cần nén: 10MB */
    private static final long IMAGE_COMPRESSION_THRESHOLD = 10L * 1024 * 1024;

    /** Các định dạng ảnh hỗ trợ nén */
    private static final Set<String> COMPRESSIBLE_IMAGE_TYPES = Set.of("jpg", "jpeg", "png");

    /**
     * Tải lên tệp đính kèm cho một thực thể QMS.
     * Nếu tệp là ảnh (JPG/PNG) và dung lượng > 10MB (từ thiết bị di động),
     * hệ thống sẽ tự động nén trước khi lưu.
     */
    @PostMapping("/upload")
    @AutoLog(value = "Tải lên tệp đính kèm QMS")
    @Operation(summary = "Tải lên tệp đính kèm cho phiếu kiểm tra")
    public Result<?> upload(@RequestParam("file") MultipartFile file,
                            @RequestParam("entityType") String entityType,
                            @RequestParam("entityId") String entityId) {
        try {
            MultipartFile fileToUpload = file;

            // Requirement 10.4: Nén ảnh nếu dung lượng vượt quá 10MB (từ thiết bị di động)
            if (isCompressibleImage(file) && file.getSize() > IMAGE_COMPRESSION_THRESHOLD) {
                fileToUpload = compressImage(file);
            }

            QmsAttachment attachment = attachmentService.upload(fileToUpload, entityType, entityId);
            return Result.OK("Tải tệp lên thành công!", attachment);
        } catch (Exception e) {
            log.error("Lỗi khi tải tệp đính kèm: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * Lấy danh sách tệp đính kèm theo thực thể.
     */
    @GetMapping("/list")
    @Operation(summary = "Danh sách tệp đính kèm theo thực thể")
    public Result<?> list(@RequestParam("entityType") String entityType,
                          @RequestParam("entityId") String entityId) {
        List<QmsAttachment> attachments = attachmentService.listByEntity(entityType, entityId);
        return Result.OK(attachments);
    }

    /**
     * Xóa tệp đính kèm theo ID.
     */
    @DeleteMapping("/delete")
    @AutoLog(value = "Xóa tệp đính kèm QMS")
    @Operation(summary = "Xóa tệp đính kèm")
    public Result<?> delete(@RequestParam("id") String id) {
        try {
            attachmentService.deleteAttachment(id);
            return Result.OK("Xóa tệp đính kèm thành công!");
        } catch (Exception e) {
            log.error("Lỗi khi xóa tệp đính kèm: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * Kiểm tra xem tệp có phải là ảnh có thể nén không.
     */
    private boolean isCompressibleImage(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            return false;
        }
        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        return COMPRESSIBLE_IMAGE_TYPES.contains(extension);
    }

    /**
     * Nén ảnh sử dụng Java ImageIO.
     * Giảm chất lượng JPEG xuống 0.7 để giảm dung lượng.
     * Nếu ảnh PNG, chuyển sang JPEG trước khi nén.
     *
     * TODO: Cân nhắc sử dụng thư viện xử lý ảnh chuyên dụng (Thumbnailator hoặc imgscalr)
     *       để nén hiệu quả hơn và hỗ trợ resize kích thước ảnh.
     */
    private MultipartFile compressImage(MultipartFile originalFile) {
        try {
            InputStream inputStream = originalFile.getInputStream();
            BufferedImage image = ImageIO.read(inputStream);

            if (image == null) {
                log.warn("Không thể đọc ảnh để nén, sử dụng tệp gốc");
                return originalFile;
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            // Nén dưới dạng JPEG với chất lượng 0.7
            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
            if (!writers.hasNext()) {
                log.warn("Không tìm thấy ImageWriter cho JPEG, sử dụng tệp gốc");
                return originalFile;
            }

            ImageWriter writer = writers.next();
            ImageOutputStream imageOutputStream = ImageIO.createImageOutputStream(outputStream);
            writer.setOutput(imageOutputStream);

            ImageWriteParam params = writer.getDefaultWriteParam();
            params.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            params.setCompressionQuality(0.7f);

            // Nếu ảnh có alpha channel (PNG), chuyển sang RGB
            BufferedImage rgbImage = image;
            if (image.getType() == BufferedImage.TYPE_INT_ARGB ||
                image.getType() == BufferedImage.TYPE_4BYTE_ABGR) {
                rgbImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
                rgbImage.createGraphics().drawImage(image, 0, 0, null);
            }

            writer.write(null, new javax.imageio.IIOImage(rgbImage, null, null), params);
            writer.dispose();
            imageOutputStream.close();

            byte[] compressedBytes = outputStream.toByteArray();
            log.info("Nén ảnh thành công: {} -> {} bytes (giảm {}%)",
                    originalFile.getSize(), compressedBytes.length,
                    Math.round((1.0 - (double) compressedBytes.length / originalFile.getSize()) * 100));

            // Trả về MultipartFile mới với dữ liệu đã nén
            String originalFilename = originalFile.getOriginalFilename();
            String compressedFilename = originalFilename;
            if (originalFilename != null && originalFilename.toLowerCase().endsWith(".png")) {
                compressedFilename = originalFilename.substring(0, originalFilename.lastIndexOf(".")) + ".jpg";
            }

            return new CompressedMultipartFile(compressedBytes, compressedFilename,
                    "image/jpeg", originalFile.getName());

        } catch (IOException e) {
            log.warn("Lỗi khi nén ảnh, sử dụng tệp gốc: {}", e.getMessage());
            return originalFile;
        }
    }

    /**
     * MultipartFile wrapper cho dữ liệu ảnh đã nén.
     */
    private static class CompressedMultipartFile implements MultipartFile {
        private final byte[] content;
        private final String filename;
        private final String contentType;
        private final String name;

        public CompressedMultipartFile(byte[] content, String filename, String contentType, String name) {
            this.content = content;
            this.filename = filename;
            this.contentType = contentType;
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getOriginalFilename() {
            return filename;
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public boolean isEmpty() {
            return content.length == 0;
        }

        @Override
        public long getSize() {
            return content.length;
        }

        @Override
        public byte[] getBytes() {
            return content;
        }

        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(content);
        }

        @Override
        public void transferTo(java.io.File dest) throws IOException, IllegalStateException {
            java.nio.file.Files.write(dest.toPath(), content);
        }
    }
}
