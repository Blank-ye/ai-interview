package com.interview.api.dto.response;

import lombok.Data;

@Data
public class ResumeVO {
    private Long id;
    private String fileName;
    private Integer status;
    private String structuredContent;
}
