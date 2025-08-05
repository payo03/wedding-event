package com.cywedding.service;

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

    public QRUser fetchQRUser(String groupName, String code) {
        logger.info("==================================================");
        logger.info("Domain : [{}], QR CODE : [{}]", groupName, code);
        logger.info("==================================================");

        QRUser param = new QRUser();
        param.setGroupName(groupName);
        param.setQrCode(code);

        QRUser user = userMapper.fetchQRUser(param);
        if (user == null) {
            user = userMapper.fetchQRUserAdmin(param);
        }
        return user;
    }

    public List<QRUser> fetchQRUserList(String groupName) {
        return userMapper.fetchQRUserList(groupName);
    }

    public Boolean validDML(String groupName, String code, DMLType type) {
        Boolean isValid = true;

        QRUser user = fetchQRUser(groupName, code);
        if (user == null) {
            return false;
        }
        if(user.isAdmin() || user.isDomainAdmin()) {
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

    public void updateUser(QRUser user) { this.updateUser(List.of(user)); }
    public void updateUser(List<QRUser> userList) {
        userMapper.updateUser(userList);
    }

    public void noticeSkipUser(QRUser user) {
        userMapper.noticeSkipUser(user);
    }
}
