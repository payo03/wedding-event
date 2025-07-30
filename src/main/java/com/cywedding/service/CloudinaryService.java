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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
public class CloudinaryService {
    private static final Logger logger = LoggerFactory.getLogger(CloudinaryService.class);

    private final Integer MAX_RETRIES = 3;
    private final Long INITIAL_DELAY = 1000L; // 1 second

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
    public String uploadCloudinaryImage(byte[] fileBytes) throws IOException {
        Map<String, Object> uploadResult = cloudinary.uploader().upload(fileBytes, ObjectUtils.emptyMap());
        
        return (String) uploadResult.get("secure_url");
    }

    @Async
    public void asyncUploadImage(String domain, String code, String fileName, byte[] fileBytes) throws IOException {
        String extension = fileName.substring(fileName.lastIndexOf("."));
        String timeStamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd HHmmss"));
        String name = domain + "-" + code + "_" + timeStamp + extension;

        long delay = INITIAL_DELAY;
        Integer attempt = 0;
        try {
            logger.info("[UPLOAD TRY] code={}", code);

            QRUser user = userService.fetchQRUser(domain, code);
            while (attempt < MAX_RETRIES) {
                try {
                    String url = uploadCloudinaryImage(fileBytes);

                    Image image = new Image();
                    image.setGroupId(user.getCustomGroupId());
                    image.setGroupName(user.getGroupName());
                    image.setQrCode(code);
                    image.setFileName(name);
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
