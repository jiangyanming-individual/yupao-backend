package com.jiang.yupao.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

import java.util.ArrayList;

/**
 * @author Lenovo
 * @date 2023/5/14
 * @time 14:07
 * @project yupao
 **/

@Configuration
@EnableSwagger2WebMvc
@Profile({"dev","test"})
public class Knife4jConfig {

    @Bean
    public Docket docket(){

        return new Docket(DocumentationType.SWAGGER_2).apiInfo(apiInfo())
                .enable(true) //配置是否启用Swagger，如果是false，在浏览器将无法访问
                .select()// 通过.select()方法，去配置扫描接口,RequestHandlerSelectors配置如何扫描接口
                .apis(RequestHandlerSelectors.basePackage("com.jiang.yupao.controller"))
                // 配置如何通过path过滤,即这里只扫描请求以/kuang开头的接口
                .paths(PathSelectors.any())//所有路径都满足:
                .build();
    }

    /**
     * 配置swaggerInfo信息:
     */
    public ApiInfo apiInfo(){
        //作者信息:
        Contact contact = new Contact("jiangyanming", "sxnu", "XXX@qq.com");
        return new ApiInfo(
                "伙伴匹配系统", //标签
                "每走一步都算数", //描述
                "1.0", //版本信息
                "https://blog.csdn.net/JEREMY_GYJ?spm=1000.2115.3001.5343", //组织连接
                contact, //联系人信息
                "Apache 2.0", //许可
                "https://blog.csdn.net/JEREMY_GYJ?spm=1000.2115.3001.5343", //许可连接
                new ArrayList() //扩展
        );
    }

}
