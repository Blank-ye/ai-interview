package com.interview.api.dto.response;

import lombok.Data;

@Data
public class LoginVO {
    private String token;
    private Long userId;
    private String username;
}
