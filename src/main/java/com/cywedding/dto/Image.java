package com.cywedding.dto;

import lombok.Data;

@Data
public class Image {
    // FIELD
    private String fileName;
    private String qrCode;
    private String imageUrl;

    // CUSTOM
    private Boolean isEmail;
    private String voteQRCode;
    private Integer groupId;
    private String groupName;

    private Integer count;
    private String base64File;
    private String plan;

    public Image() {
        isEmail = false;
    }

    public Image(Boolean isEmail) {
        this.isEmail = isEmail;
    }
}
