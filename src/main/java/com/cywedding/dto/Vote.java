package com.cywedding.dto;

import lombok.Data;

@Data
public class Vote {
    // FIELD
    private String fileName;
    private String qrCode;

    // CUSTOM
    private Integer groupId;
    private String groupName;
}
