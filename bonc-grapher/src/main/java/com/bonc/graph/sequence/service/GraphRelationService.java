package com.bonc.graph.sequence.service;

import com.bonc.graph.project.domain.Article;
import com.bonc.graph.project.domain.Topic;
import com.bonc.graph.project.mapper.GraphArticleMapper;
import com.bonc.graph.project.mapper.GraphTopicMapper;
import com.bonc.graph.sequence.domain.GraphRelation;
import com.bonc.graph.sequence.domain.GraphRelationProperty;
import com.bonc.graph.sequence.domain.GraphSequence;
import com.bonc.graph.sequence.dto.GraphSaveDTO;
import com.bonc.graph.sequence.dto.RelationDeleteDTO;
import com.bonc.graph.sequence.dto.RelationSaveDTO;
import com.bonc.graph.sequence.dto.SearchNodeOrRelationNameDTO;
import com.bonc.graph.sequence.mapper.GraphRelationMapper;
import com.bonc.graph.sequence.mapper.GraphRelationPropertyMapper;
import com.bonc.graph.sequence.mapper.GraphSequenceMapper;
import com.bonc.graph.template.domain.GraphNodeTemplate;
import com.bonc.graph.template.domain.GraphRelationTemplate;
import com.bonc.graph.template.mapper.GraphRelationTemplateMapper;
import com.bonc.graph.utils.HashGenerateUtil;
import com.bonc.graph.utils.HashUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GraphRelationService {

    @Resource
    private GraphRelationMapper graphRelationMapper;
    @Resource
    private GraphRelationPropertyMapper graphRelationPropertyMapper;
    @Resource
    private GraphRelationTemplateMapper relationTemplateMapper;

    @Resource
    private GraphArticleMapper graphArticleMapper;
    @Resource
    private GraphTopicMapper graphTopicMapper;
    @Resource
    private GraphSequenceMapper graphSequenceMapper;

    /**
     * 批量保存关系及属性
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveRelations(List<GraphSaveDTO.RelationDTO> relationDTOList, String sequenceId, String articleId,
                              String topicId, String fieldId) {
        if (relationDTOList == null || relationDTOList.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        List<GraphRelation> relationList = new ArrayList<>();
        List<GraphRelationProperty> propertyList = new ArrayList<>();

        // 1. 构建关系
        for (GraphSaveDTO.RelationDTO relationDTO : relationDTOList) {
            // 生成关系Hash
            String relationHash = HashUtil.generateRelationHash(
                    relationDTO.getRelationName(),
                    relationDTO.getRelationType(),
                    relationDTO.getRelationTrigger(),
                    relationDTO.getStartNodeHash(),
                    relationDTO.getEndNodeHash(),
                    relationDTO.getProperties()
            );

            GraphRelation relation = new GraphRelation();
            relation.setRelationHash(relationHash);
            relation.setRelationTemplateId(relationDTO.getRelationTemplateId());
            relation.setRelationTemplateName(relationDTO.getRelationTemplateName());
            relation.setRelationName(relationDTO.getRelationName());
            relation.setRelationType(relationDTO.getRelationType());
            relation.setRelationTrigger(relationDTO.getRelationTrigger());
            relation.setStartNodeHash(relationDTO.getStartNodeHash());
            relation.setEndNodeHash(relationDTO.getEndNodeHash());
            relation.setCreateTime(now);
            relation.setUpdateTime(now);
            relation.setFieldId(fieldId);
            relation.setTopicId(topicId);
            relation.setArticleId(articleId);
            relation.setSequenceId(sequenceId);
            relationList.add(relation);
        }

        // 2. 批量插入关系
        graphRelationMapper.batchInsert(relationList);

        // 3. 重新查询关系获取relationId
        List<GraphRelation> savedRelations = graphRelationMapper.selectBySequenceId(sequenceId);

        // 4. 构建并插入关系属性
        for (int i = 0; i < savedRelations.size(); i++) {
            GraphRelation relation = savedRelations.get(i);
            GraphSaveDTO.RelationDTO relationDTO = relationDTOList.get(i);
            List<GraphSaveDTO.PropertyDTO> properties = relationDTO.getProperties();

            if (properties != null && !properties.isEmpty()) {
                for (GraphSaveDTO.PropertyDTO propDTO : properties) {
                    GraphRelationProperty property = new GraphRelationProperty();
                    property.setRelationId(relation.getRelationId());
                    property.setPropertyKey(propDTO.getPropertyKey());
                    property.setPropertyValue(propDTO.getPropertyValue());
                    property.setCreateTime(now);
                    property.setUpdateTime(now);
                    propertyList.add(property);
                }
            }
        }

        if (!propertyList.isEmpty()) {
            graphRelationPropertyMapper.batchInsert(propertyList);
        }
    }

    /**
     * 根据sequenceId删除关系及属性
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteRelationsBySequenceId(String sequenceId) {
        // 1. 删除关系属性
        graphRelationPropertyMapper.deleteBySequenceId(sequenceId);
        // 2. 删除关系
        graphRelationMapper.deleteBySequenceId(sequenceId);
    }

    /**
     * 根据sequenceId查询关系（含属性）
     */
    public List<GraphRelation> getRelationsBySequenceId(String sequenceId) {
        // 1. 查询原始节点列表
        List<GraphRelation> relations = graphRelationMapper.selectBySequenceId(sequenceId);
        // 用于存储最终返回的有效节点
        List<GraphRelation> validRelations = new ArrayList<>();

        // 2. 第一步：过滤+更新模板信息（先处理无效节点，避免后续无效查询）
        for (GraphRelation relation : relations) {
            Long relationNodeTemplateId = relation.getRelationTemplateId();
            // 过滤条件：nodeTemplateId为空 或 模板查询不到
            if (relationNodeTemplateId == null) {
                continue;
            }
            GraphRelationTemplate relationTemplate = relationTemplateMapper.selectById(relationNodeTemplateId);
            if (relationTemplate == null) {
                continue;
            }

            // 模板存在，更新内存中节点的名称和颜色
            relation.setRelationTemplateName(relationTemplate.getRelationTemplateName());
            relation.setRelationType(relationTemplate.getRelationTemplateType());
            // 仅将有效节点加入待填充属性的列表
            validRelations.add(relation);
        }

        // 3. 第二步：仅为有效节点填充属性（减少无效的数据库查询）
        for (GraphRelation validRelation : validRelations) {
            List<GraphRelationProperty> properties = graphRelationPropertyMapper.selectByRelationId(validRelation.getRelationId());
            validRelation.setProperties(properties);
        }

        // 4. 返回最终处理后的有效节点列表
        return validRelations;
    }

    /**
     * 根据articleId查询关系（去重，含属性）
     */
    public List<GraphRelation> getDistinctRelationsByArticleId(String articleId) {
        // 1. 查询原始节点列表
        List<GraphRelation> relations = graphRelationMapper.selectDistinctByArticleId(articleId);
        // 用于存储最终返回的有效节点
        List<GraphRelation> validRelations = new ArrayList<>();

        // 2. 第一步：过滤+更新模板信息（先处理无效节点，避免后续无效查询）
        for (GraphRelation relation : relations) {
            Long relationNodeTemplateId = relation.getRelationTemplateId();
            // 过滤条件：nodeTemplateId为空 或 模板查询不到
            if (relationNodeTemplateId == null) {
                continue;
            }
            GraphRelationTemplate relationTemplate = relationTemplateMapper.selectById(relationNodeTemplateId);
            if (relationTemplate == null) {
                continue;
            }

            // 模板存在，更新内存中节点的名称和颜色
            relation.setRelationTemplateName(relationTemplate.getRelationTemplateName());
            relation.setRelationType(relationTemplate.getRelationTemplateType());
            // 仅将有效节点加入待填充属性的列表
            validRelations.add(relation);
        }

        // 3. 第二步：仅为有效节点填充属性（减少无效的数据库查询）
        for (GraphRelation validRelation : validRelations) {
            List<GraphRelationProperty> properties = graphRelationPropertyMapper.selectByRelationId(validRelation.getRelationId());
            validRelation.setProperties(properties);
        }

        // 4. 返回最终处理后的有效节点列表
        return validRelations;
    }

    /**
     * 根据topicId查询关系（去重，含属性）
     */
    public List<GraphRelation> getDistinctRelationsByTopicId(String topicId) {
        // 1. 查询原始节点列表
        List<GraphRelation> relations = graphRelationMapper.selectDistinctByTopicId(topicId);
        // 用于存储最终返回的有效节点
        List<GraphRelation> validRelations = new ArrayList<>();

        // 2. 第一步：过滤+更新模板信息（先处理无效节点，避免后续无效查询）
        for (GraphRelation relation : relations) {
            Long relationNodeTemplateId = relation.getRelationTemplateId();
            // 过滤条件：nodeTemplateId为空 或 模板查询不到
            if (relationNodeTemplateId == null) {
                continue;
            }
            GraphRelationTemplate relationTemplate = relationTemplateMapper.selectById(relationNodeTemplateId);
            if (relationTemplate == null) {
                continue;
            }

            // 模板存在，更新内存中节点的名称和颜色
            relation.setRelationTemplateName(relationTemplate.getRelationTemplateName());
            relation.setRelationType(relationTemplate.getRelationTemplateType());
            // 仅将有效节点加入待填充属性的列表
            validRelations.add(relation);
        }

        // 3. 第二步：仅为有效节点填充属性（减少无效的数据库查询）
        for (GraphRelation validRelation : validRelations) {
            List<GraphRelationProperty> properties = graphRelationPropertyMapper.selectByRelationId(validRelation.getRelationId());
            validRelation.setProperties(properties);
        }

        // 4. 返回最终处理后的有效节点列表
        return validRelations;
    }

    /**
     * 根据fieldId查询关系（去重，含属性）
     */
    public List<GraphRelation> getDistinctRelationsByFieldId(String fieldId) {
        // 1. 查询原始节点列表
        List<GraphRelation> relations = graphRelationMapper.selectDistinctByFieldId(fieldId);
        // 用于存储最终返回的有效节点
        List<GraphRelation> validRelations = new ArrayList<>();

        // 2. 第一步：过滤+更新模板信息（先处理无效节点，避免后续无效查询）
        for (GraphRelation relation : relations) {
            Long relationNodeTemplateId = relation.getRelationTemplateId();
            // 过滤条件：nodeTemplateId为空 或 模板查询不到
            if (relationNodeTemplateId == null) {
                continue;
            }
            GraphRelationTemplate relationTemplate = relationTemplateMapper.selectById(relationNodeTemplateId);
            if (relationTemplate == null) {
                continue;
            }

            // 模板存在，更新内存中节点的名称和颜色
            relation.setRelationTemplateName(relationTemplate.getRelationTemplateName());
            relation.setRelationType(relationTemplate.getRelationTemplateType());
            // 仅将有效节点加入待填充属性的列表
            validRelations.add(relation);
        }

        // 3. 第二步：仅为有效节点填充属性（减少无效的数据库查询）
        for (GraphRelation validRelation : validRelations) {
            List<GraphRelationProperty> properties = graphRelationPropertyMapper.selectByRelationId(validRelation.getRelationId());
            validRelation.setProperties(properties);
        }

        // 4. 返回最终处理后的有效节点列表
        return validRelations;
    }

    /**
     * 根据articleId查询所有关系名称
     */
    public Object getRelationNamesByArticleId(SearchNodeOrRelationNameDTO searchNodeOrRelationNameDTO) {
        return graphRelationMapper.getRelationNamesByArticleId(searchNodeOrRelationNameDTO);
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveRelation(RelationSaveDTO dto) {
        RelationSaveDTO.GraphRelationDTO relationDTO = dto.getRelation();

        if (relationDTO.getRelationId() == null) {
            // 新增关系
            // 生成关系Hash
            String relationHash = HashGenerateUtil.generateRelationHash(
                    relationDTO.getRelationTemplateId(), relationDTO.getRelationName(),
                    relationDTO.getStartNodeHash(), relationDTO.getEndNodeHash(), dto.getProperties());
            relationDTO.setRelationHash(relationHash);
            GraphRelation relation = buildGraphRelation(dto);
            graphRelationMapper.insert(relation);
            saveRelationProperties(relation.getRelationId(), dto.getProperties());
        } else {
            // 修改关系
            List<GraphRelation> existRelations = graphRelationMapper.selectByLevelAndHash(
                    dto.getLevel(), dto.getLevelId(), relationDTO.getRelationHash());
            if (CollectionUtils.isEmpty(existRelations)) {
                throw new RuntimeException("待修改的关系不存在");
            }

            List<GraphRelation> updateRelations = existRelations.stream()
                    .peek(relation -> {
                        relation.setRelationName(relationDTO.getRelationName());
                        relation.setRelationType(relationDTO.getRelationType());
                        relation.setRelationTrigger(relationDTO.getRelationTrigger());
                        relation.setStartNodeHash(relationDTO.getStartNodeHash());
                        relation.setEndNodeHash(relationDTO.getEndNodeHash());
                        relation.setUpdateTime(LocalDateTime.now());
                        relation.setRelationTemplateName(relationDTO.getRelationTemplateName());
                        relation.setRelationTemplateId(relationDTO.getRelationTemplateId());
                    })
                    .collect(Collectors.toList());
            graphRelationMapper.batchUpdate(updateRelations);

            // 更新属性：先删后加
            List<Long> relationIds = existRelations.stream().map(GraphRelation::getRelationId).collect(Collectors.toList());
            graphRelationPropertyMapper.batchDeleteByRelationIds(relationIds);
            for (Long relationId : relationIds) {
                saveRelationProperties(relationId, dto.getProperties());
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteRelation(RelationDeleteDTO dto) {
        // 1. 查询待删除关系
        List<GraphRelation> existRelations = graphRelationMapper.selectByLevelAndHash(
                dto.getLevel(), dto.getLevelId(), dto.getRelationHash());
        if (CollectionUtils.isEmpty(existRelations)) {
            return;
        }

        // 2. 删除关系属性
        List<Long> relationIds = existRelations.stream().map(GraphRelation::getRelationId).collect(Collectors.toList());
        graphRelationPropertyMapper.batchDeleteByRelationIds(relationIds);

        // 3. 删除关系
        graphRelationMapper.batchDelete(relationIds);
    }

    private GraphRelation buildGraphRelation(RelationSaveDTO dto) {
        GraphRelation relation = new GraphRelation();
        RelationSaveDTO.GraphRelationDTO dtoRelation = dto.getRelation();
        relation.setRelationHash(dtoRelation.getRelationHash());
        relation.setRelationName(dtoRelation.getRelationName());
        relation.setRelationType(dtoRelation.getRelationType());
        relation.setRelationTrigger(dtoRelation.getRelationTrigger());
        relation.setStartNodeHash(dtoRelation.getStartNodeHash());
        relation.setEndNodeHash(dtoRelation.getEndNodeHash());
        relation.setRelationTemplateName(dtoRelation.getRelationTemplateName());
        relation.setRelationTemplateId(dtoRelation.getRelationTemplateId());
        relation.setCreateTime(LocalDateTime.now());
        relation.setUpdateTime(LocalDateTime.now());

        // 根据层级填充当前及上级ID
        Integer level = dto.getLevel();
        String levelId = dto.getLevelId();
        switch (level) {
            case 1:
                // 全部层级：不设置任何层级ID
                break;
            case 2:
                // 领域层级：仅设置fieldId
                relation.setFieldId(levelId);
                break;
            case 3:
                // 专题层级：设置topicId + 查询并设置fieldId
                relation.setTopicId(levelId);
                Topic topic = graphTopicMapper.selectByTopicId(levelId);
                if (topic == null) {
                    throw new RuntimeException("专题不存在，topicId：" + levelId);
                }
                relation.setFieldId(topic.getFieldId());
                break;
            case 4:
                // 文章层级：设置articleId + 查询并设置topicId + fieldId
                relation.setArticleId(levelId);
                Article article = graphArticleMapper.selectByArticleId(levelId);
                if (article == null) {
                    throw new RuntimeException("文章不存在，articleId：" + levelId);
                }
                String topicId = article.getTopicId();
                relation.setTopicId(topicId);
                Topic articleTopic = graphTopicMapper.selectByTopicId(topicId);
                if (articleTopic == null) {
                    throw new RuntimeException("专题不存在，topicId：" + topicId);
                }
                relation.setFieldId(articleTopic.getFieldId());
                break;
            case 5:
                // 段落实层级：设置sequenceId + 查询并设置articleId + topicId + fieldId
                relation.setSequenceId(levelId);
                GraphSequence sequence = graphSequenceMapper.selectBySequenceId(levelId);
                if (sequence == null) {
                    throw new RuntimeException("段落不存在，sequenceId：" + levelId);
                }
                String articleId = sequence.getArticleId();
                relation.setArticleId(articleId);
                Article sequenceArticle = graphArticleMapper.selectByArticleId(articleId);
                if (sequenceArticle == null) {
                    throw new RuntimeException("文章不存在，articleId：" + articleId);
                }
                String sequenceTopicId = sequenceArticle.getTopicId();
                relation.setTopicId(sequenceTopicId);
                Topic sequenceTopic = graphTopicMapper.selectByTopicId(sequenceTopicId);
                if (sequenceTopic == null) {
                    throw new RuntimeException("专题不存在，topicId：" + sequenceTopicId);
                }
                relation.setFieldId(sequenceTopic.getFieldId());
                break;
            default:
                throw new RuntimeException("不支持的层级：" + level);
        }
        return relation;
    }

    private void saveRelationProperties(Long relationId, List<RelationSaveDTO.GraphRelationPropertyDTO> propertyDTOs) {
        if (CollectionUtils.isEmpty(propertyDTOs)) {
            return;
        }

        List<GraphRelationProperty> properties = propertyDTOs.stream()
                .map(dto -> {
                    GraphRelationProperty property = new GraphRelationProperty();
                    property.setRelationId(relationId);
                    property.setPropertyKey(dto.getPropertyKey());
                    property.setPropertyValue(dto.getPropertyValue());
                    property.setCreateTime(LocalDateTime.now());
                    property.setUpdateTime(LocalDateTime.now());
                    return property;
                })
                .collect(Collectors.toList());

        graphRelationPropertyMapper.batchInsert(properties);
    }
}
