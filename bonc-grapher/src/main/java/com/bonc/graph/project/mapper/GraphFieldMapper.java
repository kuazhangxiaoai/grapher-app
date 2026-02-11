package com.bonc.graph.project.mapper;

import com.bonc.graph.project.domain.Field;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface GraphFieldMapper {
    /**
     * 增加领域
     * @return
     */
     int addField(Field field);

    /**
     * 根据条件进行查询
     * @param condition 条件
     * @return
     */
    List<Map<String,Object>> selectFieldByCondition(String condition);

    /**
     * 删除领域
     * @param fieldId 领域ID
     * @return
     */
    int deleteByFieldId(String fieldId);
}
