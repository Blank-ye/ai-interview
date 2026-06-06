package com.interview.api.dto.request;

import lombok.Data;

/**
 * 职位请求DTO
 */
@Data
public class JobRequest {
    private String title;
    private String company;
    private String description;
    private String requirements;
    private String skills;
    private String salaryRange;
    private String location;
}
