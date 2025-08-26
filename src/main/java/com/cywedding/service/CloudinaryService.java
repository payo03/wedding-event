package com.cywedding.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.cywedding.common.DMLType;
import com.cywedding.config.UploadLimitConfig;
import com.cywedding.dto.Image;
import com.cywedding.dto.QRUser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private RestClient restClient;

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
    public void asyncUploadImage(String groupName, String code, String paramName, byte[] fileBytes) throws IOException {
        String extension = paramName.substring(paramName.lastIndexOf("."));
        String timeStamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        String uploadName = code + "_" + timeStamp;
        String fileName = uploadName + extension;

        long delay = INITIAL_DELAY;
        Integer attempt = 0;
        try {
            logger.info("[UPLOAD TRY] code={}", code);

            QRUser user = userService.fetchQRUser(groupName, code);
            Map<String, Object> options = makeUploadOptions(groupName, uploadName);
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

    @Async
    @Transactional
    @SuppressWarnings("unchecked")
    public void asyncDeleteImage(String groupName, String code, String fileName, String imageUrl) {
        try {
            logger.info("[DELETE START] group={}, file={}", groupName, fileName);

            String fileNameOnly = removeExtension(fileName);
            String publicId = groupName + "/" + fileNameOnly;
            String removePublicId = groupName + "/remove";

            // 1. Remove폴더 업로드(Cloudinary)
            URI uri = URI.create(imageUrl);
            Map<String, Object> uploadOptions = makeUploadOptions(removePublicId, fileNameOnly);

            ResponseEntity<byte[]> response = restClient.get().uri(uri).retrieve().toEntity(byte[].class);
            String url = uploadCloudinaryImage(response.getBody(), uploadOptions);
            logger.info("[MOVE SUCCESS] newUrl={}", url);
    
            // 2. 기존 이미지 삭제(Cloudinary)
            Map<String, Object> destroyResult = cloudinary.uploader().destroy(publicId, new HashMap<String, Object>());
            logger.info("[DESTROY RESULT] {}", destroyResult);
        } catch (Exception e) {
            logger.error("[DELETE FAILED] file={}, error={}", fileName, e.getMessage(), e);
        } finally {
            // 3. DB 반영
            imageService.deleteImage(groupName, code, fileName);
        }
    }

    @Async
    @Transactional
    public void asyncBannedUser(String groupName, String code) {
        QRUser bannedUser = userService.fetchQRUser(groupName, code);
        try {
            List<Image> userImageList = imageService.selectImageList(bannedUser, "P").stream()
                .filter(image -> image.getQrCode().equals(bannedUser.getQrCode()))
                .collect(Collectors.toList());
                
            for(Image userImage : userImageList) {
                String qrCode = userImage.getQrCode();
                String fileName = userImage.getFileName();
                String imageUrl = userImage.getImageUrl();

                asyncDeleteImage(groupName, qrCode, fileName, imageUrl);    // Sync
            }
        } catch (Exception e) {

        } finally {
            // 3. DB 반영
            bannedUser.setType(DMLType.UPLOAD_BANNED.name());

            userService.updateUser(bannedUser);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> makeUploadOptions(String folderName, String fileName) {
        return ObjectUtils.asMap(
            "folder", folderName,
            "public_id", fileName,
            "overwrite", true,
            "use_filename", true,
            "unique_filename", true
        );
    }

    private String removeExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf(".");
        return (dotIndex != -1) ? fileName.substring(0, dotIndex) : fileName;
    }
}
