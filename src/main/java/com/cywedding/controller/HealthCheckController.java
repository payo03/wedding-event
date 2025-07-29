package com.cywedding.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cywedding.dto.QRUser;
import com.cywedding.mapper.QRUserMapper;

@RestController
public class HealthCheckController {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private QRUserMapper userMapper;

    @GetMapping("/healthcheck")
    public Map<String, Object> healthCheck(
            @RequestParam(required = false) String groupName,
            @RequestParam(required = false) String qrCode
    ) {
        Map<String, Object> result = new LinkedHashMap<>();

        result.put("status", "SERVER IS RUNNING");
        result.put("timestamp", LocalDateTime.now().format(FORMATTER));

        if (groupName != null && qrCode != null) {
            QRUser param = new QRUser();
            param.setGroupName(groupName);
            param.setQrCode(qrCode);
            
            QRUser user = userMapper.fetchQRUser(param);
            if (user != null) {
                result.put("user", user);
            }
        }

        return result;
    }
}