package com.bonc.graph.template.mapper;

import com.bonc.graph.template.domain.GraphRelationTemplateProperty;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GraphRelationTemplatePropertyMapper {
    // 按关系模版ID查询属性
    List<GraphRelationTemplateProperty> selectByRelationTemplateId(@Param("relationTemplateId") Long relationTemplateId);
    // 批量插入属性
    int batchInsert(@Param("list") List<GraphRelationTemplateProperty> list);
    // 更新删除标识
    int updateDeleteFlagByRelationTemplateId(@Param("relationTemplateId") Long relationTemplateId, @Param("isDeleteFlag") String isDeleteFlag);
}
