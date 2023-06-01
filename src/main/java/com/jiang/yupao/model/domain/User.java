package com.jiang.yupao.model.domain;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;


/**
 * 
 * @TableName user
 */
@Data
public class User implements Serializable {
    /**
     * 主键id
     */
    @TableId(type = IdType.AUTO)
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
     * 密码
     */
    private String userPassword;

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
     * 是否删除，0表示正常 1：表示删除
     */
    @TableLogic
    private Integer isDelete;

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

    private static final long serialVersionUID = 1L;


}