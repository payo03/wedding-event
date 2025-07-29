package com.cywedding.dto;

import lombok.Data;

@Data
public class Image {
    // FIELD
    private String fileName;
    private String qrCode;
    private String imageUrl;

    // CUSTOM
    private Integer groupId;

    private Integer count;
    private String base64File;
    private String plan;
}
