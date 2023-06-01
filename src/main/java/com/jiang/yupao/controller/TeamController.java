package com.jiang.yupao.controller;

import com.alibaba.druid.support.jconsole.model.ColumnGroup;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jiang.yupao.common.BaseResponse;
import com.jiang.yupao.common.ErrorCode;
import com.jiang.yupao.common.ResultUtils;
import com.jiang.yupao.exception.BusinessException;
import com.jiang.yupao.model.domain.Team;
import com.jiang.yupao.model.domain.User;
import com.jiang.yupao.model.domain.UserTeam;
import com.jiang.yupao.model.dvo.TeamQuery;
import com.jiang.yupao.model.request.*;
import com.jiang.yupao.model.vo.TeamUserVo;
import com.jiang.yupao.service.TeamService;
import com.jiang.yupao.service.UserService;
import com.jiang.yupao.service.UserTeamService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

/**
 *
 * 进行操作队伍增删改查的的接口：
 */


@RestController
@RequestMapping("/team")
//允许携带cookie：
@CrossOrigin(origins = "http://localhost:3000",allowCredentials = "true") //配置跨域的问题；
public class TeamController {

    @Resource
    private UserService userService;

    @Resource
    private TeamService teamService;

    @Resource
    private UserTeamService userTeamService;
    private TeamQuery teamQuery;

    /**
     * 创建一个队伍
     * @param teamAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addTeam(@RequestBody TeamAddRequest teamAddRequest, HttpServletRequest request){
        /**
         * 如果队伍不存在，报参数异常：
         */
        if (teamAddRequest == null){
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        //获取当前用户：
        User loginUser = userService.getLoginUser(request);
        Team team=new Team();
        BeanUtils.copyProperties(teamAddRequest,team);
        //这里不用再校验teamId是否为空了，因为已经再TeamService中已经校验过了
        long teamId = teamService.addTeam(team, loginUser);
        return ResultUtils.success(teamId);
    }

    /**
     * 删除队伍
     * @param
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTeam(@RequestBody DeleteTeamRequest deleteTeamRequest, HttpServletRequest request){
        /**
         * 如果id不存在
         */
        long id = deleteTeamRequest.getTeamId();
        if (deleteTeamRequest==null || id <=0){
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.deleteTeam(id,loginUser);
        if (!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"删除队伍失败！");
        }
        return ResultUtils.success(true);
    }

    /**
     * 更新队伍信息
     * @param
     * @return
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateTeam(@RequestBody TeamUpdateRequest teamUpdateRequest,HttpServletRequest request){
        /**
         * 如果请求体为空的情况下，报参数异常：
         */
        if (teamUpdateRequest == null){
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        User loginUser = userService.getLoginUser(request);

        boolean result = teamService.updateTeam(teamUpdateRequest,loginUser);
        if (!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"更新队伍失败！");
        }
        return ResultUtils.success(true);
    }

    /**
     * 获取单个队伍的信息
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<Team> getTeamById(long id){
        if (id <=0){
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        Team team = teamService.getTeamById(id);
        return ResultUtils.success(team);
    }

    /**
     * 进行队伍列表查询
     * @param teamQuery
     * @return
     */
    @GetMapping("/list")
    public BaseResponse<List<TeamUserVo>> listTeams(TeamQuery teamQuery, HttpServletRequest request){
        if (teamQuery == null){
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        Boolean isAdmin = userService.isAdmin(loginUser);
        //1 拿到所有的teamList: 查询所有的队伍；
        List<TeamUserVo> teamList = teamService.listTeams(teamQuery, isAdmin);
        //2 判断当前用户是否加入,拿到teamId
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        List<Long> teamIdList = teamList.stream().map(TeamUserVo::getId).collect(Collectors.toList());
        try {
            //查到当前用户加入的队伍
            userTeamQueryWrapper.eq("userId",loginUser.getId());
            userTeamQueryWrapper.in("teamId",teamIdList);
            List<UserTeam> userteamList = userTeamService.list(userTeamQueryWrapper);
            //已加入的队伍id ==>set集合：
            Set<Long> hasJoinTeamSet = userteamList.stream().map(UserTeam::getTeamId).collect(Collectors.toSet());
            //当前用户是不是已经加入该队伍

            //遍历所有集合，然后将hasJoin设置true;
            teamList.forEach(teamUserVo->{
                boolean hasJoin=hasJoinTeamSet.contains(teamUserVo.getId());
                teamUserVo.setHasJoin(hasJoin); //如果加入team设置为true;
            });

        }catch (Exception e){}

        //3查询已经加入的人数： 设置hasJoinNum
        QueryWrapper<UserTeam> userHasJoinTeamNumWrapper = new QueryWrapper<>();
        userHasJoinTeamNumWrapper.in("teamId",teamIdList);
        List<UserTeam> userTeamList = userTeamService.list(userHasJoinTeamNumWrapper);

        //key队伍id： value:List<UserTeam>;使用stream流方式：
        Map<Long,List<UserTeam>> teamIdUserTeamList=userTeamList.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));
        teamList.forEach(teamUserVo->
                teamUserVo.setHasJoinNum(teamIdUserTeamList.getOrDefault(teamUserVo.getId(),new ArrayList<>()).size())
        );
        return ResultUtils.success(teamList);
    }

    /**
     * 我创建的队伍
     * @param teamQuery
     * @param request
     * @return
     */
    @GetMapping("/list/my/create")
    public BaseResponse<List<TeamUserVo>> listMyCreateTeams(TeamQuery teamQuery, HttpServletRequest request){
        if (teamQuery == null){
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        teamQuery.setUserId(loginUser.getId());
        List<TeamUserVo> teamUserVoList = teamService.listTeams(teamQuery, true);
        return ResultUtils.success(teamUserVoList);
    }

    /**
     * 我加入的队伍
     * @param teamQuery
     * @param request
     * @return
     */
    @GetMapping("/list/my/join")
    public BaseResponse<List<TeamUserVo>> listMyJoinTeams(TeamQuery teamQuery, HttpServletRequest request){
        if (teamQuery == null){
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("userId",loginUser.getId());

        List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
        //取出不重复的队伍的Id

        //key是teamId value是List<UserTeam>;
        Map<Long,List<UserTeam>>  listMap=userTeamList.
                stream().
                collect(Collectors.groupingBy(UserTeam::getTeamId));
        //teamId：
        ArrayList<Long> idList = new ArrayList<>(listMap.keySet());
        teamQuery.setIdList(idList);
        List<TeamUserVo> teamUserVoList = teamService.listTeams(teamQuery, true);
        return ResultUtils.success(teamUserVoList);
    }


    /**
     * 进行队伍分页信息查询：返回值是list类型不需要RequestBody
     * @param teamQuery
     * @return
     */
    @GetMapping("/list/page")
    public BaseResponse<Page<Team>> listTeamsByPage(TeamQuery teamQuery){
        if (teamQuery == null){
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        Team team=new Team();
        BeanUtils.copyProperties(teamQuery,team);
        //分页：
        Page<Team> page = new Page<>(teamQuery.getPageNum(),teamQuery.getPageSize());
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>(team);
        Page<Team> teamPage = teamService.page(page, queryWrapper);
        if (teamPage == null){
            throw new BusinessException(ErrorCode.NULL_ERROR,"获取队伍分页信息失败！");
        }
        return ResultUtils.success(teamPage);
    }

    /**
     * 只允许当前用户加入team
     * @return
     */
    @PostMapping("/join")
    public BaseResponse<Boolean> JoinTeam(@RequestBody JoinTeamRequest joinTeamRequest,HttpServletRequest request){

        if (joinTeamRequest == null){
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        User loginUser = userService.getLoginUser(request);

        boolean result = teamService.joinTeam(joinTeamRequest, loginUser);
        if (!result){
            throw new BusinessException(ErrorCode.PARAM_ERROR,"加入队伍失败！");
        }
        return ResultUtils.success(result);
    }

    /**
     * 用户退出队伍
     * @param teamQuitRequest
     * @param request
     * @return
     */
    @PostMapping("/quit")
    public BaseResponse<Boolean> quitTeam(@RequestBody TeamQuitRequest teamQuitRequest, HttpServletRequest request){

        if (teamQuitRequest == null){
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.quitTeam(teamQuitRequest, loginUser);
        if (!result){
            throw new BusinessException(ErrorCode.PARAM_ERROR,"加入队伍失败！");
        }
        return ResultUtils.success(result);
    }

}
