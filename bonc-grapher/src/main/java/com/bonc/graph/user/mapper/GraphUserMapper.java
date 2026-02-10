package com.bonc.graph.user.mapper;

import com.bonc.common.core.domain.model.GraphUser;

import java.util.List;

/**
 * graph用户信息Mapper接口
 * 
 * @author wanghao
 * @date 2026-02-10
 */
public interface GraphUserMapper
{
    /**
     * 查询graph用户信息
     * 
     * @param userId graph用户信息主键
     * @return graph用户信息
     */
    public GraphUser selectGraphUserByUserId(Long userId);

    /**
     * 查询graph用户信息列表
     * 
     * @param graphUser graph用户信息
     * @return graph用户信息集合
     */
    public List<GraphUser> selectGraphUserList(GraphUser graphUser);

    /**
     * 新增graph用户信息
     * 
     * @param graphUser graph用户信息
     * @return 结果
     */
    public int insertGraphUser(GraphUser graphUser);

    /**
     * 修改graph用户信息
     * 
     * @param graphUser graph用户信息
     * @return 结果
     */
    public int updateGraphUser(GraphUser graphUser);

    /**
     * 删除graph用户信息
     * 
     * @param userId graph用户信息主键
     * @return 结果
     */
    public int deleteGraphUserByUserId(Long userId);

    /**
     * 批量删除graph用户信息
     * 
     * @param userIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteGraphUserByUserIds(Long[] userIds);
}
