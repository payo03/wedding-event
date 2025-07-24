package com.cywedding.service;

import com.cywedding.common.DMLType;
import com.cywedding.dto.Image;
import com.cywedding.dto.QRUser;
import com.cywedding.dto.Vote;
import com.cywedding.mapper.ImageMapper;
import com.cywedding.mapper.VoteMapper;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ImageService {

    @Autowired
    QRUserService userService;

    private final ImageMapper imageMapper;
    private final VoteMapper voteMapper;

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
        Map<String, Object> result = fetchImage(fileName);

        Object fileObj = result.get("file");
        if (fileObj instanceof byte[] bytes) {
            return bytes;
        } else {
            throw new IllegalArgumentException("파일형식 오류: " + fileObj);
        }
    }

    // 관리자 전용
    public void deleteImage(String code, String fileName) {
        fetchImage(fileName);

        List<Vote> voteList = voteMapper.selectVoteList(fileName);
        List<QRUser> userList = voteList.stream()
            .map(vote -> {
                QRUser user = new QRUser();
                user.setQrCode(vote.getQrCode());
                user.setType(DMLType.DELETE.name());

                return user;
            }).collect(Collectors.toList());

        userService.updateUserList(userList);
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