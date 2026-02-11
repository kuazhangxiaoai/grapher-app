package com.bonc.graph.project.service.impl;

import com.bonc.common.utils.DateUtils;
import com.bonc.common.utils.uuid.UUID;
import com.bonc.graph.project.domain.Field;
import com.bonc.graph.project.mapper.GraphFieldMapper;
import com.bonc.graph.project.service.GraphFieldService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class GraphFieldServiceImpl implements GraphFieldService {
    @Autowired
    private GraphFieldMapper graphFieldMapper;

    /* 增加领域 */
    @Override
    public int addField(Field field) {
        String uuid = UUID.randomUUID().toString();
        field.setFieldId(uuid);
        field.setCreateTime(DateUtils.getNowDate());
        field.setDelFlag("0");
        return graphFieldMapper.addField(field);
    }

    /* 查找领域 */
    @Override
    public List<Map<String,Object>> selectFieldByCondition(String condition) {
        return graphFieldMapper.selectFieldByCondition(condition);
    }
    /* 删除领域 */
    @Override
    public int deleteByFieldId(Field field) {
        field.setUpdateTime(DateUtils.getNowDate());
        field.setDelFlag("2");
        return graphFieldMapper.deleteByFieldId(field);
    }
}
