package com.cywedding.service;

import org.springframework.stereotype.Service;

import com.cywedding.common.DMLType;
import com.cywedding.dto.QRUser;
import com.cywedding.mapper.QRUserMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QRUserService {

    private final QRUserMapper mapper;

    public QRUser fetchQRUser(String code) {
        System.out.println("QR CODE : [" + code + "]");

        return mapper.fetchQRUser(code);
    }

    public Boolean validDML(String code, DMLType type) {
        Boolean isValid = true;

        QRUser user = fetchQRUser(code);
        if (user == null) {
            return false; // QR 코드가 유효하지 않음
        }
        System.out.println("User : " + user + "");

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
        }

        // 관리자는 항상 유효함
        if(user.isAdmin()) {
            isValid = true;
        }

        return isValid;
    }
}
