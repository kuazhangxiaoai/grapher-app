package com.bonc.graph.project.mapper;


import com.bonc.graph.project.domain.Topic;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface GraphTopicMapper {
    /**
     * 增加专题
     * @param topic
     * @return
     */
    int addTopic(Topic topic);

    List<Map<String, Object>> selectTopicByCondition(@Param("condition") String condition, @Param("fieldId") String fieldId);

    int deleteBytopicId(Topic topic);
}
