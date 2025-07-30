package com.cywedding.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.cywedding.dto.QRGroup;

@Mapper
public interface QRGroupMapper {

    QRGroup fetchQRGroup(@Param("groupName") String groupName);

    void createGroup(QRGroup qrGroup);
    void createPolicy(QRGroup qrGroup);

    void insertQRPolicy(QRGroup group);
}