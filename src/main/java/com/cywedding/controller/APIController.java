package com.cywedding.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.cywedding.common.DMLType;
import com.cywedding.service.ImageService;
import com.cywedding.service.VoteService;
import com.cywedding.service.QRUserService;
import com.cywedding.dto.QRUser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class APIController {
    private static final Logger logger = LoggerFactory.getLogger(APIController.class);

    private final QRUserService userService;
    private final ImageService imageService;
    private final VoteService voteService;

    public APIController(QRUserService userService, ImageService imageService, VoteService voteService) {
        this.userService = userService;
        this.imageService = imageService;
        this.voteService = voteService;
    }

    @PostMapping("/qr/create")
    public void qrCreate(
        @RequestHeader("X-QR-CODE") String code,
        @RequestBody Map<String, String> infoMap
    ) {
        String prefix = infoMap.get("prefix");
        int count = Integer.parseInt(infoMap.get("count"));

        userService.resetUserList();
        userService.createUserList(prefix, count);
    }

    @GetMapping("/user/check")
    public ResponseEntity<?> checkUser(@RequestHeader("X-QR-CODE") String code) {
        QRUser user = userService.fetchQRUser(code);
        
        return ResponseEntity.ok(user);
    }

    @PostMapping("/image/upload")
    @SuppressWarnings("null")
    public ResponseEntity<?> uploadImage(
            @RequestHeader("X-QR-CODE") String code,
            @RequestParam("file") MultipartFile file
        ) {
        Map<String, Object> returnMap = new HashMap<>();

        Boolean isValid = userService.validDML(code, DMLType.UPLOAD);
        if (!isValid) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "❌ 업로드한 사진이 존재합니다. ❌"
            ));
        }

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "❌ 파일이 비어 있습니다. ❌"
            ));
        }
        
        try {
            String extension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
            String timeStamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd HHmmss"));
            String fileName = code + "_" + timeStamp + extension;

            imageService.uploadImage(code, fileName, file.getBytes());

            // 성공 응답
            returnMap.put("success", true);
            returnMap.put("message", "✅ 업로드 성공! ✅");
            returnMap.put("fileName", fileName);

            return ResponseEntity.ok(returnMap);
        } catch (IOException e) {
            e.printStackTrace();

            returnMap.put("success", false);
            returnMap.put("message", "❌ 파일 저장 중 오류 발생 ❌");

            return ResponseEntity.internalServerError().body(returnMap);
        }
    }

    @GetMapping("/image/list")
    public ResponseEntity<?> getImageList(@RequestHeader("X-QR-CODE") String code) {
        Map<String, Object> returnMap = new HashMap<>();

        QRUser user = userService.fetchQRUser(code);
        try {
            List<Map<String, Object>> imageList = imageService.selectImageList();

            returnMap.put("success", true);
            returnMap.put("images", imageList);
            returnMap.put("user", user);

            return ResponseEntity.ok(returnMap);
        } catch (Exception e) {
            returnMap.put("success", false);
            returnMap.put("message", "❌ 이미지 목록 조회 중 오류 발생 ❌");

            return ResponseEntity.internalServerError().body(returnMap);
        }
    }

    @GetMapping("/image/files/{filename:.+}")
    public ResponseEntity<?> getImageFile(@PathVariable String filename) throws Exception {
        Map<String, Object> returnMap = new HashMap<>();

        try {
            Path path = Paths.get(filename);
            String mimeType = Files.probeContentType(path);
            MediaType contentType = MediaType.APPLICATION_OCTET_STREAM;

            if (mimeType != null) {
                contentType = MediaType.parseMediaType(mimeType);
            }

            byte[] imageData = imageService.selectImage(filename);
            return ResponseEntity.ok()
                    .contentType(contentType)
                    .body(imageData);
        } catch (Exception e) {
            e.printStackTrace();

            returnMap.put("success", false);
            returnMap.put("message", "❌ 이미지 조회 중 오류 발생 ❌");

            return ResponseEntity.internalServerError().body(returnMap);
        }
    }

    @PostMapping("/image/vote")
    public ResponseEntity<?> voteImage(
            @RequestHeader("X-QR-CODE") String code,
            @RequestBody Map<String, String> infoMap
    ) {
        Map<String, Object> returnMap = new HashMap<>();

        String fileName = infoMap.get("fileName");

        String message = "✅ 투표 완료! ✅";
        try {
            Boolean isValid = userService.validDML(code, DMLType.VOTE);
            if(!isValid) {
                voteService.deleteVote(code, fileName);
                message = "♻️ 재투표 완료! ♻️";
            }
            
            voteService.voteImage(code, fileName);

            returnMap.put("success", true);
            returnMap.put("message", message);
            returnMap.put("fileName", fileName);

            return ResponseEntity.ok(returnMap);
        } catch (Exception e) {
            e.printStackTrace();
            message = "❌ 투표 처리 중 오류 발생 ❌";

            returnMap.put("success", false);
            returnMap.put("message", message);

            return ResponseEntity.internalServerError().body(returnMap);
        }
    }

    @PostMapping("/image/delete")
    public ResponseEntity<?> deleteImage(
            @RequestHeader("X-QR-CODE") String code,
            @RequestBody Map<String, String> infoMap
    ) {
        Map<String, Object> returnMap = new HashMap<>();

        String fileName = infoMap.get("fileName");

        String message = "✅ 삭제 완료! ✅";
        try {
            imageService.deleteImage(code, fileName);

            returnMap.put("success", true);
            returnMap.put("message", message);

            return ResponseEntity.ok(returnMap);
        } catch (Exception e) {
            e.printStackTrace();
            message = "❌ 삭제 처리 중 오류 발생 ❌";

            returnMap.put("success", false);
            returnMap.put("message", message);

            return ResponseEntity.internalServerError().body(returnMap);
        }
    }

    @PostMapping("/image/email")
    public ResponseEntity<?> sendEmail(@RequestBody Map<String, String> infoMap) {
        Map<String, Object> returnMap = new HashMap<>();

        String emailAddress = infoMap.get("emailAddress");
        String plan = infoMap.get("plan");
        
        Boolean success = true;
        String message = "✅ 이메일 전송 완료! ✅";
        try {
            imageService.sendEmail(emailAddress, plan);

            returnMap.put("success", success);
            returnMap.put("message", message);

            return ResponseEntity.ok(returnMap);
        } catch (Exception e) {
            logger.info("==================================================");
            logger.info("이메일 전송 중 오류 발생 : {}", e.getMessage());
            logger.info("==================================================");

            returnMap.put("success", false);
            returnMap.put("message", "❌ 이메일 전송 중 오류 발생 ❌: " + e.getMessage());

            return ResponseEntity.internalServerError().body(returnMap);
        }
    }
}
