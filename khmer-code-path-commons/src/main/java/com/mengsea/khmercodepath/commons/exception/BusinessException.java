package com.mengsea.khmercodepath.commons.exception;

import com.mengsea.khmercodepath.commons.constant.ExceptionCode;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final ExceptionCode exceptionCode;
    private final Object data;

    public BusinessException(ExceptionCode exceptionCode) {
        super(exceptionCode.getMessage());
        this.exceptionCode = exceptionCode;
        this.data = null;
    }

    public BusinessException(ExceptionCode exceptionCode, Object data) {
        super(exceptionCode.getMessage());
        this.exceptionCode = exceptionCode;
        this.data = data;
    }
}
