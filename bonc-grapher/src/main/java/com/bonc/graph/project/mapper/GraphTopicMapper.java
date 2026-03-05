package com.bonc.graph.project.mapper;


import com.bonc.graph.project.domain.Article;
import com.bonc.graph.project.domain.Topic;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Mapper
@Repository
public interface GraphTopicMapper {
    /**
     * 增加专题
     * @param topic
     * @return
     */
    int addTopic(Topic topic);

    /**
     * 根据条件进行查询
     * @param condition
     * @param fieldId
     * @return
     */
    List<Map<String, Object>> selectTopicByCondition(@Param("condition") String condition, @Param("fieldId") String fieldId);

    /**
     * 根据主题id进行删除
     * @param topic
     * @return
     */
    int deleteBytopicId(Topic topic);
    /*
       根据领域id查询主题
     */
    List<Topic> selectTopicByFieldId(String fieldId);

    /*根据主题id查询*/
    Topic selectTopicById(String topicId);

    /**
     * 根据topicId查询专题信息
     */
    Topic selectByTopicId(@Param("topicId") String topicId);
   /*根据专题名称查询该领域下是否有相同名字的专题*/
    int checkTopic(@Param("topicName") String topicName, @Param("fieldId") String fieldId);
}
