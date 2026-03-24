package com.bonc.graph.sequence.service;

import com.bonc.common.utils.StringUtils;
import com.bonc.graph.project.domain.Article;
import com.bonc.graph.project.domain.Topic;
import com.bonc.graph.project.mapper.GraphArticleMapper;
import com.bonc.graph.project.mapper.GraphTopicMapper;
import com.bonc.graph.sequence.domain.GraphNode;
import com.bonc.graph.sequence.domain.GraphNodeProperty;
import com.bonc.graph.sequence.domain.GraphRelation;
import com.bonc.graph.sequence.domain.GraphSequence;
import com.bonc.graph.sequence.dto.GraphSaveDTO;
import com.bonc.graph.sequence.dto.NodeDeleteDTO;
import com.bonc.graph.sequence.dto.NodeSaveDTO;
import com.bonc.graph.sequence.dto.SearchNodeOrRelationNameDTO;
import com.bonc.graph.sequence.mapper.*;
import com.bonc.graph.template.domain.GraphNodeTemplate;
import com.bonc.graph.template.mapper.GraphNodeTemplateMapper;
import com.bonc.graph.utils.HashGenerateUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GraphNodeService {

    @Resource
    private GraphNodeMapper graphNodeMapper;
    @Resource
    private GraphNodePropertyMapper graphNodePropertyMapper;
    @Resource
    private GraphNodeTemplateMapper nodeTemplateMapper;

    @Resource
    private GraphRelationMapper graphRelationMapper;
    @Resource
    private GraphRelationPropertyMapper graphRelationPropertyMapper;

    @Resource
    private GraphArticleMapper graphArticleMapper;
    @Resource
    private GraphTopicMapper graphTopicMapper;
    @Resource
    private GraphSequenceMapper graphSequenceMapper;

    @Resource
    private GraphSequenceService graphSequenceService;
    @Resource
    private GraphSequencePositionService graphSequencePositionService;

    /**
     * 批量保存节点及属性
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveNodes(List<GraphSaveDTO.NodeDTO> nodeDTOList, String sequenceId, String articleId,
                          String topicId, String fieldId) {
        if (nodeDTOList == null || nodeDTOList.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        List<GraphNode> nodeList = new ArrayList<>();
        List<GraphNodeProperty> propertyList = new ArrayList<>();

        // 1. 构建节点和属性
        for (GraphSaveDTO.NodeDTO nodeDTO : nodeDTOList) {
            // 生成节点Hash
//            String nodeHash = HashUtil.generateNodeHash(nodeDTO.getNodeName(), nodeDTO.getNodeColor(), nodeDTO.getProperties());

            // 构建节点
            GraphNode node = new GraphNode();
            node.setNodeHash(nodeDTO.getNodeHash());
            node.setNodeTemplateId(nodeDTO.getNodeTemplateId());
            node.setNodeTemplateName(nodeDTO.getNodeTemplateName());
            node.setNodeName(nodeDTO.getNodeName());
            node.setNodeDescription(nodeDTO.getNodeDescription());
            node.setNodeColor(nodeDTO.getNodeColor());
            node.setCreateTime(now);
            node.setUpdateTime(now);
            node.setFieldId(fieldId);
            node.setTopicId(topicId);
            node.setArticleId(articleId);
            node.setSequenceId(sequenceId);
            nodeList.add(node);
        }

        // 2. 批量插入节点
        graphNodeMapper.batchInsert(nodeList);

        // 3. 重新查询节点获取nodeId（PostgreSQL自增ID需要查询）
        List<GraphNode> savedNodes = graphNodeMapper.selectBySequenceId(sequenceId);

        // 4. 构建并插入节点属性
        for (int i = 0; i < savedNodes.size(); i++) {
            GraphNode node = savedNodes.get(i);
            GraphSaveDTO.NodeDTO nodeDTO = nodeDTOList.get(i);
            List<GraphSaveDTO.PropertyDTO> properties = nodeDTO.getProperties();

            if (properties != null && !properties.isEmpty()) {
                for (GraphSaveDTO.PropertyDTO propDTO : properties) {
                    GraphNodeProperty property = new GraphNodeProperty();
                    property.setNodeId(node.getNodeId());
                    property.setPropertyKey(propDTO.getPropertyKey());
                    property.setPropertyValue(propDTO.getPropertyValue());
                    property.setCreateTime(now);
                    property.setUpdateTime(now);
                    propertyList.add(property);
                }
            }
        }

        if (!propertyList.isEmpty()) {
            graphNodePropertyMapper.batchInsert(propertyList);
        }
    }

    /**
     * 根据sequenceId删除节点及属性
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteNodesBySequenceId(String sequenceId) {
        // 1. 删除节点属性
        graphNodePropertyMapper.deleteBySequenceId(sequenceId);
        // 2. 删除节点
        graphNodeMapper.deleteBySequenceId(sequenceId);
    }

    /**
     * 根据sequenceId查询节点（含属性）
     */
    public List<GraphNode> getNodesBySequenceId(String sequenceId) {
        // 1. 查询原始节点列表
        List<GraphNode> nodes = graphNodeMapper.selectBySequenceId(sequenceId);
        // 用于存储最终返回的有效节点
        List<GraphNode> validNodes = new ArrayList<>();

        // 2. 第一步：过滤+更新模板信息（先处理无效节点，避免后续无效查询）
        for (GraphNode node : nodes) {
            Long nodeTemplateId = node.getNodeTemplateId();
            // 过滤条件：nodeTemplateId为空 或 模板查询不到
            if (nodeTemplateId == null) {
                continue;
            }
            GraphNodeTemplate nodeTemplate = nodeTemplateMapper.selectById(nodeTemplateId);
            if (nodeTemplate == null) {
                continue;
            }

            // 模板存在，更新内存中节点的名称和颜色
            node.setNodeTemplateName(nodeTemplate.getNodeTemplateName());
            node.setNodeColor(nodeTemplate.getNodeTemplateColor());
            // 仅将有效节点加入待填充属性的列表
            validNodes.add(node);
        }

        // 3. 第二步：仅为有效节点填充属性（减少无效的数据库查询）
        for (GraphNode validNode : validNodes) {
            List<GraphNodeProperty> properties = graphNodePropertyMapper.selectByNodeId(validNode.getNodeId());
            validNode.setProperties(properties);
        }

        // 4. 返回最终处理后的有效节点列表
        return validNodes;
    }

    /**
     * 根据articleId查询节点（去重，含属性）
     */
    public List<GraphNode> getDistinctNodesByArticleId(String articleId) {
        // 1. 查询原始节点列表
        List<GraphNode> nodes = graphNodeMapper.selectDistinctByArticleId(articleId);
        // 用于存储最终返回的有效节点
        List<GraphNode> validNodes = new ArrayList<>();

        // 2. 第一步：过滤+更新模板信息（先处理无效节点，避免后续无效查询）
        for (GraphNode node : nodes) {
            Long nodeTemplateId = node.getNodeTemplateId();
            // 过滤条件：nodeTemplateId为空 或 模板查询不到
            if (nodeTemplateId == null) {
                continue;
            }
            GraphNodeTemplate nodeTemplate = nodeTemplateMapper.selectById(nodeTemplateId);
            if (nodeTemplate == null) {
                continue;
            }

            // 模板存在，更新内存中节点的名称和颜色
            node.setNodeTemplateName(nodeTemplate.getNodeTemplateName());
            node.setNodeColor(nodeTemplate.getNodeTemplateColor());
            // 仅将有效节点加入待填充属性的列表
            validNodes.add(node);
        }

        // 3. 第二步：仅为有效节点填充属性（减少无效的数据库查询）
        for (GraphNode validNode : validNodes) {
            List<GraphNodeProperty> properties = graphNodePropertyMapper.selectByNodeId(validNode.getNodeId());
            validNode.setProperties(properties);
        }

        // 4. 返回最终处理后的有效节点列表
        return validNodes;
    }


    /**
     * 根据topicId查询节点（去重，含属性）
     */
    public List<GraphNode> getDistinctNodesByTopicId(String topicId) {
        // 1. 查询原始节点列表
        List<GraphNode> nodes = graphNodeMapper.selectDistinctByTopicId(topicId);
        // 用于存储最终返回的有效节点
        List<GraphNode> validNodes = new ArrayList<>();

        // 2. 第一步：过滤+更新模板信息（先处理无效节点，避免后续无效查询）
        for (GraphNode node : nodes) {
            Long nodeTemplateId = node.getNodeTemplateId();
            // 过滤条件：nodeTemplateId为空 或 模板查询不到
            if (nodeTemplateId == null) {
                continue;
            }
            GraphNodeTemplate nodeTemplate = nodeTemplateMapper.selectById(nodeTemplateId);
            if (nodeTemplate == null) {
                continue;
            }

            // 模板存在，更新内存中节点的名称和颜色
            node.setNodeTemplateName(nodeTemplate.getNodeTemplateName());
            node.setNodeColor(nodeTemplate.getNodeTemplateColor());
            // 仅将有效节点加入待填充属性的列表
            validNodes.add(node);
        }

        // 3. 第二步：仅为有效节点填充属性（减少无效的数据库查询）
        for (GraphNode validNode : validNodes) {
            List<GraphNodeProperty> properties = graphNodePropertyMapper.selectByNodeId(validNode.getNodeId());
            validNode.setProperties(properties);
        }

        // 4. 返回最终处理后的有效节点列表
        return validNodes;
    }

    /**
     * 根据fieldId查询节点（去重，含属性）
     */
    public List<GraphNode> getDistinctNodesByFieldId(String fieldId) {
        // 1. 查询原始节点列表
        List<GraphNode> nodes = graphNodeMapper.selectDistinctByFieldId(fieldId);
        // 用于存储最终返回的有效节点
        List<GraphNode> validNodes = new ArrayList<>();

        // 2. 第一步：过滤+更新模板信息（先处理无效节点，避免后续无效查询）
        for (GraphNode node : nodes) {
            Long nodeTemplateId = node.getNodeTemplateId();
            // 过滤条件：nodeTemplateId为空 或 模板查询不到
            if (nodeTemplateId == null) {
                continue;
            }
            GraphNodeTemplate nodeTemplate = nodeTemplateMapper.selectById(nodeTemplateId);
            if (nodeTemplate == null) {
                continue;
            }

            // 模板存在，更新内存中节点的名称和颜色
            node.setNodeTemplateName(nodeTemplate.getNodeTemplateName());
            node.setNodeColor(nodeTemplate.getNodeTemplateColor());
            // 仅将有效节点加入待填充属性的列表
            validNodes.add(node);
        }

        // 3. 第二步：仅为有效节点填充属性（减少无效的数据库查询）
        for (GraphNode validNode : validNodes) {
            List<GraphNodeProperty> properties = graphNodePropertyMapper.selectByNodeId(validNode.getNodeId());
            validNode.setProperties(properties);
        }

        // 4. 返回最终处理后的有效节点列表
        return validNodes;
    }

    /**
     * 根据articleId查询所有节点名称
     */
    public List<String> getNodeNamesByArticleId(SearchNodeOrRelationNameDTO searchNodeOrRelationNameDTO) {
        return graphNodeMapper.getNodeNamesByArticleId(searchNodeOrRelationNameDTO);
    }


    @Transactional(rollbackFor = Exception.class)
    public void saveNode(NodeSaveDTO dto) {
        NodeSaveDTO.GraphNodeDTO nodeDTO = dto.getNode();
        // 判断level=5并且sequenceId为空
        if (5 == dto.getLevel()) {
            String articleId = dto.getArticleId();
            String sequenceId = dto.getSequenceId();
            if(StringUtils.isEmpty(sequenceId)){
                // 创建sequence并赋值给sequenceId
                sequenceId = graphSequenceService.createSequence(articleId, dto.getSequenceContent());
                // 将sequenceId赋值给levelId
                dto.setLevelId(sequenceId);
                // 批量插入位置信息
                graphSequencePositionService.batchCreateSequencePosition(sequenceId, dto.getSequencePositionList());
            }else{
                //修改段落：先校验段落存在
                GraphSequence sequence = graphSequenceMapper.selectBySequenceId(sequenceId);
                if (sequence == null) {
                    throw new RuntimeException("段落不存在，sequenceId：" + sequenceId);
                }
                // 更新段落表时间
                graphSequenceService.updateSequenceUpdateTime(sequenceId);
            }
        }

        if (nodeDTO.getNodeId() == null) {
            // 新增节点
            // 生成节点Hash
            String nodeHash = HashGenerateUtil.generateNodeHash(
                    nodeDTO.getNodeTemplateId(), nodeDTO.getNodeName(), dto.getProperties());
            nodeDTO.setNodeHash(nodeHash);
            GraphNode node = buildGraphNode(dto);
            graphNodeMapper.insert(node);
            saveNodeProperties(node.getNodeId(), dto.getProperties());
        } else {
            // 修改节点
            List<GraphNode> existNodes = graphNodeMapper.selectByLevelAndHash(
                    dto.getLevel(), dto.getLevelId(), nodeDTO.getNodeHash());
            if (CollectionUtils.isEmpty(existNodes)) {
                throw new RuntimeException("待修改的节点不存在");
            }

            List<GraphNode> updateNodes = existNodes.stream()
                    .peek(node -> {
                        node.setNodeName(nodeDTO.getNodeName());
                        node.setNodeDescription(nodeDTO.getNodeDescription());
                        node.setNodeColor(nodeDTO.getNodeColor());
                        node.setUpdateTime(LocalDateTime.now());
                        node.setNodeTemplateName(nodeDTO.getNodeTemplateName());
                        node.setNodeTemplateId(nodeDTO.getNodeTemplateId());
                    })
                    .collect(Collectors.toList());
            graphNodeMapper.batchUpdate(updateNodes);

            // 更新属性：先删后加
            List<Long> nodeIds = existNodes.stream().map(GraphNode::getNodeId).collect(Collectors.toList());
            graphNodePropertyMapper.batchDeleteByNodeIds(nodeIds);
            for (Long nodeId : nodeIds) {
                saveNodeProperties(nodeId, dto.getProperties());
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteNode(NodeDeleteDTO dto) {
        // 1. 查询待删除节点
        List<GraphNode> existNodes = graphNodeMapper.selectByLevelAndHash(
                dto.getLevel(), dto.getLevelId(), dto.getNodeHash());
        if (CollectionUtils.isEmpty(existNodes)) {
            return;
        }

        // 2. 删除节点属性
        List<Long> nodeIds = existNodes.stream().map(GraphNode::getNodeId).collect(Collectors.toList());
        graphNodePropertyMapper.batchDeleteByNodeIds(nodeIds);

        // 3. 删除关联关系及属性
        List<GraphRelation> relateRelations = graphRelationMapper.selectByNodeHash(dto.getNodeHash());
        if (CollectionUtils.isNotEmpty(relateRelations)) {
            List<Long> relationIds = relateRelations.stream().map(GraphRelation::getRelationId).collect(Collectors.toList());
            // 删除关系属性
            graphRelationPropertyMapper.batchDeleteByRelationIds(relationIds);
            // 删除关系
            graphRelationMapper.batchDelete(relationIds);
        }

        // 4. 删除节点
        graphNodeMapper.batchDelete(nodeIds);
    }

    private GraphNode buildGraphNode(NodeSaveDTO dto) {
        GraphNode node = new GraphNode();
        NodeSaveDTO.GraphNodeDTO dtoNode = dto.getNode();
        node.setNodeHash(dtoNode.getNodeHash());
        node.setNodeName(dtoNode.getNodeName());
        node.setNodeDescription(dtoNode.getNodeDescription());
        node.setNodeColor(dtoNode.getNodeColor());
        node.setNodeTemplateName(dtoNode.getNodeTemplateName());
        node.setNodeTemplateId(dtoNode.getNodeTemplateId());
        node.setCreateTime(LocalDateTime.now());
        node.setUpdateTime(LocalDateTime.now());

        // 根据层级填充当前及上级ID
        Integer level = dto.getLevel();
        String levelId = dto.getLevelId();
        switch (level) {
            case 1:
                // 全部层级：不设置任何层级ID
                break;
            case 2:
                // 领域层级：仅设置fieldId
                node.setFieldId(levelId);
                break;
            case 3:
                // 专题层级：设置topicId + 查询并设置fieldId
                node.setTopicId(levelId);
                // 查询专题对应的领域ID
                Topic topic = graphTopicMapper.selectByTopicId(levelId);
                if (topic == null) {
                    throw new RuntimeException("专题不存在，topicId：" + levelId);
                }
                node.setFieldId(topic.getFieldId());
                break;
            case 4:
                // 文章层级：设置articleId + 查询并设置topicId + fieldId
                node.setArticleId(levelId);
                // 1. 查询文章对应的专题ID
                Article article = graphArticleMapper.selectByArticleId(levelId);
                if (article == null) {
                    throw new RuntimeException("文章不存在，articleId：" + levelId);
                }
                String topicId = article.getTopicId();
                node.setTopicId(topicId);
                // 2. 查询专题对应的领域ID
                Topic articleTopic = graphTopicMapper.selectByTopicId(topicId);
                if (articleTopic == null) {
                    throw new RuntimeException("专题不存在，topicId：" + topicId);
                }
                node.setFieldId(articleTopic.getFieldId());
                break;
            case 5:
                // 段落实层级：设置sequenceId + 查询并设置articleId + topicId + fieldId
                node.setSequenceId(levelId);
                // 1. 查询段落实对应的文章ID
                GraphSequence sequence = graphSequenceMapper.selectBySequenceId(levelId);
                if (sequence == null) {
                    throw new RuntimeException("段落不存在，sequenceId：" + levelId);
                }
                String articleId = sequence.getArticleId();
                node.setArticleId(articleId);
                // 2. 查询文章对应的专题ID
                Article sequenceArticle = graphArticleMapper.selectByArticleId(articleId);
                if (sequenceArticle == null) {
                    throw new RuntimeException("文章不存在，articleId：" + articleId);
                }
                String sequenceTopicId = sequenceArticle.getTopicId();
                node.setTopicId(sequenceTopicId);
                // 3. 查询专题对应的领域ID
                Topic sequenceTopic = graphTopicMapper.selectByTopicId(sequenceTopicId);
                if (sequenceTopic == null) {
                    throw new RuntimeException("专题不存在，topicId：" + sequenceTopicId);
                }
                node.setFieldId(sequenceTopic.getFieldId());
                break;
            default:
                throw new RuntimeException("不支持的层级：" + level);
        }
        return node;
    }

    private void saveNodeProperties(Long nodeId, List<NodeSaveDTO.GraphNodePropertyDTO> propertyDTOs) {
        if (CollectionUtils.isEmpty(propertyDTOs)) {
            return;
        }

        List<GraphNodeProperty> properties = propertyDTOs.stream()
                .map(dto -> {
                    GraphNodeProperty property = new GraphNodeProperty();
                    property.setNodeId(nodeId);
                    property.setPropertyKey(dto.getPropertyKey());
                    property.setPropertyValue(dto.getPropertyValue());
                    property.setCreateTime(LocalDateTime.now());
                    property.setUpdateTime(LocalDateTime.now());
                    return property;
                })
                .collect(Collectors.toList());

        graphNodePropertyMapper.batchInsert(properties);
    }

}
