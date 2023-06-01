package com.jiang.yupao.model.request;


import lombok.Data;

import java.io.Serializable;

/**
 * 封装的删除队伍请求体
 */
@Data
public class DeleteTeamRequest implements Serializable {

    private static final long serialVersionUID = 9203861271831882405L;

    private long teamId;
}
