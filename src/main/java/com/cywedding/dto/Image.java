package com.cywedding.dto;

import lombok.Data;

@Data
public class Image {
    private String fileName;   // PRIMARY KEY
    private String qrCode;     // FOREIGN KEY to qr_user.qr_code
    private byte[] file;       // BYTEA
    private int count;
}
