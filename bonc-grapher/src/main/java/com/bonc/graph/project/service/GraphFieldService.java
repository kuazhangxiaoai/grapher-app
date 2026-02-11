package com.bonc.graph.project.service;


import com.bonc.graph.project.domain.Field;

import java.util.List;
import java.util.Map;

public interface GraphFieldService {

    /**
     * 增加领域
     * @param field
     * @return
     */
     int  addField(Field field);

    /**
     * 查询领域 根据条件查询
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
