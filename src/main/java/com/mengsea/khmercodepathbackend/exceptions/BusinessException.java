package com.mengsea.khmercodepathbackend.exceptions;

import com.mengsea.khmercodepathbackend.constant.ExceptionCode;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final ExceptionCode exceptionCode;
    private final Object data; // optional payload data

    // constructor called when no data response
    public BusinessException(ExceptionCode exceptionCode){
        super(exceptionCode.getMessage());
        this.exceptionCode = exceptionCode;
        this.data = null;
    }

    // constructor called when has data response
    public BusinessException(ExceptionCode exceptionCode, Object data){
        super(exceptionCode.getMessage());
        this.exceptionCode = exceptionCode;
        this.data = data;
    }
}
