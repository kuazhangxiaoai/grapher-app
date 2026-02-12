package com.bonc.graph.project.service;

import com.bonc.graph.project.domain.Topic;

import java.util.List;
import java.util.Map;

public interface GraphTopicService {
    /* 增加主题 */
    int addTopic(Topic topic);

    /* 根据条件查询*/
    List<Map<String,Object>> selectTopicByCondition(String condition, String fieldId);

    /* 删除主题  */
    int deleteBytopicId(Topic topic);
}
