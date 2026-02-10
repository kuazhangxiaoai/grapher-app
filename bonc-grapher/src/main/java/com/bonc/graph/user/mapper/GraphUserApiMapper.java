package com.bonc.graph.user.mapper;

import com.bonc.common.core.domain.model.GraphUser;
import org.apache.ibatis.annotations.Param;

/**
 * 登录
 * 
 * @author ruoyi
 * @date 2025-04-10
 */
public interface GraphUserApiMapper
{
    /**
     * 登录
     */
    GraphUser login(@Param("userName") String userName, @Param("passWord") String passWord);

}
