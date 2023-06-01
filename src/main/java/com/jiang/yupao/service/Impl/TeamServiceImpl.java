package com.jiang.yupao.service.Impl;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jiang.yupao.common.ErrorCode;
import com.jiang.yupao.exception.BusinessException;
import com.jiang.yupao.mapper.TeamMapper;
import com.jiang.yupao.model.domain.Team;
import com.jiang.yupao.model.domain.User;
import com.jiang.yupao.model.domain.UserTeam;
import com.jiang.yupao.model.dvo.TeamQuery;
import com.jiang.yupao.model.enums.TeamStatusEnums;
import com.jiang.yupao.model.request.JoinTeamRequest;
import com.jiang.yupao.model.request.TeamQuitRequest;
import com.jiang.yupao.model.request.TeamUpdateRequest;
import com.jiang.yupao.model.vo.TeamUserVo;
import com.jiang.yupao.model.vo.UserVo;
import com.jiang.yupao.service.TeamService;
import com.jiang.yupao.service.UserTeamService;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisClient;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
* @author Lenovo
* @description 针对表【team(队伍表)】的数据库操作Service实现
* @createDate 2023-05-21 19:40:42
*/
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
    implements TeamService {

    @Resource
    private UserTeamService userTeamService;

    @Resource
    private UserServiceImpl userService;

    @Resource
    private RedissonClient redissonClient;

    /**
     * 创建队伍
     * @param team
     * @param loginUser
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class) //开启事务
    public long addTeam(Team team, User loginUser) {
        // 1. 请求参数是否为空？
        if (team == null){
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        // 2. 是否登录，未登录不允许创建
        if (loginUser == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN,"没有登录");
        }
        //获取当前登录用户的id
        final Long userId = loginUser.getId();

        //  3. 校验信息
        // 1). 队伍人数 > 1 且 <= 20
        int maxNum = Optional.ofNullable(team.getMaxNum()).orElse(0);//如果人数为空直接赋值为0
        if (maxNum<0 || maxNum >20){
            throw new BusinessException(ErrorCode.PARAM_ERROR,"队伍人数不符合要求");
        }
        // 2). 队伍标题 <= 20 //队伍标题不能为空；
        String name = team.getName();
        if (StringUtils.isBlank(name) || name.length() >20){
            throw new BusinessException(ErrorCode.PARAM_ERROR,"队伍标题过长不符合要求");
        }
        // 3). 描述 <= 512
        String description = team.getDescription();
        if (StringUtils.isNotBlank(description) && description.length()>512){
            throw new BusinessException(ErrorCode.PARAM_ERROR,"队伍描述过长");
        }
        // 4). status 是否公开（int）不传默认为 0（公开）
        int status = Optional.ofNullable(team.getStatus()).orElse(0);
        TeamStatusEnums teamStatusEnums = TeamStatusEnums.getTeamStatusByValue(status);
        if (teamStatusEnums == null){
            throw new BusinessException(ErrorCode.PARAM_ERROR,"队伍状态不满足要求");
        }
        // 5). 如果 status 是加密状态，一定要有密码，且密码 <= 32
        String password = team.getPassword();
        //处于加密的状态：
        if (TeamStatusEnums.SECRET.equals(status)){
            //处于加密状态，密码为空，或者密码长度大于32
            if (StringUtils.isBlank(password) || password.length()>32){
                throw new BusinessException(ErrorCode.PARAM_ERROR,"密码设置不满足要求");
            }
        }
        // 6). 超时时间 > 当前时间
        Date expireTime = team.getExpireTime();
        if (new Date().after(expireTime)){
            throw new BusinessException(ErrorCode.PARAM_ERROR,"当前时间超过过期时间");
        }
        // 7). 校验用户最多创建 5 个队伍
        //todo 有bug 创建需要加锁
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId",userId);
        long hasTeamNum = this.count(queryWrapper);
        if (hasTeamNum >=5){
            throw new BusinessException(ErrorCode.PARAM_ERROR,"队伍数量超过5个");
        }

        // 4. 插入队伍信息到队伍表
        team.setId(null);
        team.setUserId(userId);//设置队长id；
        boolean result = this.save(team);
        Long teamId = team.getId(); //队伍的id
        if (!result || teamId == null){
            throw new BusinessException(ErrorCode.PARAM_ERROR,"创建队伍失败!");
        }
        // 5. 插入用户，队伍=> 队伍关系到关系表
        UserTeam userTeam = new UserTeam();
        userTeam.setTeamId(teamId);
        userTeam.setUserId(userId);
        userTeam.setJoinTime(new Date());
        result = userTeamService.save(userTeam);
        if (!result){
            throw new BusinessException(ErrorCode.PARAM_ERROR,"创建队伍失败！");
        }
        return teamId;
    }

    /**
     * 查询队伍信息
     * @param teamQuery
     * @param isAdmin
     * @return
     */
    @Override
    public List<TeamUserVo> listTeams(TeamQuery teamQuery, boolean isAdmin) {

        //新建一个Team的查询
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        //1 组合查询条件：
        if(teamQuery!=null){

            //(1)根据id来查
            Long id = teamQuery.getId();
            if (id!=null && id>0){
                queryWrapper.eq("id",id);
            }
            List<Long> idList = teamQuery.getIdList();
            if (CollectionUtils.isNotEmpty(idList)){
                queryWrapper.in("id",idList);
            }

            //(2)根据搜索关键词 searchText
            String searchText = teamQuery.getSearchText();
            if (StringUtils.isNotBlank(searchText)){
                //关键词不为空；
                queryWrapper.and(qw->qw.like("name",searchText).or().like("description",searchText));
            }
            //(3)根据队伍名称 name
            String name = teamQuery.getName();
            if (StringUtils.isNotBlank(name)){
                queryWrapper.eq("name",name);
            }
            //(4)根据描述：des
            String description = teamQuery.getDescription();
            if (StringUtils.isNotBlank(description)){
                queryWrapper.eq("description",description);
            }

            //(5)查询最大人数；maxNum
            Integer maxNum = teamQuery.getMaxNum();
            if (maxNum!=null && maxNum>0){
                queryWrapper.eq("maxNum",maxNum);
            }
            //(6)根据创建人来查userId
            Long userId = teamQuery.getUserId();
            if (userId!=null && userId>0){
                queryWrapper.eq("userId",userId);
            }
            //(7)根据状态来查
            Integer status = teamQuery.getStatus();
            //得到枚举的状态：
            TeamStatusEnums statusEnums = TeamStatusEnums.getTeamStatusByValue(status);
            if (statusEnums == null){
                //如果初始为空，直接赋值为公开的
                statusEnums=TeamStatusEnums.PUBLIC;
            }

//            (8)如果不是管理员而且不能查看别人私有的队伍
            if (!isAdmin && statusEnums.equals(TeamStatusEnums.PRIVATE)){
                throw new BusinessException(ErrorCode.NO_AUTH,"没有权限");
            }
            queryWrapper.eq("status",statusEnums.getValue());
        }
        //2 当前展示的是过期时间为时间为空或者expireTime >new Date()
        queryWrapper.and(qw->qw.gt("expireTime",new Date()).or().isNull("expireTime"));
        //查询队伍：
        List<Team> teamList = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(teamList)){
            return new ArrayList<>();
        }
        //3 关联查询创建人的信息
        List<TeamUserVo> teamUserVoList = new ArrayList<>();
        for (Team team : teamList) {
            Long userId = team.getUserId();
            if (userId == null){
                continue;
            }
            //查询到创建人的信息：
            User user = userService.getById(userId);
            TeamUserVo teamUserVo=new TeamUserVo();
            BeanUtils.copyProperties(team,teamUserVo);
            //用户信息脱敏
            if (user!=null){
                UserVo userVo=new UserVo();
                BeanUtils.copyProperties(user,userVo);
                teamUserVo.setCreateUserVo(userVo);
            }
            teamUserVoList.add(teamUserVo);
        }
        //返回TeamUserVoList
        // todo 关联查询队伍中所有用户的信息；
        return teamUserVoList;
    }

    /**
     * 更新队伍信息
     * @param teamUpdateRequest
     * @param loginUser
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser) {

        if (teamUpdateRequest == null){
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        Long teamId = teamUpdateRequest.getId();
        if (teamId == null || teamId<=0){
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        //队伍不存在
        Team oldTeam = this.getById(teamId);
        if (oldTeam == null){
            throw new BusinessException(ErrorCode.NULL_ERROR,"队伍不存在");
        }
        //只有管理员和当前创建队伍的队长才可以修改信息：
        if (loginUser.getId() !=oldTeam.getUserId() && !userService.isAdmin(loginUser)){
            throw new BusinessException(ErrorCode.NO_AUTH,"不是管理员或者队长");
        }
        //如果修改队伍为加密队伍： 必须要设置密码 ；而且设置密码长度不能超过32位;
        Integer status = teamUpdateRequest.getStatus();
        TeamStatusEnums statusEnums = TeamStatusEnums.getTeamStatusByValue(status);
        String password = teamUpdateRequest.getPassword();
        if (statusEnums.equals(TeamStatusEnums.SECRET)) {

            if (StringUtils.isBlank(password)){
                throw new BusinessException(ErrorCode.PARAM_ERROR,"设置为加密队伍必须要设置密码");
            }
            //密码长度不能超过32位
            if (StringUtils.isNotBlank(password) && password.length()>32){
                throw new BusinessException(ErrorCode.PARAM_ERROR,"密码长度不能超过32位");
            }
        }
        Team updateTeam = new Team();
        BeanUtils.copyProperties(teamUpdateRequest,updateTeam);
        //更新队伍信息：
        boolean result = this.updateById(updateTeam);
        return result;
    }
    /**
     * 用户去加入队伍：
     * @param joinTeamRequest
     * @param loginUser
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean joinTeam(JoinTeamRequest joinTeamRequest, User loginUser) {

        if (joinTeamRequest == null){
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        //1. 参数异常：
        Long teamId = joinTeamRequest.getTeamId();
        if (teamId == null ||teamId <=0){
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        //2. 队伍不存在
        Team team = this.getById(teamId);
        if (team == null){
            throw new BusinessException(ErrorCode.NULL_ERROR,"队伍不存在");
        }

        //2. 队伍已经过期：
        Date expireTime = team.getExpireTime();
        if (expireTime !=null && expireTime.before(new Date())){
            throw new BusinessException(ErrorCode.PARAM_ERROR,"队伍已经过期");
        }
        // 3.禁止加入私有队伍
        Integer status = team.getStatus();
        TeamStatusEnums statusEnums= TeamStatusEnums.getTeamStatusByValue(status);
        if (TeamStatusEnums.PRIVATE.equals(statusEnums)){
            throw new BusinessException(ErrorCode.NO_AUTH,"禁止加入私有队伍");
        }

        //4. 加入加密的队伍，需要验证密码：
        String password = joinTeamRequest.getPassword();
        if (TeamStatusEnums.SECRET.equals(statusEnums)){
            //如果密码是空；或者密码不等于加密密码，抛出异常；
            if (StringUtils.isBlank(password) || !password.equals(team.getPassword())){
                throw new BusinessException(ErrorCode.PARAM_ERROR,"密码错误");
            }
        }
        Long userId = loginUser.getId();
        //todo 加入队伍加锁：
        // 6.查询该用户加入的队伍个数 <=5
        //只有一个线程能拿到锁：
        RLock lock = redissonClient.getLock("yupao:join_team:lock");
        try {
            //抢到锁就执行；(0，-1):看门狗的机制
            while (true){
                if (lock.tryLock(0,-1, TimeUnit.MILLISECONDS)) {
                    System.out.println("getLock:"+Thread.currentThread().getId());
                    //执行业务逻辑：
                    QueryWrapper<UserTeam>  userTeamQueryWrapper= new QueryWrapper<>();
                    userTeamQueryWrapper.eq("userId",userId);
                    long hasJoinNum = userTeamService.count(userTeamQueryWrapper);
                    //统计用户加入了几个队伍
                    if (hasJoinNum>5){
                        throw new BusinessException(ErrorCode.PARAM_ERROR,"创建和加入的队伍最多5个");
                    }
                    //7.同一个用户不能重复加入同一个队伍
                    userTeamQueryWrapper=new QueryWrapper<>();
                    userTeamQueryWrapper.eq("userId",userId);
                    userTeamQueryWrapper.eq("teamId",teamId);
                    long hasJoinNumTeam= userTeamService.count(userTeamQueryWrapper);
                    if (hasJoinNumTeam>0){
                        throw new BusinessException(ErrorCode.PARAM_ERROR,"不能重复加入同一个队伍");
                    }
                    //8.队伍满了，不能加入了
                    userTeamQueryWrapper=new QueryWrapper<>();
                    userTeamQueryWrapper.eq("teamId",teamId);
                    long hasJoinNumPerson = userTeamService.count(userTeamQueryWrapper);
                    if (hasJoinNumPerson >=team.getMaxNum()){
                        throw new BusinessException(ErrorCode.PARAM_ERROR,"队伍已经满了");
                    }
                    //9 修改user-team表：保存用户队伍的信息(设置用户id,队伍Id,加入的时间)
                    UserTeam userTeam = new UserTeam();
                    userTeam.setUserId(userId);
                    userTeam.setTeamId(teamId);
                    userTeam.setJoinTime(new Date());
                    boolean result = userTeamService.save(userTeam);
                    return result;
                }
            }
        } catch (InterruptedException e) {
            log.error("doCacheRecommendUser error",e);
            return false;
        }finally {
            //解锁,最后需要自己释放锁：
            if (lock.isHeldByCurrentThread()){
                System.out.println("unlock:"+Thread.currentThread().getId());
                lock.unlock();
            }
        }
    }
    /**
     * 用户退出：
     * @param teamQuitRequest
     * @param loginUser
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser) {
        // 1.校验请求
        if (teamQuitRequest == null){
            throw new BusinessException(ErrorCode.PARAM_ERROR,"请求参数异常");
        }
        // 2.校验队伍是否存在
        Long teamId = teamQuitRequest.getTeamId();
        Team team= getTeamById(teamId);
        // 3.校验自己是否加入队伍(userTeam);//查询自己是否在队伍中
        Long userId = loginUser.getId();
        UserTeam queryUserTeam = new UserTeam();
        queryUserTeam.setUserId(userId);
        queryUserTeam.setTeamId(teamId);
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>(queryUserTeam);
        long count = userTeamService.count(userTeamQueryWrapper);
        if (count == 0){
            throw new BusinessException(ErrorCode.PARAM_ERROR,"没有加入队伍");
        }

        long teamhasJoinNum = countTeamUserByTeamId(teamId);
        // 4.如果队伍还有一人，解散队伍
        if (teamhasJoinNum == 1){
//            userTeamQueryWrapper = new QueryWrapper<>();
//            userTeamQueryWrapper.eq("userId",userId);
//            userTeamQueryWrapper.eq("teamId",teamId);
//            //移除用户队伍信息：
//            boolean reslut = userTeamService.remove(userTeamQueryWrapper);
//            if (!reslut){
//                throw new BusinessException(ErrorCode.PARAM_ERROR,"解散队伍失败！");
//            }
            //删除队伍表的信息
            this.removeById(teamId);
        }else{
            // 5.还有其他人>=2(1.如果是队长退出权限转移给第二个加入队伍的人, 2.普通队员直接退出);

            if (team.getUserId()==loginUser.getId()){
                //退出的人是队长：查询最早加入队伍的所有用户和时间
                QueryWrapper<UserTeam> userTeamQueryWrapper2 = new QueryWrapper<>();
                userTeamQueryWrapper2.eq("teamId",teamId);
                userTeamQueryWrapper2.last("order by id asc limit 2");//查询两条；按id升序的结果
                List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper2);
                if (CollectionUtils.isEmpty(userTeamList) || userTeamList.size()<=1){
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR,"用户队伍不存在");
                }
                UserTeam nextUserTeam = userTeamList.get(1);
                Long nextLeaderId = nextUserTeam.getUserId();

                //更新新的队长
                Team updateTeam=new Team();
                updateTeam.setId(teamId);//保持旧队伍id
                updateTeam.setUserId(nextLeaderId);
                boolean result = this.updateById(updateTeam);
                if (!result){
                    throw new BusinessException(ErrorCode.PARAM_ERROR,"更换队伍长失败");
                }
            }
        }
        //移除用户队伍信息表的内容；
        return userTeamService.remove(userTeamQueryWrapper);
    }

    /**
     * 删除队伍
     * @param id
     * @param loginUser
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTeam(long id, User loginUser) {

        //校验队伍是否存在
        Team team = getTeamById(id);
        Long teamId = team.getId();
        //校验你是不是队伍的队长；
        if ((long)loginUser.getId()!=(long)team.getUserId()){
            //如果当前用户不是队长；没有权限删除队伍
            throw new BusinessException(ErrorCode.NO_AUTH,"没有权限删除队伍");
        }
        //移除所有与队伍关联的用户信息；
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId",teamId);
        boolean result = userTeamService.remove(userTeamQueryWrapper);
        if (!result){
            throw new BusinessException(ErrorCode.PARAM_ERROR,"删除队伍失败！");
        }
        //移除队伍信息; 根据队伍id来删除
        return this.removeById(teamId);
    }

    /**
     * 根据用户id查询用户：
     * @param teamId
     * @return
     */
    @Override
    public Team getTeamById(Long teamId){
        if (teamId == null || teamId <=0){
            throw  new BusinessException(ErrorCode.PARAM_ERROR);
        }
        Team team = this.getById(teamId);
        if (team == null){
            throw  new BusinessException(ErrorCode.NULL_ERROR,"队伍不存在");
        }
        return team;
    }

    /**
     * 获取当前队伍的数量
     * @return
     */
    public long countTeamUserByTeamId(long teamId){
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId",teamId);
        long result = userTeamService.count(userTeamQueryWrapper);
        return result;
    }

}





