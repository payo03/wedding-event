package com.cywedding.dto;

import lombok.Data;

@Data
public class QRUser {
    // FIELD
    private Integer groupId;
    private String qrCode;
    private boolean isUpload;
    private boolean isVote;
    private boolean isDomainAdmin;
    private boolean isAdmin;

    // CUSTOM
    private String type;
    private String groupName;
}