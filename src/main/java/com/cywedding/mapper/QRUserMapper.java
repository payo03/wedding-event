package com.cywedding.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.cywedding.common.DMLType;
import com.cywedding.dto.QRUser;

@Mapper
public interface QRUserMapper {

    QRUser fetchQRUser(@Param("code") String code);

    void updateUser(@Param("code") String code, @Param("type") DMLType type);
}