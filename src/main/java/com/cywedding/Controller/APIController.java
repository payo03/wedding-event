package com.cywedding.Controller;

import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.bind.annotation.RestController;

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
import java.util.List;

@RestController
@RequestMapping("/api/image")
public class APIController {

    private static final String UPLOAD_DIR = "images";

    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("❌ 파일이 비어 있습니다.");
        }

        try {
            // 파일 이름 유니크하게
            String originalFilename = file.getOriginalFilename();
            String timeStamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd HHmmss"));
            String safeFileName = timeStamp + "_" + originalFilename;

            // 저장 경로 생성 (예: uploads 폴더)
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs(); // 폴더가 없으면 생성
            }

            // 저장 경로
            File destination = new File(uploadDir, safeFileName);
            Files.copy(file.getInputStream(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);

            // 성공 응답
            return ResponseEntity.ok().body("✅ 업로드 성공: " + safeFileName);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("❌ 파일 저장 중 오류 발생");
        }
    }

    @GetMapping("/list")
    public ResponseEntity<List<String>> getImageList() {
        File folder = new File(UPLOAD_DIR);
        File[] files = folder.listFiles();

        List<String> imageList = new ArrayList<>();

        if (files != null) {
            for (File file : files) {
                String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path("/image/files/")
                        .path(file.getName())
                        .toUriString();
                imageList.add(fileDownloadUri);
            }
        }

        return ResponseEntity.ok(imageList);
    }

    @GetMapping("/files/{filename:.+}")
    public ResponseEntity<?> getImageFile(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(UPLOAD_DIR).resolve(filename).normalize();
            UrlResource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG) // 필요 시 타입 감지 로직 추가 가능
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().body("잘못된 파일 경로");
        }
    }
}
