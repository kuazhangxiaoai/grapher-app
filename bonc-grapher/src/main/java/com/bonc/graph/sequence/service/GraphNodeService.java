package com.bonc.graph.sequence.service;

import com.bonc.graph.sequence.domain.GraphNode;
import com.bonc.graph.sequence.domain.GraphNodeProperty;
import com.bonc.graph.sequence.dto.GraphSaveDTO;
import com.bonc.graph.sequence.mapper.GraphNodeMapper;
import com.bonc.graph.sequence.mapper.GraphNodePropertyMapper;
import com.bonc.graph.utils.HashUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class GraphNodeService {

    @Resource
    private GraphNodeMapper graphNodeMapper;
    @Resource
    private GraphNodePropertyMapper graphNodePropertyMapper;

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
                    property.setUpdateTime(now.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli());
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
        List<GraphNode> nodes = graphNodeMapper.selectBySequenceId(sequenceId);
        // 填充属性
        for (GraphNode node : nodes) {
            List<GraphNodeProperty> properties = graphNodePropertyMapper.selectByNodeId(node.getNodeId());
            node.setProperties(properties);
        }
        return nodes;
    }

    /**
     * 根据articleId查询节点（去重，含属性）
     */
    public List<GraphNode> getDistinctNodesByArticleId(String articleId) {
        List<GraphNode> nodes = graphNodeMapper.selectDistinctByArticleId(articleId);
        // 填充属性
        for (GraphNode node : nodes) {
            List<GraphNodeProperty> properties = graphNodePropertyMapper.selectByNodeId(node.getNodeId());
            node.setProperties(properties);
        }
        return nodes;
    }


    /**
     * 根据TopicId查询节点（去重，含属性）
     */
    public List<GraphNode> getDistinctNodesByTopicId(String topicId) {
        List<GraphNode> nodes = graphNodeMapper.selectDistinctByTopicId(topicId);
        // 填充属性
        for (GraphNode node : nodes) {
            List<GraphNodeProperty> properties = graphNodePropertyMapper.selectByNodeId(node.getNodeId());
            node.setProperties(properties);
        }
        return nodes;
    }
}
