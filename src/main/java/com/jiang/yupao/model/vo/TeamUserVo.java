package com.jiang.yupao.model.vo;


import lombok.Data;
import java.io.Serializable;
import java.util.Date;

/**
 * 返回队伍用户信息封装类： 用于队伍、用户的信息脱敏；
 */
@Data
public class TeamUserVo implements Serializable {

    private static final long serialVersionUID = -3035532302164791385L;
    /**
     * 主键id
     */
    private Long id;

    /**
     * 队伍名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 最大人数
     */
    private Integer maxNum;

    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 用户id(队长的id)
     */
    private Long userId;

    /**
     * 状态 0：公开，1：私有;2：加密
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 创建人的信息：
     */
    private UserVo createUserVo;

    /**
     * 已经加入的用户人数
     */
    private Integer hasJoinNum;
    /**
     * 是否加入
     */
    private boolean hasJoin=false;
}
