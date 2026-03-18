package com.bonc.graph.sequence.service;

import com.bonc.graph.project.domain.Article;
import com.bonc.graph.project.domain.Topic;
import com.bonc.graph.project.mapper.GraphArticleMapper;
import com.bonc.graph.project.mapper.GraphTopicMapper;
import com.bonc.graph.sequence.domain.GraphNode;
import com.bonc.graph.sequence.domain.GraphRelation;
import com.bonc.graph.sequence.domain.GraphSequence;
import com.bonc.graph.sequence.dto.GraphResponseDTO;
import com.bonc.graph.sequence.dto.GraphSaveDTO;
import com.bonc.graph.sequence.mapper.GraphSequenceMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GraphCoreService {

    @Resource
    private GraphArticleMapper graphArticleMapper;
    @Resource
    private GraphTopicMapper graphTopicMapper;
    @Resource
    private GraphSequenceService graphSequenceService;
    @Resource
    private GraphSequenceMapper graphSequenceMapper;
    @Resource
    private GraphSequencePositionService graphSequencePositionService;
    @Resource
    private GraphNodeService graphNodeService;
    @Resource
    private GraphRelationService graphRelationService;

    /**
     * 图谱保存提交
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveGraph(GraphSaveDTO saveDTO) {
        String articleId = saveDTO.getArticleId();
        String sequenceId = saveDTO.getSequenceId();

        // 1. 校验文章存在
        Article article = graphArticleMapper.selectByArticleId(articleId);
        if (article == null) {
            throw new RuntimeException("文章不存在，articleId：" + articleId);
        }
        String topicId = article.getTopicId();

        // 2. 校验专题存在
        Topic topic = graphTopicMapper.selectByTopicId(topicId);
        if (topic == null) {
            throw new RuntimeException("专题不存在，topicId：" + topicId);
        }
        String fieldId = topic.getFieldId();

        // 3. 新增段落
        if (sequenceId == null || sequenceId.isEmpty()) {
            sequenceId = graphSequenceService.createSequence(articleId, saveDTO.getSequenceContent());
            // 批量插入位置信息
            graphSequencePositionService.batchCreateSequencePosition(sequenceId, saveDTO.getSequencePositionList());
        } else {
            // 4. 修改段落：先校验段落存在
            GraphSequence sequence = graphSequenceMapper.selectBySequenceId(sequenceId);
            if (sequence == null) {
                throw new RuntimeException("段落不存在，sequenceId：" + sequenceId);
            }
            // 删除旧数据
            graphNodeService.deleteNodesBySequenceId(sequenceId);
            graphRelationService.deleteRelationsBySequenceId(sequenceId);
//            graphSequencePositionService.deletePositionBySequenceId(sequenceId);
//            // 重新插入位置信息
//            graphSequencePositionService.batchCreateSequencePosition(sequenceId, saveDTO.getSequencePositionList());
            // 更新主表时间
            graphSequenceService.updateSequenceUpdateTime(sequenceId);
        }

        // 5. 保存节点和关系
        if (!CollectionUtils.isEmpty(saveDTO.getGraphNode())) {
            graphNodeService.saveNodes(saveDTO.getGraphNode(), sequenceId, articleId, topicId, fieldId);
        }
        if (!CollectionUtils.isEmpty(saveDTO.getGraphRelation())) {
            graphRelationService.saveRelations(saveDTO.getGraphRelation(), sequenceId, articleId, topicId, fieldId);
        }
    }

    /**
     * 根据sequenceId查询图谱
     */
    public GraphResponseDTO getGraphBySequenceId(String sequenceId) {
        // 1. 查询节点
        List<GraphNode> nodes = graphNodeService.getNodesBySequenceId(sequenceId);
        // 2. 查询关系
        List<GraphRelation> relations = graphRelationService.getRelationsBySequenceId(sequenceId);
        // 3. 转换为返回DTO
        return convertToGraphResponseDTO(nodes, relations);
    }

    /**
     * 根据articleId查询图谱（去重）
     */
    public GraphResponseDTO getGraphByArticleId(String articleId) {
        // 1. 查询节点（去重）
        List<GraphNode> nodes = graphNodeService.getDistinctNodesByArticleId(articleId);
        // 2. 查询关系（去重）
        List<GraphRelation> relations = graphRelationService.getDistinctRelationsByArticleId(articleId);
        // 3. 转换为返回DTO
        return convertToGraphResponseDTO(nodes, relations);
    }

    /**
     * 根据TopicId查询图谱（去重）
     */
    public GraphResponseDTO getGraphByTopicId(String topicId) {
        // 1. 查询节点（去重）
        List<GraphNode> nodes = graphNodeService.getDistinctNodesByTopicId(topicId);
        // 2. 查询关系（去重）
        List<GraphRelation> relations = graphRelationService.getDistinctRelationsByTopicId(topicId);
        // 3. 转换为返回DTO
        return convertToGraphResponseDTO(nodes, relations);
    }


    /**
     * 根据fieldId查询图谱（去重）
     */
    public GraphResponseDTO getGraphByFieldId(String fieldId) {
        // 1. 查询节点（去重）
        List<GraphNode> nodes = graphNodeService.getDistinctNodesByFieldId(fieldId);
        // 2. 查询关系（去重）
        List<GraphRelation> relations = graphRelationService.getDistinctRelationsByFieldId(fieldId);
        // 3. 转换为返回DTO
        return convertToGraphResponseDTO(nodes, relations);
    }

    /**
     * 转换为返回DTO
     */
    private GraphResponseDTO convertToGraphResponseDTO(List<GraphNode> nodes, List<GraphRelation> relations) {
        GraphResponseDTO responseDTO = new GraphResponseDTO();

        // 转换节点
        List<GraphResponseDTO.NodeResponseDTO> nodeDTOList = nodes.stream().map(node -> {
            GraphResponseDTO.NodeResponseDTO nodeDTO = new GraphResponseDTO.NodeResponseDTO();
            nodeDTO.setNodeHash(node.getNodeHash());
            nodeDTO.setNodeTemplateId(node.getNodeTemplateId());
            nodeDTO.setNodeTemplateName(node.getNodeTemplateName());
            nodeDTO.setNodeName(node.getNodeName());
            nodeDTO.setNodeDescription(node.getNodeDescription());
            nodeDTO.setNodeColor(node.getNodeColor());
            // 转换节点属性
            List<GraphResponseDTO.PropertyDTO> propDTOList = node.getProperties().stream().map(prop -> {
                GraphResponseDTO.PropertyDTO propDTO = new GraphResponseDTO.PropertyDTO();
                propDTO.setPropertyKey(prop.getPropertyKey());
                propDTO.setPropertyValue(prop.getPropertyValue());
                return propDTO;
            }).collect(Collectors.toList());
            nodeDTO.setProperties(propDTOList);
            return nodeDTO;
        }).collect(Collectors.toList());

        // 转换关系
        List<GraphResponseDTO.RelationResponseDTO> relationDTOList = relations.stream().map(relation -> {
            GraphResponseDTO.RelationResponseDTO relationDTO = new GraphResponseDTO.RelationResponseDTO();
            relationDTO.setRelationHash(relation.getRelationHash());
            relationDTO.setRelationTemplateId(relation.getRelationTemplateId());
            relationDTO.setRelationTemplateName(relation.getRelationTemplateName());
            relationDTO.setRelationName(relation.getRelationName());
            relationDTO.setRelationType(relation.getRelationType());
            relationDTO.setRelationTrigger(relation.getRelationTrigger());
            relationDTO.setStartNodeHash(relation.getStartNodeHash());
            relationDTO.setEndNodeHash(relation.getEndNodeHash());
            // 转换关系属性
            List<GraphResponseDTO.PropertyDTO> propDTOList = relation.getProperties().stream().map(prop -> {
                GraphResponseDTO.PropertyDTO propDTO = new GraphResponseDTO.PropertyDTO();
                propDTO.setPropertyKey(prop.getPropertyKey());
                propDTO.setPropertyValue(prop.getPropertyValue());
                return propDTO;
            }).collect(Collectors.toList());
            relationDTO.setProperties(propDTOList);
            return relationDTO;
        }).collect(Collectors.toList());

        responseDTO.setNodes(nodeDTOList);
        responseDTO.setRelations(relationDTOList);
        return responseDTO;
    }
}
