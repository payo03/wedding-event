package com.cywedding.dto;

import lombok.Data;

@Data
public class QRUser {
    private String qrCode;
    private boolean isUpload;
    private boolean isVote;
    private boolean isAdmin;
}