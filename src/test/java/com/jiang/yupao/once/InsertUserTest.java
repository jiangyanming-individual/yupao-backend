package com.jiang.yupao.once;
import java.util.Date;

import com.google.common.base.Stopwatch;
import com.jiang.yupao.model.domain.User;
import com.jiang.yupao.service.UserService;
import org.apache.tomcat.util.threads.ThreadPoolExecutor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Lenovo
 * @date 2023/5/20
 * @time 15:16
 * @project yupao
 **/

@SpringBootTest
public class InsertUserTest {


    @Resource
    private UserService userService;

    //线程池的设置：(16默认的线程数,1000最大的线程池,10000,time弃用线程的时间,10000能有多少个任务)
    private ExecutorService executorService=new ThreadPoolExecutor(16,1000,10000, TimeUnit.MINUTES,new ArrayBlockingQueue<>(10000));

    @Test
    public void doConCurrencyInsertUser(){

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int INSERT_NUM=400000;

        int j=0;
        int batch_size=5000;

        List<CompletableFuture<Void>> futureList = new ArrayList<>();
        for (int i=0;i<INSERT_NUM / batch_size;i++){
            ArrayList<User> userList = new ArrayList<>();

            while (true){
                j++;
                User user = new User();
                user.setUserName("假数据");
                user.setUserAccount("fakedata");
                user.setAvatarUrl("https://tupian.qqw21.com/article/UploadPic/2020-8/20208522181014944.jpg");
                user.setGender(0);
                user.setUserPassword("123456789");
                user.setPhone("7845121654");
                user.setProfile("我就是我，不一样的");
                user.setEmail("1547877815@qq.com");
                user.setUserStatus(0);
                user.setUserRole(0);
                user.setPlanetCode("16");
                user.setTags("[]");
                userList.add(user);

                if (j % batch_size== 0){
                    break;
                }
            }
            //使用CompletableFuture 的方法开启多线程：
            //开启异步任务： userService.saveBatch(userList, batch_size); 分批插入，一次batch_size大小；
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                System.out.println("test:" + Thread.currentThread().getName());
                userService.saveBatch(userList, batch_size);
            }, executorService); // executorService自定义的线程池；
            futureList.add(future);
        }
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{})).join();
        stopWatch.stop();
        System.out.println(stopWatch.getLastTaskTimeMillis());
    }
}
