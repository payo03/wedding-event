package com.cywedding.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.cywedding.dto.QRUser;

@Mapper
public interface QRUserMapper {

    QRUser fetchQRUser(String code);
    void updateUser(QRUser qrUser);
}