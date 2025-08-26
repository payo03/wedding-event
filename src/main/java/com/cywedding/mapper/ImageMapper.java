package com.cywedding.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.cywedding.dto.Image;
import com.cywedding.dto.QRUser;

@Mapper
public interface ImageMapper {

    List<Image> selectImageList(Image image);
    Image selectImage(@Param("fileName") String fileName);

    void uploadImage(Image image);
    
    void deleteImageByImage(Image image);
    void deleteImageByUser(QRUser user);
}
