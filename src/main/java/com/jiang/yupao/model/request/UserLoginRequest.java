package com.jiang.yupao.model.request;

import lombok.Data;

/**
 *
 *登录请求体
 * @author Lenovo
 * @date 2023/5/11
 * @time 13:35
 * @project yupao
 **/


//加上Lomback注解
@Data
public class UserLoginRequest {


    private String userAccount;
    private String userPassword;

}
