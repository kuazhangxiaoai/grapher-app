package com.bonc.graph.project.service.impl;

import com.bonc.common.utils.DateUtils;
import com.bonc.common.utils.uuid.UUID;
import com.bonc.graph.project.domain.Topic;
import com.bonc.graph.project.mapper.GraphTopicMapper;
import com.bonc.graph.project.service.GraphTopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class GraphTopicServiceImpl implements GraphTopicService {

    @Autowired
    private GraphTopicMapper graphTopicMapper;

    /* 增加主题 */
    @Override
    public int addTopic(Topic topic) {
        String uuid = UUID.randomUUID().toString();
        topic.setTopicId(uuid);
        topic.setCreateTime(DateUtils.getNowDate());
        topic.setDelFlag("0");
        return graphTopicMapper.addTopic(topic);
    }

    /* 根据条件查询*/
    @Override
    public List<Map<String, Object>> selectTopicByCondition(String condition, String fieldId) {
        return graphTopicMapper.selectTopicByCondition(condition,fieldId);
    }
    /* 删除主题 */
    @Override
    public int deleteBytopicId(Topic topic) {
        topic.setUpdateTime(DateUtils.getNowDate());
        topic.setDelFlag("2");
        return graphTopicMapper.deleteBytopicId(topic);
    }
}
