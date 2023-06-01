package com.jiang.yupao.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jiang.yupao.mapper.UserTeamMapper;
import com.jiang.yupao.model.domain.UserTeam;
import com.jiang.yupao.service.UserTeamService;
import org.springframework.stereotype.Service;

/**
* @author Lenovo
* @description 针对表【user_team(队伍表 -用户关系表)】的数据库操作Service实现
* @createDate 2023-05-21 19:41:49
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService {

}




