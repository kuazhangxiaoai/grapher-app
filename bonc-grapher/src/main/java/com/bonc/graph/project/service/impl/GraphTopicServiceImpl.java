package com.bonc.graph.project.service.impl;

import com.bonc.common.utils.DateUtils;
import com.bonc.common.utils.uuid.UUID;
import com.bonc.graph.project.domain.Article;
import com.bonc.graph.project.domain.Topic;
import com.bonc.graph.project.mapper.GraphArticleMapper;
import com.bonc.graph.project.mapper.GraphTopicMapper;
import com.bonc.graph.project.service.GraphArticleService;
import com.bonc.graph.project.service.GraphTopicService;
import com.bonc.graph.template.domain.GraphNodeTemplate;
import com.bonc.graph.template.domain.GraphNodeTemplateProperty;
import com.bonc.graph.template.domain.GraphRelationTemplate;
import com.bonc.graph.template.domain.GraphRelationTemplateProperty;
import com.bonc.graph.template.mapper.GraphNodeTemplateMapper;
import com.bonc.graph.template.mapper.GraphNodeTemplatePropertyMapper;
import com.bonc.graph.template.mapper.GraphRelationTemplateMapper;
import com.bonc.graph.template.mapper.GraphRelationTemplatePropertyMapper;
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
    @Autowired
    private GraphArticleService graphArticleService;
    @Autowired
    private GraphNodeTemplatePropertyMapper graphNodeTemplatePropertyMapper;
    @Autowired
    private GraphRelationTemplatePropertyMapper graphRelationTemplatePropertyMapper;

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
        //查找该主题下的所有article
        List<Article> articles = graphArticleMapper.selectArticlesByTopicId(topic.getTopicId());
        if(articles.size()>0){
            for(Article article:articles){
                graphArticleService.deleteArticle(article);
            }
        }
        //查找该主题下的所有节点模板并删除
        List<GraphNodeTemplate> graphNodeTemplates =  graphNodeTemplateMapper.selectByTopicId(topic.getTopicId());
        if(graphNodeTemplates.size()>0){
            for(GraphNodeTemplate graphNodeTemplate:graphNodeTemplates){
                //删除节点模板
                graphNodeTemplateMapper.updateDeleteFlag(graphNodeTemplate.getNodeTemplateId(),"1");
                //删除节点模板属性
                graphNodeTemplatePropertyMapper.updateDeleteFlagByNodeTemplateId(graphNodeTemplate.getNodeTemplateId(),"1");
            }
        }
        //查找该主题下的所有关系模板并删除
        List<GraphRelationTemplate> graphRelationTemplates = graphRelationTemplateMapper.selectByTopicId(topic.getTopicId());
        if(graphRelationTemplates.size()>0){
            for(GraphRelationTemplate graphRelationTemplate:graphRelationTemplates){
                //删除关系模板
                graphRelationTemplateMapper.updateDeleteFlag(graphRelationTemplate.getRelationTemplateId(),"1");
                //删除关系模板属性
                graphRelationTemplatePropertyMapper.updateDeleteFlagByRelationTemplateId(graphRelationTemplate.getRelationTemplateId(),"1");
            }
        }

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

    /* ========复制实体模板和关系模板 ===========*/

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

            // 插入后，需要重新查询新节点模板，以获取数据库生成的新ID，并建立映射
            List<GraphNodeTemplate> insertedNewNodes = graphNodeTemplateMapper.selectByTopicId(newTopicId);
            for (int i = 0; i < oldNodes.size(); i++) {
                Long oldNodeId = oldNodes.get(i).getNodeTemplateId();
                Long newNodeId = insertedNewNodes.get(i).getNodeTemplateId();
                oldToNewNodeIdMap.put(oldNodeId, newNodeId);
            }
        }


        // ========== 2. 复制节模板点属性（补全核心逻辑） ==========
        //根据旧的节点id查询节点模板属性
        List<GraphNodeTemplateProperty> allOldNodeProperties = new ArrayList<>();
        for (GraphNodeTemplate oldNode : oldNodes) {
            List<GraphNodeTemplateProperty> oldNodeProperties = graphNodeTemplatePropertyMapper.selectByNodeTemplateId(oldNode.getNodeTemplateId());
            // 如果当前节点有属性，加入总列表
            if (!CollectionUtils.isEmpty(oldNodeProperties)) {
                allOldNodeProperties.addAll(oldNodeProperties);
            }
        }

        // 复制属性并关联新节点ID
        if (!CollectionUtils.isEmpty(allOldNodeProperties)) {
            List<GraphNodeTemplateProperty> newNodeProperties = new ArrayList<>();
            for (GraphNodeTemplateProperty oldProp : allOldNodeProperties) {
                GraphNodeTemplateProperty newProp = new GraphNodeTemplateProperty();
                // 复制旧属性的所有字段
                BeanUtils.copyProperties(oldProp, newProp);

                // 清空自增主键，让数据库生成新ID
                newProp.setNodeTemplatePropertyId(null);
                // 替换为新的节点ID（通过旧→新映射
                Long newNodeId = oldToNewNodeIdMap.get(oldProp.getNodeTemplateId());
                if (newNodeId != null) {
                    newProp.setNodeTemplateId(newNodeId);
                }
                newProp.setCreateTime(LocalDateTime.now());
                newProp.setUpdateTime(null);
                newProp.setIsDeleteFlag(oldProp.getIsDeleteFlag());
                newNodeProperties.add(newProp);
            }
            // 批量插入新属性
            graphNodeTemplatePropertyMapper.batchInsert(newNodeProperties);
        }

        // ========== 3. 复制关系模板 ==========
        List<GraphRelationTemplate> oldRelations = graphRelationTemplateMapper.selectByTopicId(topicId);
        if (CollectionUtils.isEmpty(oldRelations)) {
            return;
        }

        // 存储旧关系ID到新关系ID的映射
        Map<Long, Long> oldToNewRelationIdMap = new HashMap<>();

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

            // 插入后，重新查询新关系模板，建立旧→新关系ID映射
            List<GraphRelationTemplate> insertedNewRelations = graphRelationTemplateMapper.selectByTopicId(newTopicId);
            Map<String, Long> relationNameToNewIdMap = new HashMap<>();
            for (GraphRelationTemplate newRelation : insertedNewRelations) {
                relationNameToNewIdMap.put(newRelation.getRelationTemplateName(), newRelation.getRelationTemplateId());
            }
            for (GraphRelationTemplate oldRelation : oldRelations) {
                Long oldRelationId = oldRelation.getRelationTemplateId();
                Long newRelationId = relationNameToNewIdMap.get(oldRelation.getRelationTemplateName());
                if (newRelationId != null) {
                    oldToNewRelationIdMap.put(oldRelationId, newRelationId);
                }
            }
        }

        // ========== 4. 复制关系模板属性（新增核心逻辑） ==========
        List<GraphRelationTemplateProperty> allOldRelationProperties = new ArrayList<>();
        for (GraphRelationTemplate oldRelation : oldRelations) {
            // 查询当前旧关系对应的所有属性
            List<GraphRelationTemplateProperty> oldRelationProperties = graphRelationTemplatePropertyMapper.selectByRelationTemplateId(oldRelation.getRelationTemplateId());
            if (!CollectionUtils.isEmpty(oldRelationProperties)) {
                allOldRelationProperties.addAll(oldRelationProperties);
            }
        }

        // 复制关系属性并关联新关系ID
        if (!CollectionUtils.isEmpty(allOldRelationProperties)) {
            List<GraphRelationTemplateProperty> newRelationProperties = new ArrayList<>();
            for (GraphRelationTemplateProperty oldProp : allOldRelationProperties) {
                GraphRelationTemplateProperty newProp = new GraphRelationTemplateProperty();
                BeanUtils.copyProperties(oldProp, newProp);

                // 清空自增主键，让数据库生成新ID
                newProp.setRelationTemplatePropertyId(null);
                // 替换为新的关系ID（核心）
                Long newRelationId = oldToNewRelationIdMap.get(oldProp.getRelationTemplateId());
                if (newRelationId != null) {
                    newProp.setRelationTemplateId(newRelationId);
                }
                newProp.setCreateTime(LocalDateTime.now());
                newProp.setUpdateTime(null);
                newProp.setIsDeleteFlag(oldProp.getIsDeleteFlag());

                newRelationProperties.add(newProp);
            }
            // 批量插入新关系属性
            graphRelationTemplatePropertyMapper.batchInsert(newRelationProperties);
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
