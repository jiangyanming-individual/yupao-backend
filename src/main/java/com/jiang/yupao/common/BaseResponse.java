package com.jiang.yupao.common;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Lenovo
 * @date 2023/5/11
 * @time 17:04
 * @project yupao
 **/


/**
 * 自定义返回类
 * @param <T>
 */
@Data
public class BaseResponse<T> implements Serializable {

    private int code;
    private T data;
    private String message;
    private String desc;


    public BaseResponse(){

    }

    /**
     * 传入code data message desc;
     * @param code
     * @param data
     * @param message
     * @param desc
     */
    public BaseResponse(int code, T data, String message, String desc) {
      this.code=code;
      this.data=data;
      this.message=message;
      this.desc=desc;
    }

    /**
     * 只传入code data message
     * @param code
     * @param data
     * @param message
     */
    public BaseResponse(int code, T data,String message) {
        this(code,data,message,"");
    }


    /**
     * 只传入code data
     * @param code
     * @param data
     */
    public BaseResponse(int code, T data) {
      this(code,data,"","");
    }

    /**
     * 传入自定定义的错误码
     * @param errorCode
     */
    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(),null,errorCode.getMessage(),errorCode.getDesc());
    }
}
