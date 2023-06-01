package com.jiang.yupao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jiang.yupao.model.domain.Team;
import com.jiang.yupao.model.domain.User;
import com.jiang.yupao.model.dvo.TeamQuery;
import com.jiang.yupao.model.request.JoinTeamRequest;
import com.jiang.yupao.model.request.TeamQuitRequest;
import com.jiang.yupao.model.request.TeamUpdateRequest;
import com.jiang.yupao.model.vo.TeamUserVo;

import java.util.List;

/**
* @author Lenovo
* @description 针对表【team(队伍表)】的数据库操作Service
* @createDate 2023-05-21 19:40:42
*/
public interface TeamService extends IService<Team> {

    /**
     * 添加队伍
     * @param team
     * @param loginUser
     * @return
     */
    long addTeam(Team team ,User loginUser);

    /**
     * 查询队伍信息：
     * @param teamQuery
     * @param isAdmin
     * @return
     */
    List<TeamUserVo> listTeams(TeamQuery teamQuery,boolean isAdmin);

    Boolean updateTeam(TeamUpdateRequest teamUpdateRequest,User loginUser);

    boolean joinTeam(JoinTeamRequest joinTeamRequest, User loginUser);

    boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser);

    boolean deleteTeam(long id, User loginUser);

    /**
     * 获取单个队伍信息
     * @param id
     * @return
     */
    Team getTeamById(Long id);
}
