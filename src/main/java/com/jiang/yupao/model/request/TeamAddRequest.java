package com.jiang.yupao.model.request;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 *添加队伍请求体；去除自动生成的参数：
 */
@Data
public class TeamAddRequest {
    private static final long serialVersionUID = 1L;
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
