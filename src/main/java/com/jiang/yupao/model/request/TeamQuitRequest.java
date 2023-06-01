package com.jiang.yupao.model.request;
import lombok.Data;
import java.io.Serializable;

/**
 * 用户退出请求体：
 */
@Data
public class TeamQuitRequest implements Serializable {
    private static final long serialVersionUID = -5880662556889317152L;

    private long teamId;
}
