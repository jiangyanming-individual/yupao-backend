package com.jiang.yupao.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jiang.yupao.common.BaseResponse;
import com.jiang.yupao.common.ErrorCode;
import com.jiang.yupao.common.ResultUtils;
import com.jiang.yupao.exception.BusinessException;
import com.jiang.yupao.model.domain.User;
import com.jiang.yupao.service.UserService;
import io.swagger.models.auth.In;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import com.jiang.yupao.model.request.UserLoginRequest;
import com.jiang.yupao.model.request.UserRegisterRequest;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author Lenovo
 * @date 2023/5/11
 * @time 13:31
 * @project yupao
 **/

@RestController
@RequestMapping("/user")
//允许携带cookie：
@CrossOrigin(origins = "http://localhost:3000",allowCredentials = "true") //配置跨域的问题；
public class UserController {

    @Resource
    private UserService userService;

    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest){

        if (userRegisterRequest == null){
            throw new BusinessException(ErrorCode.NULL_ERROR,"注册参数为空！");
        }

        String userAccount=userRegisterRequest.getUserAccount();
        String userPassword=userRegisterRequest.getUserPassword();
        String checkPassword=userRegisterRequest.getCheckPassword();
        String planetCode=userRegisterRequest.getPlanetCode();
        //注册校验逻辑 如果有一个为空的话，就不涉及逻辑校验
        if (StringUtils.isAnyBlank(userAccount,userPassword,checkPassword,planetCode)){

            throw new BusinessException(ErrorCode.NULL_ERROR,"注册参数为空！");
        }
        Long register = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
        return ResultUtils.success(register); //注册成功；
    }


    @PostMapping("/login")
    public BaseResponse<User> UserLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {

        //如果登录参数为空，直接返回空；
        if (userLoginRequest == null){
            throw new BusinessException(ErrorCode.NULL_ERROR,"登录参数为空！");
        }

        String userAccount=userLoginRequest.getUserAccount();
        String userPassword=userLoginRequest.getUserPassword();

        //如果有一个参数为空，直接返回null;
        if (StringUtils.isAnyBlank(userAccount,userPassword)){
            throw new BusinessException(ErrorCode.NULL_ERROR,"登录参数有空！");
        }
        //如果都符合校验，直接调用servic层中的userLogin这个方法；
        User user=userService.userLogin(userAccount,userPassword,request);

        return ResultUtils.success(user);

    }

    /**
     * 退出登录
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public BaseResponse<Integer> userLogout(HttpServletRequest request){

        if (request == null){
            throw new BusinessException(ErrorCode.PARAM_ERROR,"请求参数异常！");
        }
        Integer integer = userService.userLogout(request);

        return ResultUtils.success(integer);
    }

    /**
     * 获取当前用户信息；
     * @param request
     * @return
     */
    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request){

        if (request == null){
            throw new BusinessException(ErrorCode.PARAM_ERROR,"请求参数异常！");
        }
        //返回脱敏后的用户信息：
        User user=userService.getCurrentUser(request);
        return ResultUtils.success(user);
    }


    /**
     * 根据用户名查找用户；必须是管理员才可以
     * @param username
     * @param request
     * @return
     */
    @GetMapping("/search")
    public BaseResponse<List<User>> searchUsers(@RequestBody String username,HttpServletRequest request){

        List<User> userList = userService.searchUsers(username, request);
        return ResultUtils.success(userList);
    }

    /**
     * 推荐用户的页面；返回一页数据：
     * @param
     * @param request
     * @return
     */
    @GetMapping("/recommend")
    public BaseResponse<Page<User>> recommendUsers(long pageSize,long pageNum,HttpServletRequest request){
        
        Page<User> userList = userService.recommendUsers(pageSize,pageNum,request);
        return ResultUtils.success(userList);
    }


    /**
     * 根据id删除用户；首先必须是管理员
     * @param id
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody Long id,HttpServletRequest request){

        System.out.println(id);
        Boolean res = userService.deleteUser(id, request);

        return ResultUtils.success(res);//删除成功；
    }


    @PostMapping("/update")
    public BaseResponse<Boolean> updateUser(@RequestBody User user ,HttpServletRequest request){
        //校验参数是否为空：
        if (user == null){
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        //todo 如果前端没有修改任何内容，直接不用发请求直接报错；

        //调用service：
        //获取当前登录的用户：
        User loginUser = userService.getLoginUser(request);
        boolean result= userService.updateUser(user, loginUser);
        return ResultUtils.success(result);//更新成功，返回
    }

    /**
     * 根据标签查询用户
     * @return
     * RequestParam：
     */
    @GetMapping("/search/tags")
    public BaseResponse<List<User>> searchUserByTags(@RequestParam(required = false) List<String> tagNameList){
        if (CollectionUtils.isEmpty(tagNameList)){
            //参数异常：
            throw  new BusinessException(ErrorCode.PARAM_ERROR,"参数异常！");
        }
        //根据标签查找用户：
        List<User> userList = userService.searchUserByTags(tagNameList);
        return ResultUtils.success(userList);
    }

    /**
     * 用户匹配：
     * @param num
     * @param request
     * @return
     */
    @GetMapping("/match")
    public BaseResponse<List<User>> matchUsers(long num,HttpServletRequest request){

        if (num <=0 || num >20){
            throw new BusinessException(ErrorCode.PARAM_ERROR,"参数异常");
        }

        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(userService.matchUsers(num,loginUser));
    }


}
