package com.qian.exception;

import com.qian.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.exceptions.TooManyResultsException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<String> handleBusinessException(BusinessException e) {
        log.error("业务异常：{}", e.getMessage());

        return Result.clientError("操作失败");
    }

    @ExceptionHandler(AuthenticationFailureException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<String> handleAuthenticationException(AuthenticationFailureException e) {
        log.error("认证异常：{}", e.getMessage());

        return Result.clientError("认证失败");
    }

    @ExceptionHandler(InvalidOperationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<String> handleOperationException(InvalidOperationException e) {
        log.error("操作异常：{}", e.getMessage());

        return Result.clientError("非法操作");
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<String> handleResourceNotFoundException(ResourceNotFoundException e) {
        log.error("找不到资源：{}", e.getMessage());

        return Result.clientError("资源不存在");
    }

    //Mybatis-Plus 查询极小概率抛出
    @ExceptionHandler(TooManyResultsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<String> handleTooManyResultsException(TooManyResultsException e) {
        log.error("查询异常：{}", e.getMessage());

        return Result.clientError("查询失败");
    }

}
