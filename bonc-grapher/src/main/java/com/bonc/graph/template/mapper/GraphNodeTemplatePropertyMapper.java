package com.bonc.graph.template.mapper;

import com.bonc.graph.template.domain.GraphNodeTemplateProperty;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GraphNodeTemplatePropertyMapper {
    // 按节点模版ID查询属性
    List<GraphNodeTemplateProperty> selectByNodeTemplateId(@Param("nodeTemplateId") Long nodeTemplateId);
    // 批量插入属性
    int batchInsert(@Param("list") List<GraphNodeTemplateProperty> list);
    // 更新删除标识
    int updateDeleteFlagByNodeTemplateId(@Param("nodeTemplateId") Long nodeTemplateId, @Param("isDeleteFlag") String isDeleteFlag);
}
