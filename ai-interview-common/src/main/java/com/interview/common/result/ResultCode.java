package com.interview.common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 响应状态码
 * 200: 成功
 * 4xx: 客户端错误
 * 5xx: 服务端错误
 * 1xxx: 业务错误码
 */
@Getter
@AllArgsConstructor
public enum ResultCode {
    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或token已过期"),
    FORBIDDEN(403, "没有权限"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    // 业务错误码
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_ALREADY_EXISTS(1002, "用户已存在"),
    PASSWORD_ERROR(1003, "密码错误"),
    RESUME_NOT_FOUND(1004, "简历不存在"),
    RESUME_PARSE_FAILED(1005, "简历解析失败"),
    INTERVIEW_NOT_FOUND(1006, "面试不存在"),
    INTERVIEW_ALREADY_ENDED(1007, "面试已结束");

    private final Integer code;
    private final String message;
}
