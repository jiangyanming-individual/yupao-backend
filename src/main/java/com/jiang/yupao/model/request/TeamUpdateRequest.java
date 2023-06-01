package com.jiang.yupao.model.request;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * 队伍更新请求体
 */
@Data
public class TeamUpdateRequest implements Serializable {
    private static final long serialVersionUID = -5168716182056171905L;

    /**
     * 主键id ：根据id来更新
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
//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
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
     * 密码
     */
    private String password;


}
