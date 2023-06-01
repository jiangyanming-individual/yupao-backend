package com.jiang.yupao.service;

import org.junit.jupiter.api.Test;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisClient;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.ArrayList;

/**
 * @author Lenovo
 * @date 2023/5/21
 * @time 11:46
 * @project yupao
 **/

@SpringBootTest
public class doRedissonTest {

    @Resource
    private RedissonClient redissonClient;

    @Test
    public void doTest(){
        //List。数据存在本地JVM内存中
        ArrayList<Object> list = new ArrayList<>();
        list.add("yupi");
        System.out.println("list: "+list.get(0));

        //数据存在redis的内存中
        RList<Object> rList = redissonClient.getList("test_list");
        rList.add("yupi");
        System.out.println("getlist:"+rList.get(0));
    }
}
