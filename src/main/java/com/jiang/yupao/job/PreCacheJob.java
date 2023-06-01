package com.jiang.yupao.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jiang.yupao.mapper.UserMapper;
import com.jiang.yupao.model.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 开启定时任务
 *
 */

@Component
@Slf4j
public class PreCacheJob {

    @Resource
    private UserMapper userMapper;

    @Resource
    private RedissonClient redissonClient;


    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    //重点用户：
    private List<Long> mainUserList= Arrays.asList(1l);

    @Scheduled(cron = "0 59 23 * * *") //设置每天的23：59：0更新
    public void doCacheRecommendUsers(){
        //加锁
        RLock lock = redissonClient.getLock("yupo:precachejob:docache:lock");
        try {
            //尝试获得锁，不用等待，因为只来获取一次锁，获取不到就直接走；(0，-1):看门狗的机制
            if (lock.tryLock(0,-1,TimeUnit.MILLISECONDS)) {
                System.out.println("getLock:"+Thread.currentThread().getId());
                //进行缓存预热：
                for (Long userId : mainUserList) {
                    //数据库中查询信息
                    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                    Page<User> userPage = userMapper.selectPage(new Page<>(1, 20), queryWrapper);
                    //设置redis的key
                    String redisKey = String.format("yupao:user:recommend:%s", userId);
                    ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
                    try {
                        //写缓存：设置缓存消失时间:
                        valueOperations.set(redisKey, userPage, 30000, TimeUnit.MILLISECONDS);
                    } catch (Exception e) {
                        log.error("redis set key error!");
                    }
                }
            }
        } catch (InterruptedException e) {
            log.error("doCacheRecommendUser error",e);
        }finally {
            //解锁,最后需要解锁：
            if (lock.isHeldByCurrentThread()){
                System.out.println("unlock:"+Thread.currentThread().getId());
                lock.unlock();
            }
        }
    }

}
