package com.bonc.graph.user.service;

import com.bonc.common.core.domain.model.GraphUser;
import org.apache.ibatis.annotations.Param;

/**
 * ppt用户信息Service接口
 * 
 * @author ruoyi
 * @date 2025-04-10
 */
public interface IGraphUserApiService
{
    /**
     * 登录
     */
    GraphUser login(@Param("userName") String userName, @Param("password") String password);

    /**
     * 注册
     */
    void register(GraphUser graphUser);
}
