package com.jiang.yupao.model.vo;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 返回用户信息封装类，用于用户信息的脱敏；
 */
@Data
public class UserVo implements Serializable {

    private static final long serialVersionUID = 4886000989195477899L;

    private Long id;
    /**
     * 用户名
     */
    private String userName;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 头像
     */
    private String avatarUrl;

    /**
     * 性别 ：0:表示男
     */
    private Integer gender;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */

    private String profile;

    private String email;

    /**
     * 状态 0：表示正常
     */
    private Integer userStatus;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 用户权限 0:表示普通用户；1:表示管理员
     */
    private Integer userRole;

    /**
     * 星球编码
     */
    private String planetCode;

    /**
     * 标签列表
     */
    private String tags;

}
