package com.jiang.yupao.service;

import com.jiang.yupao.model.domain.User;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

/**
 * @author Lenovo
 * @date 2023/5/10
 * @time 19:57
 * @project yupao
 **/

@SpringBootTest
public class YuPaoServiceTest {

    @Autowired
    private UserService userService;
    private HttpServletRequest request;


    /**
     * 测试根据标签查询相匹配的用户
     */
    @Test
    public void testSearchUserByTagName(){
        List<String> tagNameList = Arrays.asList( "python");
        List<User> userList = userService.searchUserByTags(tagNameList);
        Assert.assertNotNull(userList);
    }

    /**
     * 测试登录
     */
    @Test
    public void testUserLogin(){

        String userAccount="";
        String userPassword="";
        User user=userService.userLogin(userAccount,userPassword,request);
        System.out.println("测试的结果:" + user);

    }

    //import org.junit.jupiter.api.Test的测试单元

    /**
     *
     *注册：
     */
    @Test
    public void  testRegister(){
        String userAccount="jack";
        String userPassword="";
        String checkPassword="123456789";
        String planetCode="1";

//        Long reslut = userService.userRegister(userAccount, userPassword, checkPassword,planetCode);
//        System.out.println("result:"+reslut);


//        userAccount="yu";
//        userPassword="123456789";
//        Long accountResult = userService.userRegister(userAccount, userPassword, checkPassword,planetCode);
//        System.out.println("accountResult:"+accountResult);

//        userAccount="yupi";
//        userPassword="123";
//        Long passwordResult = userService.userRegister(userAccount, userPassword, checkPassword,planetCode);
//        System.out.println("passwordResult:"+passwordResult);
////
        //校验特殊的字符：
//        userAccount="yi@@@@pi";
//        userPassword="123456789";
//        Long validResult = userService.userRegister(userAccount, userPassword, checkPassword,planetCode);
//        System.out.println("validResult:"+validResult);
//
//
//        //校验两次密码
//        userAccount="jack";
//        userPassword="1234567810";
//        Long checkPasswordResult = userService.userRegister(userAccount, userPassword, checkPassword,planetCode);
//        System.out.println("checkPasswordResult:"+checkPasswordResult);
//
//        //账户不能重复：
//        userAccount="jack";//原始的账户名是123456
//        userPassword="123456789";
//        Long checkUserAccount = userService.userRegister(userAccount, userPassword, checkPassword,planetCode);
//        System.out.println("checkUserAccount:"+checkUserAccount);
//
        //星球编号不能过长：
//        userAccount="yipi";
//        userPassword="123456789";
//        checkPassword="123456789";
//        planetCode="100001";
//        Long insertResult01 = userService.userRegister(userAccount, userPassword, checkPassword,planetCode);
//        System.out.println("插入的结果是；"+insertResult01);

//        //星球编号不能重复：
//        userAccount="yipi";
//        userPassword="123456789";
//        checkPassword="123456789";
//        planetCode="1";
//        Long insertResult02= userService.userRegister(userAccount, userPassword, checkPassword,planetCode);
//        System.out.println("插入的结果是；"+insertResult02);

//
        //真正插入成功2：
        userAccount="yupiadmin";
        userPassword="123456789";
        checkPassword="123456789";
        planetCode="3";
        Long insertResult03= userService.userRegister(userAccount, userPassword, checkPassword,planetCode);
        System.out.println("插入的结果是；"+insertResult03);
    }
}
