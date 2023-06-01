package com.jiang.yupao.common;


/**
 * 自定义返回工具类:
 */
public class ResultUtils {

    /**
     * 定义的泛型方法 成功
     * @param <T>
     * @return
     */
    public static <T> BaseResponse<T> success(T data){
        return new BaseResponse<>(0,data,"OK");
    }


    /**
     * 定义的泛型方法 失败 自定义 code,message,desc
     * @param
     * @return
     */
    public static  BaseResponse error(int code,String message,String desc){
        return new BaseResponse(code,null,message,desc);
    }

    /**
     定义的泛型方法 失败 自定义 message,desc
     * @param errorCode
     * @param message
     * @param desc
     * @return
     */
    public static BaseResponse error(ErrorCode errorCode,String message,String desc){
        return new BaseResponse(errorCode.getCode(),null,message,desc);
    }
    /**
     定义的泛型方法 失败 自定义 message,desc
     * @param errorCode
     * @param
     * @param desc
     * @return
     */
    public static BaseResponse error(ErrorCode errorCode,String desc){
        return new BaseResponse(errorCode.getCode(),null,errorCode.getMessage(),desc);
    }
}
