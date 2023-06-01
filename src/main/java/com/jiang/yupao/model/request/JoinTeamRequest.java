package com.jiang.yupao.model.request;
import lombok.Data;

import java.io.Serializable;

/**
 * 请求加入对请求体
 */
@Data
public class JoinTeamRequest implements Serializable {

    private static final long serialVersionUID = -3236026718054233339L;

    private Long teamId;
    private String password;

}
