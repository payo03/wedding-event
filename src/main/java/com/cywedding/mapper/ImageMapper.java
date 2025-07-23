package com.cywedding.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ImageMapper {
    
    List<Map<String, Object>> selectImageList();
    Map<String, Object> selectImage(@Param("fileName") String fileName);

    void uploadImage(@Param("code") String code,
                     @Param("fileName") String fileName,
                     @Param("file") byte[] file);
}
