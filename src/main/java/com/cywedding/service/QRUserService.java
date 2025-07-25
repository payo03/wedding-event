package com.cywedding.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.cywedding.common.DMLType;
import com.cywedding.dto.QRUser;
import com.cywedding.mapper.QRUserMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QRUserService {
    private static final Logger logger = LoggerFactory.getLogger(QRUserService.class);

    private final QRUserMapper userMapper;

    public QRUser fetchQRUser(String code) {
        logger.info("==================================================");
        logger.info("QR CODE : [{}]", code);
        logger.info("==================================================");

        return userMapper.fetchQRUser(code);
    }

    public Boolean validDML(String code, DMLType type) {
        Boolean isValid = true;

        QRUser user = fetchQRUser(code);
        if (user == null) {
            return false;
        }
        if(user.isAdmin()) {
            // 관리자는 항상 유효함
            return true;
        }

        logger.info("==================================================");
        logger.info("{}", user);
        logger.info("==================================================");

        switch (type) {
            case UPLOAD:
                if (user.isUpload()) {
                    isValid = false;
                }
                break;
            case VOTE:
                if (user.isVote()) {
                    isValid = false;
                }
                break;
            default:
                break;
        }

        return isValid;
    }

    public void updateUserList(QRUser user) { this.updateUserList(List.of(user)); }
    public void updateUserList(List<QRUser> userList) {
        userMapper.updateUserList(userList);
    }

    public void resetUserList() {
        userMapper.resetUserList();
    }

    public void createUserList(String prefix, Integer count) {
        List<QRUser> userList = new ArrayList<QRUser>();
        for(Integer i = 1; i <= count; i++) {
            QRUser user = new QRUser();
            user.setQrCode(prefix + String.format("%03d", i));
            user.setUpload(false);
            user.setVote(false);
            user.setAdmin(false);

            userList.add(user);
        }
        logger.info("생성된 QRUser 목록: {}", userList);
        userMapper.createUserList(userList);
    }
}
