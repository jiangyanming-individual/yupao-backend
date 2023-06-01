package com.jiang.yupao.exception;

import com.jiang.yupao.common.ErrorCode;

/**
 *
 *
 * 定义业务异常类
 */
public class BusinessException extends RuntimeException{


    private final int code;
    private final String desc;

    /**
     * 构造方法：
     * @param message
     * @param code
     * @param desc
     */
    public BusinessException(String message, int code, String desc) {
        super(message);
        this.code = code;
        this.desc = desc;
    }

    /**
     * 传入ErrorCode类型
     * 自定义错误code和desc;
     * @param errorCode
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.desc = errorCode.getDesc();
    }

    /**
     * 自定义描述
     * @param errorCode
     * @param desc
     */
    public BusinessException(ErrorCode errorCode,String desc) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
