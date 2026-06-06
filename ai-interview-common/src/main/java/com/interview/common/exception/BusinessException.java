package com.interview.common.exception;

import com.interview.common.result.ResultCode;
import lombok.Getter;

/**
 * 业务异常
 * 用于抛出业务错误，由GlobalExceptionHandler统一处理
 */
@Getter
public class BusinessException extends RuntimeException {
    private final Integer code;

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    public BusinessException(String message) {
        super(message);
        this.code = ResultCode.INTERNAL_ERROR.getCode();
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}
