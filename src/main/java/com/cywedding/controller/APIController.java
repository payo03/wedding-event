package com.cywedding.controller;

import org.postgresql.util.PSQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.cywedding.common.DMLType;
import com.cywedding.config.UploadLimitConfig;
import com.cywedding.service.CloudinaryService;
import com.cywedding.service.ImageService;
import com.cywedding.service.QRGroupService;
import com.cywedding.service.VoteService;
import com.cywedding.service.QRUserService;
import com.cywedding.dto.Image;
import com.cywedding.dto.QRUser;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class APIController {
    private static final Logger logger = LoggerFactory.getLogger(APIController.class);

    private final QRGroupService groupService;
    private final QRUserService userService;
    private final ImageService imageService;
    private final VoteService voteService;
    private final CloudinaryService cloudinaryService;

    @Autowired
    private UploadLimitConfig limitConfig;

    public APIController(
            QRGroupService groupService, 
            QRUserService userService, 
            ImageService imageService, 
            VoteService voteService, 
            CloudinaryService cloudinaryService
    ) {
            this.groupService = groupService;
            this.userService = userService;
            this.imageService = imageService;
            this.voteService = voteService;
            this.cloudinaryService = cloudinaryService;
    }

    /**
     * QR 코드 생성 요청
     * @param infoMap   QR 코드 생성에 필요한 정보가 담긴 Map
     */
    @PostMapping("/qr/create")
    public void qrCreate(
            @RequestBody Map<String, String> infoMap
    ) {

        groupService.createQR(infoMap);
    }

    /**
     * QR 코드 시간 조정 요청
     * @param groupName Header로 전달되는 그룹 이름
     * @param infoMap   시간 조정에 필요한 정보가 담긴 Map (예: uploadStart, uploadEnd, votingStart, votingEnd)
     */
    @PostMapping("/qr/time")
    public void updateQRTime(
            @RequestHeader("X-DOMAIN") String groupName,
            @RequestBody Map<String, String> infoMap
    ) {
        logger.info("==================================================");
        logger.info("{}", infoMap);
        logger.info("==================================================");

        groupService.updateQRTime(groupName, infoMap);
    }

    /**
     * QR 코드로 사용자 정보 조회
     * @param groupName Header로 전달되는 그룹 이름
     * @param code      Header로 전달되는 QR 코드
     * @return
     */
    @GetMapping("/user/check")
    public ResponseEntity<?> checkUser(
            @RequestHeader("X-DOMAIN") String groupName,
            @RequestHeader("X-QR-CODE") String code
    ) {
        QRUser user = userService.fetchQRUser(groupName, code);
        
        return ResponseEntity.ok(user);
    }

    @PostMapping("/user/skip")
    public ResponseEntity<?> noticeSkipUser(
            @RequestHeader("X-DOMAIN") String groupName,
            @RequestHeader("X-QR-CODE") String code
    ) {
        QRUser user = userService.fetchQRUser(groupName, code);
        userService.noticeSkipUser(user);
        
        return ResponseEntity.ok(user);
    }

    /**
     * 이미지 업로드 요청
     * @param groupName Header로 전달되는 그룹 이름
     * @param code      Header로 전달되는 QR 코드
     * @param file      업로드할 이미지 파일
     * @return
     */
    @PostMapping("/image/upload")
    public ResponseEntity<?> uploadImage(
            @RequestHeader("X-DOMAIN") String groupName,
            @RequestHeader("X-QR-CODE") String code,
            @RequestParam MultipartFile file
        ) {
        Map<String, Object> returnMap = new HashMap<>();

        // 1. 기 업로드 요청자일경우
        Boolean isValid = userService.validDML(groupName, code, DMLType.UPLOAD);
        if (!isValid) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "❌ 이미지 업로드 횟수를 초과하였습니다. ❌"
            ));
        }

        // 2. 업로드 요청이 너무 많을 경우
        if (!limitConfig.tryAcquire()) {
            return ResponseEntity.status(429).body(Map.of(
                "success", false,
                "message", "❌ 이미지 업로드 요청이 많아 대기 중입니다. (최대 동시 업로드 인원: " + limitConfig.getLimit() + "명) 잠시 후 다시 시도해 주세요. ❌"
            ));
        }

        // 3. 파일이 비어있는 경우
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "❌ 이미지가 비어 있습니다. ❌"
            ));
        }
        
        try {
            byte[] fileBytes = file.getBytes();
            String fileName = file.getOriginalFilename();
            cloudinaryService.asyncUploadImage(groupName, code, fileName, fileBytes);

            // 성공 응답
            returnMap.put("success", true);
            returnMap.put("message", "✅ 업로드 요청 완료! ✅");

            return ResponseEntity.ok(returnMap);
        } catch (IOException e) {
            logger.info("==================================================");
            e.printStackTrace();
            logger.info("==================================================");

            returnMap.put("success", false);
            returnMap.put("message", "❌ 이미지 업로드 요청 중 오류 발생 ❌");

            return ResponseEntity.internalServerError().body(returnMap);
        }
    }

    /**
     * 이미지 목록 조회 요청
     * @param groupName Header로 전달되는 그룹 이름
     * @param code      Header로 전달되는 QR 코드
     * @return
     */
    @GetMapping("/image/list")
    public ResponseEntity<?> getImageList(
            @RequestHeader("X-DOMAIN") String groupName,
            @RequestHeader("X-QR-CODE") String code
    ) {
        Map<String, Object> returnMap = new HashMap<>();

        QRUser user = userService.fetchQRUser(groupName, code);
        String plan = "P"; // default
        try {
            // Admin 혹은 본인사진인경우
            List<Image> images = imageService.selectImageList(user, plan).stream()
                .filter(image -> user.isDomainAdmin() || user.isAdmin() || image.isOpen())
                .collect(Collectors.toList());

            returnMap.put("success", true);
            returnMap.put("images", images);
            returnMap.put("user", user);

            return ResponseEntity.ok(returnMap);
        } catch (Exception e) {
            logger.info("==================================================");
            e.printStackTrace();
            logger.info("==================================================");

            returnMap.put("success", false);
            returnMap.put("message", "❌ 이미지 목록 조회 중 오류 발생 ❌");

            return ResponseEntity.internalServerError().body(returnMap);
        }
    }

    @PostMapping("/image/banned")
    public ResponseEntity<?> bannedUser(
            @RequestHeader("X-DOMAIN") String groupName,
            @RequestHeader("X-QR-CODE") String code,
            @RequestBody Map<String, String> infoMap
    ) {
        Map<String, Object> returnMap = new HashMap<>();
        
        logger.info("==================================================");
        logger.info("{}", infoMap);
        logger.info("==================================================");

        String qrCode = infoMap.get("qrCode");

        String message = "✅ 사용자 금지 완료! ✅";
        try {
            cloudinaryService.asyncBannedUser(groupName, qrCode);

            returnMap.put("success", true);
            returnMap.put("message", message);

            return ResponseEntity.ok(returnMap);
        } catch (Exception e) {
            logger.info("==================================================");
            e.printStackTrace();
            logger.info("==================================================");

            message = "❌ 사용자 업로드 금지 반영중 오류 발생 ❌";

            returnMap.put("success", false);
            returnMap.put("message", message);

            return ResponseEntity.internalServerError().body(returnMap);
        }
    }

    /**
     * 이미지 투표 요청
     * @param groupName Header로 전달되는 그룹 이름
     * @param code      Header로 전달되는 QR 코드
     * @param infoMap   투표할 이미지 정보가 담긴 Map (예: fileName)
     * @return
     */
    @PostMapping("/image/vote")
    public ResponseEntity<?> voteImage(
            @RequestHeader("X-DOMAIN") String groupName,
            @RequestHeader("X-QR-CODE") String code,
            @RequestBody Map<String, String> infoMap
    ) {
        Map<String, Object> returnMap = new HashMap<>();

        String fileName = infoMap.get("fileName");

        String message = "✅ 투표 완료! ✅";
        try {
            Boolean isValid = userService.validDML(groupName, code, DMLType.VOTE);
            if(!isValid) {
                voteService.deleteVote(groupName, code, fileName);
                message = "♻️ 재투표 완료! ♻️";
            }
            
            voteService.voteImage(groupName, code, fileName);

            returnMap.put("success", true);
            returnMap.put("message", message);
            returnMap.put("fileName", fileName);

            return ResponseEntity.ok(returnMap);
        } catch (DataIntegrityViolationException e) {
            logger.info("==================================================");
            e.printStackTrace();
            logger.info("==================================================");

            message = "❌ 투표 처리 중 오류 발생 ❌";

            Throwable rootCause = e.getRootCause();
            if (rootCause instanceof PSQLException) {
                PSQLException psqlEx = (PSQLException) rootCause;
                if ("23505".equals(psqlEx.getSQLState())) { // 중복 키 위반 오류 (SQLState '23505')
                    message = "❌ 이미 투표하신 사진입니다. ❌";
                }

                returnMap.put("success", false);
                returnMap.put("message", message);

                return ResponseEntity.status(HttpStatus.CONFLICT).body(returnMap);
            } else {
                returnMap.put("success", false);
                returnMap.put("message", message);

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnMap);
            }
        } catch (Exception e) {
            logger.info("==================================================");
            e.printStackTrace();
            logger.info("==================================================");

            message = "❌ 투표 처리 중 오류 발생 ❌";

            returnMap.put("success", false);
            returnMap.put("message", message);

            return ResponseEntity.internalServerError().body(returnMap);
        }
    }

    /**
     * 이미지 삭제 요청
     * @param groupName Header로 전달되는 그룹 이름
     * @param code      Header로 전달되는 QR 코드
     * @param infoMap   삭제할 이미지 정보가 담긴 Map (예: fileName)
     * @return
     */
    @PostMapping("/image/delete")
    public ResponseEntity<?> deleteImage(
            @RequestHeader("X-DOMAIN") String groupName,
            @RequestHeader("X-QR-CODE") String code,
            @RequestBody Map<String, String> infoMap
    ) {
        logger.info("==================================================");
        logger.info("{}", infoMap);
        logger.info("==================================================");

        Map<String, Object> returnMap = new HashMap<>();

        String qrCode = infoMap.get("qrCode");
        String fileName = infoMap.get("fileName");
        String imageUrl = infoMap.get("imageUrl");

        String message = "✅ 삭제 완료! ✅";
        try {
            cloudinaryService.asyncDeleteImage(groupName, qrCode, fileName, imageUrl);

            returnMap.put("success", true);
            returnMap.put("message", message);

            return ResponseEntity.ok(returnMap);
        } catch (Exception e) {
            logger.info("==================================================");
            e.printStackTrace();
            logger.info("==================================================");

            message = "❌ 삭제 처리 중 오류 발생 ❌";

            returnMap.put("success", false);
            returnMap.put("message", message);

            return ResponseEntity.internalServerError().body(returnMap);
        }
    }

    /**
     * 이메일로 이미지 전송 요청
     * @param groupName Header로 전달되는 그룹 이름
     * @param infoMap   이메일 전송에 필요한 정보가 담긴 Map (예: plan, emailAddress)
     * @return
     */
    @PostMapping("/image/email")
    public ResponseEntity<?> sendEmail(
            @RequestHeader("X-DOMAIN") String groupName,
            @RequestBody Map<String, String> infoMap
    ) {
        Map<String, Object> returnMap = new HashMap<>();

        String plan = infoMap.get("plan");
        String emailAddress = infoMap.get("emailAddress");
        
        Boolean success = true;
        String message = "✅ 이메일 전송 완료! ✅";
        try {
            imageService.sendEmail(groupName, plan, emailAddress);

            returnMap.put("success", success);
            returnMap.put("message", message);

            return ResponseEntity.ok(returnMap);
        } catch (Exception e) {
            logger.info("==================================================");
            e.printStackTrace();
            logger.info("==================================================");

            returnMap.put("success", false);
            returnMap.put("message", "❌ 이메일 전송 중 오류 발생 ❌: " + e.getMessage());

            return ResponseEntity.internalServerError().body(returnMap);
        }
    }
}
