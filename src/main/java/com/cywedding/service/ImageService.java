package com.cywedding.service;

import com.cywedding.common.DMLType;
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

    public void uploadImage(String code, String fileName, byte[] fileBytes) {
        imageMapper.uploadImage(code, fileName, fileBytes);
        userMapper.updateUser(code, DMLType.UPLOAD);
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
}
