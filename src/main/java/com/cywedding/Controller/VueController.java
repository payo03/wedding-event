package com.cywedding.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class VueController {
    
    @RequestMapping("/image/list")
    public String forwardToIndex() {
        return "forward:/index.html";
    }
    
    @GetMapping("/qr/{qrcode}")
    public String qrLogin(@PathVariable(name = "qrcode", required = true) String path) {
        System.out.println("QR Code Path: " + path);

        return "forward:/index.html";
    }
}
