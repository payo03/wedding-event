package com.cywedding.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.cywedding.dto.Image;

@Mapper
public interface ImageMapper {

    List<Image> selectImageList(@Param("plan") String plan);
    Image selectImage(String fileName);

    void uploadImage(Image image);
    
    void deleteImage(String fileName);
}
