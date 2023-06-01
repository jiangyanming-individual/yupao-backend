package com.jiang.yupao.common;

import lombok.Data;
import java.io.Serializable;

/**
 * @author Lenovo
 * @date 2023/5/21
 * @time 21:04
 * @project yupao
 **/
@Data
public class RequestPages implements Serializable {

    private static final long serialVersionUID = 7398537658016678956L;

    protected int pageSize=10;

    protected int pageNum=1;


}
