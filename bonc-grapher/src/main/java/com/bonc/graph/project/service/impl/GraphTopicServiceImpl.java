package com.bonc.graph.project.service.impl;

import com.bonc.common.utils.DateUtils;
import com.bonc.common.utils.uuid.UUID;
import com.bonc.graph.project.domain.Article;
import com.bonc.graph.project.domain.Topic;
import com.bonc.graph.project.mapper.GraphArticleMapper;
import com.bonc.graph.project.mapper.GraphTopicMapper;
import com.bonc.graph.project.service.GraphTopicService;
import com.bonc.graph.template.domain.GraphNodeTemplate;
import com.bonc.graph.template.domain.GraphNodeTemplateProperty;
import com.bonc.graph.template.domain.GraphRelationTemplate;
import com.bonc.graph.template.mapper.GraphNodeTemplateMapper;
import com.bonc.graph.template.mapper.GraphRelationTemplateMapper;
import lombok.Data;
import net.sf.jsqlparser.statement.select.Top;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.validation.ValidationException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GraphTopicServiceImpl implements GraphTopicService {

    @Autowired
    private GraphTopicMapper graphTopicMapper;
    @Autowired
    private GraphArticleMapper graphArticleMapper;
    @Autowired
    private GraphNodeTemplateMapper graphNodeTemplateMapper;
    @Autowired
    private GraphRelationTemplateMapper graphRelationTemplateMapper;

    /* 增加主题 */
    @Override
    public int addTopic(Topic topic) {
        checkTopic(topic);
        String uuid = UUID.randomUUID().toString();
        topic.setTopicId(uuid);
        topic.setCreateTime(DateUtils.getNowDate());
        topic.setDelFlag("0");
        return graphTopicMapper.addTopic(topic);
    }

    /*检查该领域下是否有相同名字的专题*/
    private void checkTopic(Topic topic) {
        int graphTopicCount = graphTopicMapper.checkTopic(topic.getTopicName(),topic.getFieldId());
        if(graphTopicCount>0){
            throw new ValidationException("该领域已存在名称为【" + topic.getTopicName() + "】的专题，请勿重复创建");

        }
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

    /*复制主题*/
    @Override
    @Transactional
    public String copyTopic(String topicId, String newFieldId,String topicName, String userName) {
        String newTopicId = UUID.randomUUID().toString();
        //根据主题id进行查找
        Topic oldTopic = graphTopicMapper.selectTopicById(topicId);

        Topic newTopic = new Topic();
        newTopic.setTopicId(newTopicId);
        newTopic.setCreateBy(userName);
        if(!"".equals(topicName) && topicName !=null ){
            newTopic.setTopicName(topicName);
        }else {
            newTopic.setTopicName(oldTopic.topicName);
        }
        // 如果传进来领域id 就是复制领域时调用的该方法
        if(newFieldId!=null){
            newTopic.setFieldId(newFieldId);
        }else{
            newTopic.setFieldId(oldTopic.getFieldId());
        }
        newTopic.setCreateTime(DateUtils.getNowDate());
        newTopic.setDelFlag("0");

        graphTopicMapper.addTopic(newTopic);


        copyArticle(topicId,newTopicId,userName);
        copyNodeAndRelation(topicId,newTopicId);
        return newTopicId;
    }

    /*复制实体和关系*/
    private void copyNodeAndRelation(String topicId, String newTopicId) {

        // 查询节点
        List<GraphNodeTemplate> oldNodes = graphNodeTemplateMapper.selectByTopicId(topicId);
        if (CollectionUtils.isEmpty(oldNodes)) {
            return;
        }

        // 存储旧节点ID到新节点ID的映射
        Map<Long, Long> oldToNewNodeIdMap = new HashMap<>();

        List<GraphNodeTemplate> newNodes = new ArrayList<>();
        for (GraphNodeTemplate oldNode : oldNodes) {
            GraphNodeTemplate newNode = new GraphNodeTemplate();
            BeanUtils.copyProperties(oldNode, newNode);
            // 清空自增主键，让数据库生成新ID
            newNode.setNodeTemplateId(null);
            newNode.setTopicId(newTopicId);
            newNode.setCreateTime(LocalDateTime.now());
            newNode.setUpdateTime(null);
            newNodes.add(newNode);
        }

        // 批量插入新节点
        if (!newNodes.isEmpty()) {
            graphNodeTemplateMapper.bantchInsert(newNodes);

            // 插入后，需要重新查询新节点，以获取数据库生成的新ID，并建立映射
            List<GraphNodeTemplate> insertedNewNodes = graphNodeTemplateMapper.selectByTopicId(newTopicId);
            for (int i = 0; i < oldNodes.size(); i++) {
                Long oldNodeId = oldNodes.get(i).getNodeTemplateId();
                Long newNodeId = insertedNewNodes.get(i).getNodeTemplateId();
                oldToNewNodeIdMap.put(oldNodeId, newNodeId);
            }
        }

        // 复制关系，并替换其中的节点ID
        List<GraphRelationTemplate> oldRelations = graphRelationTemplateMapper.selectByTopicId(topicId);
        if (CollectionUtils.isEmpty(oldRelations)) {
            return;
        }

        List<GraphRelationTemplate> newRelations = new ArrayList<>();
        for (GraphRelationTemplate oldRelation : oldRelations) {
            GraphRelationTemplate newRelation = new GraphRelationTemplate();
            BeanUtils.copyProperties(oldRelation, newRelation);

            // 清空自增主键
            newRelation.setRelationTemplateId(null);
            // 关联到新主题
            newRelation.setTopicId(newTopicId);
            // 替换为新的节点ID
            newRelation.setStartNodeTemplateId(oldToNewNodeIdMap.get(oldRelation.getStartNodeTemplateId()));
            newRelation.setEndNodeTemplateId(oldToNewNodeIdMap.get(oldRelation.getEndNodeTemplateId()));
            // 设置新的创建时间
            newRelation.setCreateTime(LocalDateTime.now());
            newRelation.setUpdateTime(null);

            newRelations.add(newRelation);
        }

        // 批量插入新关系
        if (!newRelations.isEmpty()) {
            graphRelationTemplateMapper.bantchInsert(newRelations);
        }
    }

    /*复制图谱*/
    public void copyArticle(String oldTopicId,String newTopicId,String username){
        // 1. 根据旧的topicId查询所有文章
        List<Article> oldArticles = graphArticleMapper.selectArticlesByTopicId(oldTopicId);

        if (oldArticles == null || oldArticles.isEmpty()) {
            return; // 没有文章需要复制，直接返回
        }

        // 2. 批量构建新文章列表
        List<Article> newArticles = new ArrayList<>(oldArticles.size());
        for (Article oldArticle : oldArticles) {
            Article newArticle = new Article();
            // 生成新的文章ID
            newArticle.setArticleId(UUID.randomUUID().toString());
            // 复制基础字段
            newArticle.setArticleName(oldArticle.getArticleName());
            newArticle.setCreateMethod(oldArticle.getCreateMethod());
            newArticle.setFileName(oldArticle.getFileName());
            newArticle.setFileType(oldArticle.getFileType());
            newArticle.setFileSize(oldArticle.getFileSize());
            newArticle.setFileUrl(oldArticle.getFileUrl());
            // 关联到新专题
            newArticle.setTopicId(newTopicId);
            newArticle.setCreateBy(username);
            newArticles.add(newArticle);
        }

        if (!newArticles.isEmpty()) {
            graphArticleMapper.batchInsertArticles(newArticles);
        }
    }


}
