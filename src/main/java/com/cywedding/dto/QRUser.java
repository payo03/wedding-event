package com.cywedding.dto;

import lombok.Data;

@Data
public class QRUser {
    // FIELD
    private Integer groupId;
    private String qrCode;
    private Integer uploadCount;
    private Integer voteCount;
    private boolean isDomainAdmin;
    private boolean isAdmin;

    // CUSTOM
    private Integer adminGroupId;
    private boolean isUpload;
    private boolean isVote;
    private String type;
    private String groupName;

    public Integer getCustomGroupId() {
        return isAdmin ? adminGroupId : groupId;
    }
}