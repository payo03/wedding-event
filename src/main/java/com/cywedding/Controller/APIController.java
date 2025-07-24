package com.cywedding.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.cywedding.common.DMLType;
import com.cywedding.service.ImageService;
import com.cywedding.service.QRUserService;

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
@RequestMapping("/api/image")
public class APIController {

    private final QRUserService userService;
    private final ImageService imageService;

    public APIController(QRUserService userService, ImageService imageService) {
        this.userService = userService;
        this.imageService = imageService;
    }

    @PostMapping("/upload")
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

    @GetMapping("/list")
    public ResponseEntity<?> getImageList(@RequestHeader("X-QR-CODE") String code) {
        Map<String, Object> returnMap = new HashMap<>();

        try {
            List<Map<String, Object>> imageList = imageService.selectImageList();

            returnMap.put("success", true);
            returnMap.put("images", imageList);

            return ResponseEntity.ok(returnMap);
        } catch (Exception e) {
            returnMap.put("success", false);
            returnMap.put("message", "❌ 이미지 목록 조회 중 오류 발생 ❌");

            return ResponseEntity.internalServerError().body(returnMap);
        }
    }

    @GetMapping("/files/{filename:.+}")
    public ResponseEntity<?> getImageFile(@PathVariable String filename) throws Exception {
        Map<String, Object> returnMap = new HashMap<>();

        try {
            Path path = Paths.get(filename);  // 확장자 기반으로만 판단
            String mimeType = Files.probeContentType(path);
            MediaType contentType = MediaType.APPLICATION_OCTET_STREAM; // fallback

            if (mimeType != null) {
                contentType = MediaType.parseMediaType(mimeType);
            }

            byte[] imageData = imageService.selectImage(filename);
            if (imageData == null) {
                returnMap.put("success", false);
                returnMap.put("message", "❌ 해당 파일을 DB에서 찾을 수 없습니다. ❌");

                return ResponseEntity.status(404).body(returnMap);
            }

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

    @PostMapping("/vote")
    public ResponseEntity<?> voteImage(
            @RequestHeader("X-QR-CODE") String code,
            @RequestBody Map<String, String> infoMap
    ) {
        Map<String, Object> returnMap = new HashMap<>();

        String fileName = infoMap.get("fileName");
        Boolean isValid = userService.validDML(code, DMLType.VOTE);

        if (!isValid) {
            returnMap.put("success", false);
            returnMap.put("message", "❌ 이미 투표하셨습니다. ❌");

            return ResponseEntity.badRequest().body(returnMap);
        }

        try {
            imageService.voteImage(code, fileName);

            returnMap.put("success", true);
            returnMap.put("message", "✅ 투표 완료! ✅");
            returnMap.put("fileName", fileName);

            return ResponseEntity.ok(returnMap);
        } catch (Exception e) {
            e.printStackTrace();
            returnMap.put("success", false);
            returnMap.put("message", "❌ 투표 처리 중 오류 발생 ❌");

            return ResponseEntity.internalServerError().body(returnMap);
        }
    }
}
