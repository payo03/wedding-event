package com.cywedding.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QRGroup {
    // FIELD
    private Integer groupId;
    private String groupName;
    private Integer maxUploads;
    private Integer maxVotes;
    
    // CUSTOM
    private String prefix;
    private Integer count;
}
