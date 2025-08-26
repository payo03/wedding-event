package com.cywedding.dto;

import java.time.LocalDateTime;

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
    private boolean isNoticeSkip;

    // FEIDL2 (policy)
    private Integer maxUploads;
    private Integer maxVotes;
    private LocalDateTime votingStart;
    private LocalDateTime votingEnd;
    private LocalDateTime uploadStart;
    private LocalDateTime uploadEnd;

    // CUSTOM
    private Integer adminGroupId;
    private boolean isUpload;
    private boolean isVote;
    private String type;
    private String groupName;
    private String plan;
    private String imageUrl;    // 배경화면
    private String imageUrl2;   // 공지화면

    public Integer getCustomGroupId() {
        return isAdmin ? adminGroupId : groupId;
    }
}