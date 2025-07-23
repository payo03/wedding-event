package com.cywedding.controller;

import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.cywedding.common.DMLType;
import com.cywedding.dto.QRUser;
import com.cywedding.service.ImageService;
import com.cywedding.service.QRUserService;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.StandardCopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/image")
public class APIController {

    private final QRUserService userService;
    private final ImageService imageService;
    private static final String UPLOAD_DIR = "images";

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

            /*
            // 저장 경로 생성 (예: uploads 폴더)
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs(); // 폴더가 없으면 생성
            }

            // 저장 경로
            File destination = new File(uploadDir, fileName);
            Files.copy(file.getInputStream(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
            */
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

        /*
        File folder = new File(UPLOAD_DIR);

        // 폴더가 없으면 에러 응답
        if (!folder.exists() || !folder.isDirectory()) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "❌ 이미지 폴더가 존재하지 않습니다. ❌"
            ));
        }

        File[] files = folder.listFiles();

        List<Map<String, String>> imageList = new ArrayList<>();
        if (files != null) {
            for (File file : files) {
                String fileURL = ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path("/api/image/files/")
                        .path(file.getName())
                        .toUriString();

                imageList.add(Map.of(
                        "name", file.getName(),
                        "url", fileURL
                ));
            }
        }
        */
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
            /*
            Path filePath = Paths.get(UPLOAD_DIR).resolve(filename).normalize();
            UrlResource resource = new UrlResource(filePath.toUri());
            String contentType = Files.probeContentType(filePath);

            if (resource.exists()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .body(resource);
            } else {
                return ResponseEntity.status(404).body(Map.of(
                        "success", false,
                        "message", "❌ 해당 파일을 찾을 수 없습니다. ❌"
                ));
            }
            */
            String ext = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
            MediaType contentType = switch (ext) {
                case "png" -> MediaType.IMAGE_PNG;
                case "gif" -> MediaType.IMAGE_GIF;
                default -> MediaType.IMAGE_JPEG;
            };

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

        String imageName = infoMap.get("imageName");
        Boolean isValid = userService.validDML(code, DMLType.VOTE);

        if (!isValid) {
            returnMap.put("success", false);
            returnMap.put("message", "❌ 이미 투표하셨습니다. ❌");

            return ResponseEntity.badRequest().body(returnMap);
        }

        try {
            // 실제 투표 수 증가 처리
            // imageService.voteImage(imageName, code);

            returnMap.put("success", true);
            returnMap.put("message", "✅ 투표 완료! ✅");
            returnMap.put("imageName", imageName);

            return ResponseEntity.ok(returnMap);
        } catch (Exception e) {
            e.printStackTrace();
            returnMap.put("success", false);
            returnMap.put("message", "❌ 투표 처리 중 오류 발생 ❌");

            return ResponseEntity.internalServerError().body(returnMap);
        }
    }
}
