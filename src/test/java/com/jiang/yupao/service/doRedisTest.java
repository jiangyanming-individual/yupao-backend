package com.jiang.yupao.service;

import com.jiang.yupao.model.domain.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.annotation.Resource;

/**
 * @author Lenovo
 * @date 2023/5/20
 * @time 19:13
 * @project yupao
 **/

@SpringBootTest
public class doRedisTest {

    @Resource
    private RedisTemplate redisTemplate;

    @Test
    public void Test(){
        ValueOperations valueOperations = redisTemplate.opsForValue();
//        增
        valueOperations.set("yupiString","dog");
        valueOperations.set("yupiInt",1);
        valueOperations.set("yupiDouble",2.0);
        User user = new User();
        user.setId(1L);
        user.setUserName("yupi");
        valueOperations.set("yupiUser",user);
        //查
        Object yupi = valueOperations.get("yupiString");
        Assertions.assertTrue("dog".equals((String)yupi));
        yupi = valueOperations.get("yupiInt");
        Assertions.assertTrue(1==((Integer)yupi));
        yupi = valueOperations.get("yupiDouble");
        Assertions.assertTrue(2.0==((Double)yupi));
        System.out.println(valueOperations.get("yupiUser"));
        //删除
//        valueOperations.set("yupiString","dog");
//        redisTemplate.delete("yupiString");
    }
}
