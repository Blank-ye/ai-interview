package com.interview.api.dto.response;

import lombok.Data;

@Data
public class JobMatchVO {
    private Long jobId;
    private String jobTitle;
    private String company;
    private Double score;
}
