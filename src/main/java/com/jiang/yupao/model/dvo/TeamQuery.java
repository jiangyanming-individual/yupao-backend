package com.jiang.yupao.model.dvo;
import com.jiang.yupao.common.RequestPages;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 *
 *请求参数包装类;有些字段是前端不关心的；
 */

@Data
@EqualsAndHashCode(callSuper=true)
public class TeamQuery extends RequestPages {

    /**
     * 主键队伍的id
     */
    private Long id;

    /**
     * 根据加入队伍的userId列表
     */
    private List<Long> idList;

    /**
     * 队伍名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 搜索关键词： 前端传入searchText参数
     */
    private String searchText;

    /**
     * 最大人数
     */
    private Integer maxNum;

    /**
     * 用户id(队长的id)
     */
    private Long userId;

    /**
     * 状态 0：公开，1：私有;2：加密
     */
    private Integer status;
}
