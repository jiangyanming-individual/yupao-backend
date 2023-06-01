package com.jiang.yupao.model.enums;

/**
 * 队伍状态的枚举类：
 */
public enum TeamStatusEnums {
    /**
     * 公开
     */
    PUBLIC(0,"公开"),
    /**
     *私有
     */
    PRIVATE(1,"私有"),
    /**
     * 加密
     */
    SECRET(2,"加密");


    private int value;
    private String text;

    /**
     * 根据value获取枚举对象；
     */
    public static TeamStatusEnums getTeamStatusByValue(Integer value){
        //使用包装类
        if (value == null){
            return null;
        }
        /**
         * 遍历所有的枚举值：如果跟value相等直接返回整个枚举对象；
         */
        TeamStatusEnums[] teamStatusEnums = TeamStatusEnums.values();
        for (TeamStatusEnums teamStatusEnum : teamStatusEnums) {

            if (teamStatusEnum.getValue() == value) {
                return teamStatusEnum;
            }
        }
        return null;
    }

    TeamStatusEnums(int value, String text) {
        this.value = value;
        this.text = text;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
