package com.jiang.yupao.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jiang.yupao.common.ErrorCode;
import com.jiang.yupao.exception.BusinessException;
import com.jiang.yupao.model.domain.User;
import com.jiang.yupao.mapper.UserMapper;
import com.jiang.yupao.service.UserService;
import com.jiang.yupao.utils.AlgorithmUtils;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.jiang.yupao.constant.UserConstant.ADMIN_ROLE;
import static com.jiang.yupao.constant.UserConstant.USER_LOGIN_STATE;

/**
* @author Lenovo
* @description 针对表【user】的数据库操作Service实现
* @createDate 2023-05-11 13:23:43
*/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService {

    //service调用dao层；
    @Resource
    private UserMapper userMapper;


    @Resource
    private RedisTemplate redisTemplate;

    //加入断言进行混淆加密
    private static final String SALT="jack"; //自己设置的断言；用于混淆加密；

    /**
     *  //注册的逻辑，
     *  参入参数userAccount，
     *  userPassword,
     *  checkPassword,
     *  planetCode星球编号
     * @param userAccount
     * @param userPassword
     * @param checkPassword
     * @param planetCode
     * @return
     */

    @Override
    public Long userRegister(String userAccount, String userPassword, String checkPassword, String planetCode){

        //(1)注册不合法
        //1. 非空 任意一个为“  ”，“”，null，都为true;
        if (StringUtils.isAnyBlank(userAccount,userPassword,checkPassword,planetCode)){
//            return (long) -1;
           throw new BusinessException(ErrorCode.NULL_ERROR,"注册参数为空");
        }

        //2. 账户长度 不小于 4 位
        if (userAccount.length()<4){
//            return (long) -1;
            throw new BusinessException(ErrorCode.PARAM_ERROR,"该账号长度不符合要求！");
        }

        //3. 密码就 不小于 8 位
        if (userPassword.length()<8){
            throw new BusinessException(ErrorCode.PARAM_ERROR,"密码不符合要求！");
        }
        //4.星球编号不能超过5
        if (planetCode.length()>5){
            throw new BusinessException(ErrorCode.PARAM_ERROR,"星球编号长度不符合要求！");
        }

        //5. 账户不包含特殊字符 (先校验字符合法性可以避免少查找数据库),使用正则匹配；

        String validRule="[`~!@#$%^&*()+=|{}':;',\\\\\\\\[\\\\\\\\].<>/?~！@#￥%……\n" +
                "&*（）——+|{}【】‘；：”“’。，、？]";

        Matcher matcher = Pattern.compile(validRule).matcher(userAccount);
        if (matcher.find()){
            throw new BusinessException(ErrorCode.PARAM_ERROR,"该账号含有特殊符号！");
        }

        //6. 密码和校验密码相同

        if (!userPassword.equals(checkPassword)){
            throw new BusinessException(ErrorCode.PARAM_ERROR,"两次密码不一致！");
        }

        //7. 账户不能重复;在数据库中查找是否存在相同的账户
        /**
         * QueryWrapper ：mybatis-plus中使用的语法；查找用户
         * **/
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount",userAccount);
        Long count = userMapper.selectCount(queryWrapper);

        if (count>0){
            throw new BusinessException(ErrorCode.PARAM_ERROR,"该账号已经存在！");
        }

        //8. 星球编号不能重复;在数据库中查找是否存在相同的编号
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("planetCode",planetCode);
        count = userMapper.selectCount(queryWrapper);

        if (count >0){
            throw new BusinessException(ErrorCode.PARAM_ERROR,"该星球编号已经存在！");
        }

        //(2) 注册合法，对密码加密
        String verifyPassword= DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes(StandardCharsets.UTF_8));

        //(3) 往数据库中插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(verifyPassword);
        user.setPlanetCode(planetCode);

        int res = userMapper.insert(user);
        if (res < 0){
            throw new BusinessException(ErrorCode.PARAM_ERROR,"插入数据失败！");
        }
        return user.getId();
    }

    /**
     * 登录逻辑
     * @param userAccount
     * @param userPassword
     * @param request Http请求
     * @return
     */
    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {

        //1. 校验用户账户和密码是否合法
        //1. 非空
        if (StringUtils.isAnyBlank(userAccount,userPassword)){
            throw new BusinessException(ErrorCode.NULL_ERROR,"请求参数为空！");
        }

        //2. 账户长度 不小于 4 位
        if (userAccount.length()<4){
            throw new BusinessException(ErrorCode.PARAM_ERROR,"账号长度不符合要求！");
        }

        //3. 密码就 不小于 8 位
        if (userPassword.length()<8){
            throw new BusinessException(ErrorCode.PARAM_ERROR,"密码长度不符合要求！");
        }
        //4. 账户不包含特殊字符，检验不合法的字符；
        String validRule="[`~!@#$%^&*()+=|{}':;',\\\\\\\\[\\\\\\\\].<>/?~！@#￥%……\n" +
                "&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validRule).matcher(userAccount);
        if (matcher.find()){
            throw new BusinessException(ErrorCode.PARAM_ERROR,"账号含有特殊符号！");
        }
        //2. 校验密码是否输入正确，要和数据库中的密文密码和账户进行比较
        String encodePassword=DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes(StandardCharsets.UTF_8));
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount",userAccount);
        queryWrapper.eq("userPassword",encodePassword);//加密后的密码和数据库中的进行对比；

        User user=userMapper.selectOne(queryWrapper);

        if (user == null){
            log.info("user login failed, userAccount Cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAM_ERROR,"没有该用户！！");
        }
        //3. 如果成功：给用户信息脱敏，隐藏敏感信息，防止数据库中的字段泄露;
        User saftyUser=getSaftyUser(user);

        //4. 我们要记录用户的登录态（session），将其存到服务器上（用后端 SpringBoot 框架封装的服务tomcat 去记录cookie
        request.getSession().setAttribute(USER_LOGIN_STATE,saftyUser);

        //5. 返回脱敏后的用户信息
        return saftyUser;
    }

    /**
     * 退出登录
     * @param request
     * @return
     */
    @Override
    public Integer userLogout(HttpServletRequest request) {

        request.getSession().removeAttribute(USER_LOGIN_STATE);

        return 1;
    }


    /**
     * 获取当前用户信息：
     * @param request
     * @return
     */
    @Override
    public User getCurrentUser(HttpServletRequest request) {

        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user=(User) userObj;

        if (user == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN,"用户未登录！");
        }
        //获取合法用户id：
        Long id = user.getId();
        //查询用户
        User newUser=userMapper.selectById(id);
        //用户信息脱敏：
        return getSaftyUser(newUser);

    }

    /**
     * 获取当前登录的用户：
     * @param request
     * @return
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {

        if (request == null){
            throw  new BusinessException(ErrorCode.PARAM_ERROR);
        }

        User user = (User)request.getSession().getAttribute(USER_LOGIN_STATE);
        if (user == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        return user;
    }

    /**
     * 用户信息脱敏：
     * @param OriginUser
     * @return
     */
    public User getSaftyUser(User OriginUser){

        if (OriginUser == null){
            throw new BusinessException(ErrorCode.PARAM_ERROR,"该用户不存在！");
        }
        User user=new User();
        user.setId(OriginUser.getId());
        user.setUserName(OriginUser.getUserName());
        user.setUserAccount(OriginUser.getUserAccount());
        user.setAvatarUrl(OriginUser.getAvatarUrl());
        user.setPlanetCode(OriginUser.getPlanetCode());
        user.setProfile(OriginUser.getProfile());
        user.setEmail(OriginUser.getEmail());
        user.setPhone(OriginUser.getPhone());
        user.setGender(OriginUser.getGender());
        user.setCreateTime(OriginUser.getCreateTime());
        user.setUserStatus(OriginUser.getUserStatus());
        user.setUserRole(OriginUser.getUserRole());
        user.setTags(OriginUser.getTags());

        return user;
    }

    /**
     * 判断是不是管理员
     * @param request
     * @return
     */
    @Override
    public Boolean isAdmin(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user=(User) userObj;

        if (user == null || user.getUserRole()!=ADMIN_ROLE){
            throw new BusinessException(ErrorCode.NO_AUTH,"不是管理员，无权限！");
        }
        return true;
    }

    /**
     * 判断当前登录用户是不是管理员：
     * @param loginUser
     * @return
     */
    @Override
    public Boolean isAdmin(User loginUser) {

        if (loginUser.getUserRole()!= ADMIN_ROLE){
            return false;
        }
        return true;
    }

    /**
     * 更新用户的信息
     * @param user
     * @param loginUser
     * @return
     */
    @Override
    public boolean updateUser(User user, User loginUser) {

        Long userId = user.getId();
        //用户id不合法：
        if (user==null || userId<=0){
            throw  new BusinessException(ErrorCode.PARAM_ERROR);
        }
        //权限校验：只有管理员或者 当前用户自己才可以修改信息： 如果当前用户不是管理员而且不是当前的登录用户，就直接报错；
        if (!isAdmin(loginUser) && userId!=loginUser.getId()){
            throw new BusinessException(ErrorCode.NO_AUTH,"没有权限修改用户信息");
        }
        //查找要修改的用户：
        User olderUser = this.getById(userId);
        //如果没有该用户：
        if (olderUser == null){
            throw  new BusinessException(ErrorCode.NULL_ERROR,"没有该用户！");
        }
        //更新用户，直接传入新的user；
        boolean result = this.updateById(user);
        if (!result){
            throw  new BusinessException(ErrorCode.SYSTEM_ERROR,"更新用户失败！");
        }
        return result;
    }

    /**
     * 根据用户名查询用户
     * @param username
     * @param request
     * @return
     */
    @Override
    public List<User> searchUsers(String username, HttpServletRequest request){

        //如果不是管理员的话：
        if (!isAdmin(request)){
            throw new BusinessException(ErrorCode.NO_AUTH,"不是管理员，无权限！");
        }

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();

        /**
         * 如果username不为空的情况下：进行模糊查询；
         */
        if (StringUtils.isNotBlank(username)){
            queryWrapper.like("username",username);
        }
        List<User> userList = userMapper.selectList(queryWrapper);
        //用户信息脱敏；stream转为list
        return userList.stream().map(user -> getSaftyUser(user)).collect(Collectors.toList());
    }

    /**
     * 推荐用户,返回list
     * @param request
     * @return
     */
    @Override
    public Page<User> recommendUsers(long pageSize, long pageNum, HttpServletRequest request) {

        //获取当前用户，方便设置key;
        User loginUser = getLoginUser(request);
        String redisKey=String.format("partner:user:recommend:%s",loginUser.getId());
        ValueOperations<String,Object> valueOperations = redisTemplate.opsForValue();
        //如果缓存存在查缓存，
        Page<User> userPage = (Page<User>)valueOperations.get(redisKey);
        if (userPage!=null){
            return userPage;
        }

        //缓存不存在查数据库：
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        userPage = userMapper.selectPage(new Page<>(pageNum, pageSize), queryWrapper);//分页
        //然后写缓存：
        try {
            //redis设置key-value时主要一定要设置过期时间：(设置超时时间)
            valueOperations.set(redisKey,userPage,30000, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.error("redis write error");
        }
        return userPage;
    }

    /**
     * 根据id查询删除用户
     * @param id
     * @param request
     * @return
     */
    @Override
    public Boolean deleteUser(Long id ,HttpServletRequest request){

        if (!isAdmin(request) || id <0){
            throw new BusinessException(ErrorCode.NO_AUTH,"不是管理员，无权限！");
        }
        return userMapper.deleteById(id) >0;
    }

    /**
     *
     *
     * 两种方法：（1）使用数据库查询、 （2）使用内存查询；
     * 根据标签查用户
     * @param tagNameList
     * @return
     */
    @Override
    public List<User> searchUserByTags(List<String> tagNameList){

        if (CollectionUtils.isEmpty(tagNameList)){
            throw new BusinessException(ErrorCode.PARAM_ERROR,"请求参数为空！");
        }
//        /**
//         * 进行模糊匹配查询
//         */
//        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
//        for (String tagName : tagNameList) {
//            queryWrapper = queryWrapper.like("tags",tagName);
//        }
//
//        List<User> userList = userMapper.selectList(queryWrapper);
//        /**
//         * 用户脱敏后，转为list
//         */
//        return userList.stream().map(user -> getSaftyUser(user)).collect(Collectors.toList());

        /**
         * 使用内存查询：
         */
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        List<User> userList = userMapper.selectList(queryWrapper);
        Gson gson = new Gson();

        //使用java8 stream()流 转为list
        return userList.stream().filter(user -> {
            String tagsStr=user.getTags();
            if (StringUtils.isBlank(tagsStr)){
                return false;
            }
            Set<String> tempTagNameSet=gson.fromJson(tagsStr,new TypeToken<Set<String>>(){
            }.getType());

            for (String tagName : tagNameList) {
                //如果Set中不含有名字就return false；
                if (!tempTagNameSet.contains(tagName)){
                    return false;
                }
            }
            return true;
        }).map(this::getSaftyUser).collect(Collectors.toList());
    }

    /**
     * 用户匹配；
     * @param num
     * @param loginUser
     * @return
     */
    @Override
    public List<User> matchUsers(long num, User loginUser) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "tags");
        queryWrapper.isNotNull("tags");
        List<User> userList = this.list(queryWrapper);
        String tags = loginUser.getTags();
        Gson gson = new Gson();
        List<String> tagList = gson.fromJson(tags, new TypeToken<List<String>>() {
        }.getType());
        // 用户列表的下标 => 相似度
        List<Pair<User, Long>> list = new ArrayList<>();
        // 依次计算所有用户和当前用户的相似度
        for (int i = 0; i < userList.size(); i++) {
            User user = userList.get(i);
            String userTags = user.getTags();
            // 无标签或者为当前用户自己
            if (StringUtils.isBlank(userTags) || user.getId().equals(loginUser.getId())) {
                continue;
            }
            List<String> userTagList = gson.fromJson(userTags, new TypeToken<List<String>>() {
            }.getType());
            // 计算分数
            long distance = AlgorithmUtils.minDistance(tagList, userTagList);
            list.add(new Pair<>(user, distance));
        }
        // 按编辑距离由小到大排序
        List<Pair<User, Long>> topUserPairList = list.stream()
                .sorted((a, b) -> (int) (a.getValue() - b.getValue()))
                .limit(num)
                .collect(Collectors.toList());
        // 原本顺序的 userId 列表
        List<Long> userIdList = topUserPairList.stream().map(pair -> pair.getKey().getId()).collect(Collectors.toList());
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.in("id", userIdList);
        // 1, 3, 2
        // User1、User2、User3
        // 1 => User1, 2 => User2, 3 => User3
        Map<Long, List<User>> userIdUserListMap = this.list(userQueryWrapper)
                .stream()
                .map(user -> getSaftyUser(user))
                .collect(Collectors.groupingBy(User::getId));
        List<User> finalUserList = new ArrayList<>();
        for (Long userId : userIdList) {
            finalUserList.add(userIdUserListMap.get(userId).get(0));
        }
        return finalUserList;
    }

}




