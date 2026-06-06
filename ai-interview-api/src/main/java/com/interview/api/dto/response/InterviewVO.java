package com.interview.api.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class InterviewVO {
    private Long id;
    private Long userId;
    private Long resumeId;
    private Long jobId;
    private Integer status;
    private BigDecimal totalScore;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
