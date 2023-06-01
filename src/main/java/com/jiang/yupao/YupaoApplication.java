package com.jiang.yupao;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@MapperScan("com.jiang.yupao.mapper") //启动类必须要加扫描mapper文件；
@EnableScheduling //引入定时任务；
public class YupaoApplication {

    public static void main(String[] args) {
        SpringApplication.run(YupaoApplication.class, args);
    }

}
