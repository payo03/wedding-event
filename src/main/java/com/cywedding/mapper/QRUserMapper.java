package com.cywedding.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.cywedding.dto.QRUser;

@Mapper
public interface QRUserMapper {

    QRUser fetchQRUser(QRUser user);
    QRUser fetchQRUserAdmin(QRUser user);
    List<QRUser> fetchQRUserList(String groupName);

    void updateUser(@Param("userList") List<QRUser> userList);
    void noticeSkipUser(QRUser user);

    void resetUserList(@Param("groupId") Integer groupId);

    void createUserList(@Param("userList") List<QRUser> userList);
}