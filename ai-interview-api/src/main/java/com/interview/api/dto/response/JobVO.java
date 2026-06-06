package com.interview.api.dto.response;

import lombok.Data;

@Data
public class JobVO {
    private Long id;
    private String title;
    private String company;
    private String description;
    private String requirements;
    private String salaryRange;
    private String location;
}
