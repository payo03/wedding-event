package com.cywedding.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cywedding.dto.QRUser;
import com.cywedding.mapper.QRUserMapper;

@RestController
public class HealthCheckController {

    @Autowired
    private QRUserMapper userMapper;

    @GetMapping("/healthcheck")
    public String healthCheck(@RequestParam(required = false) String code) {

        String result = "SERVER IS RUNNING : " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        if (code != null) {
            QRUser user = userMapper.fetchQRUser(code);
            if (user != null) {
                result += "\nUSER: " + user;
            } else {
                result += "\nUSER: not found for code = " + code;
            }
        }

        return result;
    }
}