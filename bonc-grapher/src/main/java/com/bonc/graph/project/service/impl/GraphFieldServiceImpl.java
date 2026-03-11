package com.bonc.graph.project.service.impl;

import com.bonc.common.utils.DateUtils;
import com.bonc.common.utils.uuid.UUID;
import com.bonc.graph.project.domain.Field;
import com.bonc.graph.project.domain.Topic;
import com.bonc.graph.project.mapper.GraphFieldMapper;
import com.bonc.graph.project.mapper.GraphTopicMapper;
import com.bonc.graph.project.service.GraphFieldService;
import com.bonc.graph.project.service.GraphTopicService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.ValidationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class GraphFieldServiceImpl implements GraphFieldService {
    @Autowired
    private GraphFieldMapper graphFieldMapper;
    @Autowired
    private GraphTopicMapper graphTopicMapper;
    @Autowired
    private GraphTopicService graphTopicService;

    /* 增加领域 */
    @Override
    public int addField(Field field) {
        checkField(field.getFieldName());
        String uuid = UUID.randomUUID().toString();
        field.setFieldId(uuid);
        field.setCreateTime(DateUtils.getNowDate());
        field.setDelFlag("0");
        return graphFieldMapper.addField(field);
    }

    /*检查是否有重复的领域*/
    private void checkField(String fieldName) {
        int graphFieldCount= graphFieldMapper.checkField(fieldName);

        if(graphFieldCount>0){
            throw new ValidationException("已存在名称为【" + fieldName + "】的领域，请勿重复创建");
        }
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
        List<Topic> topics = graphTopicMapper.selectTopicByFieldId(field.getFieldId());
        if(topics.size()>0){
            for(Topic topic:topics){
                graphTopicService.deleteBytopicId(topic);
            }
        }
        return graphFieldMapper.deleteByFieldId(field);
    }
    /*复制领域*/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String copyField(String fieldId, String fieldName, String userName) {

        int count = graphFieldMapper.checkField(fieldName);
        if(count>0){
            throw new ValidationException("已存在名称为【" + fieldName + "】的领域，请勿重复复制");

        }
        // 先复制领域
        String fieldUuid = UUID.randomUUID().toString();
        Field field = graphFieldMapper.selectByFieldId(fieldId);
        Field newField = new Field();
        newField.setFieldId(fieldUuid);
        newField.setFieldName(fieldName);
        newField.setCreateBy(userName);
        graphFieldMapper.addField(newField);
        //复制领域下的所有专题
        List<Topic> oldTopics = graphTopicMapper.selectTopicByFieldId(fieldId);
        if (CollectionUtils.isNotEmpty(oldTopics)) {
            for (Topic oldTopic : oldTopics) {
                graphTopicService.copyTopic(oldTopic.getTopicId(), fieldUuid, oldTopic.getTopicName(), userName);
            }
        }
        return fieldUuid;
    }
}
