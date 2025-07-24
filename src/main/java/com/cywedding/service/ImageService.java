package com.cywedding.service;

import com.cywedding.common.DMLType;
import com.cywedding.dto.Image;
import com.cywedding.dto.QRUser;
import com.cywedding.mapper.ImageMapper;
import com.cywedding.mapper.QRUserMapper;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final ImageMapper imageMapper;
    private final QRUserMapper userMapper;

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
        userMapper.updateUser(user);
    }

    public List<Map<String, Object>> selectImageList() {
        return imageMapper.selectImageList();
    }

    public byte[] selectImage(String fileName) {
        Map<String, Object> result = imageMapper.selectImage(fileName);
        if (result == null) return null;

        Object fileObj = result.get("file");
        if (fileObj instanceof byte[] bytes) {
            return bytes;
        }

        throw new IllegalArgumentException("DB에서 가져온 파일 데이터가 byte[] 형식이 아님: " + fileObj);
    }

    public void voteImage(String code, String fileName) {
        Map<String, Object> dbImage = imageMapper.selectImage(fileName);
        if (dbImage == null) {
            throw new IllegalArgumentException("이미지가 존재하지 않습니다: " + fileName);
        }

        Image image = new Image();
        image.setQrCode(code);
        image.setFileName(fileName);
        imageMapper.insertVote(image);

        QRUser user = new QRUser();
        user.setQrCode(image.getQrCode());
        user.setType(DMLType.VOTE.name());
        userMapper.updateUser(user);
    }
}