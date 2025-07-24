package com.cywedding.dto;

import lombok.Data;

@Data
public class Image {
    // FIELD
    private String fileName;
    private String qrCode;
    private byte[] file;

    // CUSTOM
    private int count;
}
