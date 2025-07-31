package com.cywedding.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.cywedding.config.UploadLimitConfig;
import com.cywedding.dto.Image;
import com.cywedding.dto.QRUser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
public class CloudinaryService {
    private static final Logger logger = LoggerFactory.getLogger(CloudinaryService.class);

    private final Integer MAX_RETRIES = 3;
    private final Long INITIAL_DELAY = 1000L; // 1 second
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd HHmmss");

    @Autowired
    private QRUserService userService;

    @Autowired
    private ImageService imageService;

    @Autowired
    private UploadLimitConfig limitConfig;

    private final Cloudinary cloudinary;
    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }
    
    @SuppressWarnings("unchecked")
    public String uploadCloudinaryImage(byte[] fileBytes, Map<String, Object> options) throws IOException {
        Map<String, Object> uploadResult = cloudinary.uploader().upload(fileBytes, options);
        
        return (String) uploadResult.get("secure_url");
    }

    @Async
    @SuppressWarnings("unchecked")
    public void asyncUploadImage(String domain, String code, String paramName, byte[] fileBytes) throws IOException {
        String extension = paramName.substring(paramName.lastIndexOf("."));
        String timeStamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        String fileName = domain + "-" + code + "_" + timeStamp + extension;

        String folderName = domain + "/" + LocalDate.now().toString();
        String uploadName = code + "_" + timeStamp + extension;

        long delay = INITIAL_DELAY;
        Integer attempt = 0;
        try {
            logger.info("[UPLOAD TRY] code={}", code);

            QRUser user = userService.fetchQRUser(domain, code);
            Map<String, Object> options = ObjectUtils.asMap(
                "folder", folderName,
                "public_id", uploadName,
                "overwrite", true,
                "use_filename", true,
                "unique_filename", true
            );
            while (attempt < MAX_RETRIES) {
                try {
                    String url = uploadCloudinaryImage(fileBytes, options);

                    Image image = new Image();
                    image.setGroupId(user.getCustomGroupId());
                    image.setGroupName(user.getGroupName());
                    image.setQrCode(code);
                    image.setFileName(fileName);
                    image.setImageUrl(url);
                    imageService.uploadImage(image);

                    logger.info("[UPLOAD SUCCESS] url={}", url);
                    break;
                } catch (IOException ex) {
                    logger.warn("[UPLOAD RETRY] attempt {}/{} error={}", attempt, MAX_RETRIES, ex.getMessage());

                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();

                        logger.warn("[UPLOAD INTERRUPTED] attempt {}/{}", attempt, MAX_RETRIES);
                        break;
                    }
                    delay *= 2;
                }
                attempt++;
            }
        } finally {
            limitConfig.release();
        }
    }
}
