package com.jiang.yupao.model.request;

import lombok.Data;

/**
 *
 * 注册请求体
 * @author Lenovo
 * @date 2023/5/11
 * @time 13:35
 * @project yupao
 **/


@Data
public class UserRegisterRequest {

    private String userAccount;
    private String userPassword;
    private String checkPassword;
    private String planetCode;
}
