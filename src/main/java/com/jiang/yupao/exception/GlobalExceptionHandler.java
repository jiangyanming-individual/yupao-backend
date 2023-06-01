package com.jiang.yupao.exception;

import com.jiang.yupao.common.BaseResponse;
import com.jiang.yupao.common.ErrorCode;
import com.jiang.yupao.common.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author Lenovo
 * @date 2023/5/11
 * @time 17:36
 * @project yupao
 **/

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {


    /**
     * 捕获BusinessException处理类
     * @param e
     * @return
     */
    @ExceptionHandler(BusinessException.class)
    public BaseResponse businessExceptionHandler(BusinessException e){
        log.error("BusinessException:"+e.getMessage(),e);

        return ResultUtils.error(e.getCode(),e.getMessage(),e.getDesc());
    }

    /**
     *运行时异常,系统异常;
     * @param e
     * @return
     */
    @ExceptionHandler(RuntimeException.class)
    public BaseResponse runtimeExceptionHandler(RuntimeException e){
        log.error("RuntimeException:",e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR,e.getMessage(),"");
    }
}
