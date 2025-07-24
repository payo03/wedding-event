package com.cywedding.dto;

import lombok.Data;

@Data
public class QRUser {
    // FIELD
    private String qrCode;
    private boolean isUpload;
    private boolean isVote;
    private boolean isAdmin;

    // CUSTOM
    private String type;
}