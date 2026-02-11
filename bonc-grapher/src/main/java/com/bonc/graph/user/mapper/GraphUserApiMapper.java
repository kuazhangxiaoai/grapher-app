package com.bonc.graph.user.mapper;

import com.bonc.common.core.domain.model.GraphUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 登录
 * 
 * @author wanghao
 * @date 2026-02-10
 */
@Mapper
public interface GraphUserApiMapper
{
    /**
     * 登录
     */
    GraphUser login(@Param("userName") String userName, @Param("passWord") String passWord);

}
