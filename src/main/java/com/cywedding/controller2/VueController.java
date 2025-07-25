package com.cywedding.controller2;

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
    
    @GetMapping("/qr/{qrcode}")
    public String qrLogin(@PathVariable(name = "qrcode", required = true) String code) {
        logger.info("==================================================");
        logger.info("QR Code : {}", code);
        logger.info("==================================================");

        return "forward:/index.html";
    }
}
