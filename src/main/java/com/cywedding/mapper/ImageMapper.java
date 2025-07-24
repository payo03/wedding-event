package com.cywedding.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.cywedding.dto.Image;

@Mapper
public interface ImageMapper {

    List<Map<String, Object>> selectImageList();
    Map<String, Object> selectImage(String fileName);

    void uploadImage(Image image);
    void insertVote(Image image);
}
