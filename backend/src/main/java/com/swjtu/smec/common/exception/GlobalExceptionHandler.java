package com.swjtu.smec.common.exception;

import com.swjtu.smec.common.enums.GlobalErrorCodeConstants;
import com.swjtu.smec.common.result.CommonResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public CommonResult<?> exceptionHandler(Exception e) {
        System.err.println("[GlobalExceptionHandler] 捕获到异常: " + e.getMessage());
        e.printStackTrace();
        return CommonResult.error(
                GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR.getCode(),
                GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR.getMsg()
        );
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseBody
    public CommonResult<?> runtimeExceptionHandler(RuntimeException e) {
        System.err.println("[GlobalExceptionHandler] 运行时异常: " + e.getMessage());
        e.printStackTrace();
        return CommonResult.error(
                GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR.getCode(),
                e.getMessage() != null ? e.getMessage() : "服务器内部错误"
        );
    }
}
