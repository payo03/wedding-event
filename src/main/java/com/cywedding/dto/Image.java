package com.cywedding.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class Image {
    // FIELD
    private String qrCode;
    private String fileName;
    private String imageUrl;
    private Integer groupId;
    private LocalDateTime createdAt;

    // CUSTOM
    private boolean isOpen;
    private boolean isEmail;
    private String voteQRCode;
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
