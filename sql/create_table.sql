
-- 用户表
create table user
(
    id           bigint auto_increment comment '主键id'
        primary key,
    userName     varchar(256)                       null comment '用户名',
    userAccount  varchar(256)                       null comment '账号',
    avatarUrl    varchar(1024)                      null comment '头像',
    gender       tinyint  default 0                 null comment '性别 ：0:表示男',
    userPassword varchar(512)                       null comment '密码',
    phone        varchar(128)                       null comment '手机号',
    profile      varchar(512)                       null comment '个人简介',
    email        varchar(512)                       null comment '邮箱',
    userStatus   int      default 0                 null comment '状态 0：表示正常',
    createTime   datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime   datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint  default 0                 null comment '是否删除，0表示正常 1：表示删除',
    userRole     int      default 0                 null comment '用户权限 0:表示普通用户；1:表示管理员',
    planetCode   varchar(512)                       null comment '星球编码',
    tags         varchar(1024)                      null comment '标签列表'
)comment '用户表';



-- 标签表
create table tag
(
    id         bigint auto_increment comment '主键id'
        primary key,
    tagName    varchar(256)                       null comment '标签名称',
    userId     bigint                             null comment '用户id',
    parentId   bigint                             null comment '父标签id',
    isParent   tinyint  default 0                 null comment '0：不是父标签，1是父标签',
    createTime datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除，0表示正常 1：表示删除',
    constraint uniIdx_tagName
        unique (tagName),
    constraint uniIdx_userId
        unique (userId)
)
    comment '标签';


-- 队伍表
create table team
(
    id           bigint auto_increment comment '主键id'
        primary key,
    name     varchar(256)                      not null comment '队伍名称',
    description    varchar(1024)                      null comment '描述',
    maxNum   int      default 1                not null comment '最大人数',
    expireTime   datetime   null comment '过期时间',
    userId           bigint comment '用户id(队长的id)',
    status   int      default 0               not  null comment '状态 0：公开，1：私有;2：加密',
    password varchar(512)                       null comment '密码',
    createTime   datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime   datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint  default 0               not  null comment '是否删除，0表示正常 1：表示删除'
)comment '队伍表';


-- 队伍表 -用户关系表(多对多的关系)
create table user_team
(
    id           bigint auto_increment comment '主键id'
        primary key,
    userId           bigint comment '用户id(队长的id)',
    teamId           bigint comment '队伍id',
    joinTime   datetime   null comment '加入时间',
    createTime   datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime   datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint  default 0               not  null comment '是否删除，0表示正常 1：表示删除'
)comment '队伍表 -用户关系表';