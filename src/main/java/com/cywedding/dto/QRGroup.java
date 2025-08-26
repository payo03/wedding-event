package com.cywedding.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QRGroup {
    // FIELD
    private Integer groupId;
    private String groupName;
    private String plan;
    private String imageUrl;
    
    // FEIDL2 (policy)
    private Integer policyVersion;
    private Integer maxUploads;
    private Integer maxVotes;
    private LocalDateTime votingStart;
    private LocalDateTime votingEnd;
    private LocalDateTime uploadStart;
    private LocalDateTime uploadEnd;
    
    // CUSTOM
    private String prefix;
    private Integer count;
    private boolean isNoticeSkip;
}
