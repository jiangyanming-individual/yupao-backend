package com.jiang.yupao.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jiang.yupao.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import java.awt.print.Book;
import java.util.List;

/**
* @author Lenovo
* @description 针对表【user】的数据库操作Service
* @createDate 2023-05-11 13:23:43
*/
public interface UserService extends IService<User> {

    //注册的逻辑，参入参数userAccount，userPassword, checkPassword,planetCode星球编号
    Long userRegister(String userAccount, String userPassword, String checkPassword, String planetCode);

    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    Integer userLogout(HttpServletRequest request);

    User getCurrentUser(HttpServletRequest request);

    User getLoginUser(HttpServletRequest request);
    Boolean isAdmin(HttpServletRequest request);
    Boolean isAdmin(User loginUser);

    boolean updateUser(User user,User loginUser);

    List<User> searchUsers(String username, HttpServletRequest request);
    Page<User> recommendUsers(long pageSize, long pageNum, HttpServletRequest request);

    Boolean deleteUser(Long id, HttpServletRequest request);

    List<User> searchUserByTags(List<String> tagNameList);

    List<User> matchUsers(long num, User loginUser);

}
