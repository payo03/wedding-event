package com.cywedding.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cywedding.common.DMLType;
import com.cywedding.dto.QRUser;
import com.cywedding.mapper.ImageMapper;
import com.cywedding.mapper.QRUserMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QRUserService {
    private static final Logger logger = LoggerFactory.getLogger(QRUserService.class);

    private final QRUserMapper userMapper;
    private final ImageMapper imageMapper;

    public QRUser fetchQRUser(String domain, String code) {
        logger.info("==================================================");
        logger.info("Domain : [{}], QR CODE : [{}]", domain, code);
        logger.info("==================================================");

        QRUser param = new QRUser();
        param.setGroupName(domain);
        param.setQrCode(code);

        QRUser user = userMapper.fetchQRUser(param);
        if (user == null) {
            user = userMapper.fetchQRUserAdmin(param);
        }
        return user;
    }

    public Boolean validDML(String domain, String code, DMLType type) {
        Boolean isValid = true;

        QRUser user = fetchQRUser(domain, code);
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

    @Transactional
    public void bannedUser(String groupName, String qrCode, String fileName) {
        QRUser bannedUser = fetchQRUser(groupName, qrCode);
        bannedUser.setType(DMLType.UPLOAD_BANNED.name());

        updateUserList(bannedUser);
        imageMapper.deleteImageByUser(bannedUser);
    }

    public void updateUserList(QRUser user) { this.updateUserList(List.of(user)); }
    public void updateUserList(List<QRUser> userList) {
        userMapper.updateUserList(userList);
    }
}
