package com.cywedding.service;

import com.cywedding.common.DMLType;
import com.cywedding.dto.Image;
import com.cywedding.dto.QRUser;
import com.cywedding.mapper.ImageMapper;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ImageService {

    @Autowired
    QRUserService userService;

    private final ImageMapper imageMapper;

    public void uploadImage(String code, String fileName, byte[] file) {
        Image image = new Image();
        image.setQrCode(code);
        image.setFileName(fileName);
        image.setFile(file);

        imageMapper.uploadImage(image);

        // QRUser 객체 생성해서 업데이트
        QRUser user = new QRUser();
        user.setQrCode(image.getQrCode());
        user.setType(DMLType.UPLOAD.name()); // String으로 설정
        userService.updateUserList(user);
    }

    public List<Map<String, Object>> selectImageList() {
        return imageMapper.selectImageList();
    }

    public byte[] selectImage(String fileName) {
        Map<String, Object> imageObj = fetchImage(fileName);

        Object fileObj = imageObj.get("file");
        if (fileObj instanceof byte[] bytes) {
            return bytes;
        } else {
            throw new IllegalArgumentException("파일형식 오류: " + fileObj);
        }
    }

    // 관리자 전용
    public void deleteImage(String code, String fileName) {
        Map<String, Object> imageObj = fetchImage(fileName);
        System.out.println(imageObj);

        QRUser user = new QRUser();
        user.setQrCode(String.valueOf(imageObj.get("code")));
        user.setType(DMLType.DELETE.name());

        userService.updateUserList(user);
        imageMapper.deleteImage(fileName);
    }

    public Map<String, Object> fetchImage(String fileName) {
        Map<String, Object> image = imageMapper.selectImage(fileName);
        if (image == null) {
            throw new IllegalArgumentException("이미지가 존재하지 않습니다: " + fileName);
        }
        return image;
    }
}