package com.cywedding.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class VueController {
    private static final Logger logger = LoggerFactory.getLogger(VueController.class);
    
    @RequestMapping("/image/list")
    public String forwardToIndex() {
        return "forward:/index.html";
    }
    
    @GetMapping("/qr/{groupName}/{qrCode}")
    public String qrLogin(
        @PathVariable String groupName,
        @PathVariable String qrCode
    ) {
        logger.info("==================================================");
        logger.info("Domain : {}, QR Code : {}", groupName, qrCode);
        logger.info("==================================================");

        return "forward:/index.html";
    }
}
