package com.bonc.graph.sequence.controller;

import com.bonc.common.utils.StringUtils;
import com.bonc.graph.sequence.dto.GraphResponseDTO;
import com.bonc.graph.sequence.dto.GraphSaveDTO;
import com.bonc.graph.sequence.dto.GraphSequenceDTO;
import com.bonc.graph.sequence.service.GraphCoreService;
import com.bonc.graph.sequence.service.GraphNodeService;
import com.bonc.graph.sequence.service.GraphRelationService;
import com.bonc.graph.sequence.service.GraphSequenceService;
import com.bonc.graph.user.domain.Result;
import com.bonc.graph.utils.HanLPSegmentUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 图谱核心接口
 */
@Slf4j
@RestController
@RequestMapping("/graph_api/v1/sequence")
public class GraphController {

    @Resource
    private GraphSequenceService graphSequenceService;
    @Resource
    private GraphCoreService graphCoreService;
    @Resource
    private GraphNodeService graphNodeService;
    @Resource
    private GraphRelationService graphRelationService;

    // 1. 图谱构建-段落分词接口
    @PostMapping("/segmentSequence")
    public Result segmentSequence(@RequestBody Map<String,Object> reqMap) {
        log.info("图谱构建-段落分词接口:/graph_api/v1/sequence/segmentSequence");
        Result result = new Result();
        try {
            String sequenceContent = (String) reqMap.get("sequenceContent");
            if (StringUtils.isEmpty(sequenceContent)) {
                throw new IllegalArgumentException("内容不能为空");
            }
            // 调用分词工具类处理
            List<Map<String, Object>> segmentResult = HanLPSegmentUtil.segmentParagraphWithOffset(sequenceContent);
            result.successResult(segmentResult);
        } catch (Exception e) {
            result.failResult(e.getMessage());
            e.printStackTrace();
        }
        return result;
    }


    /**
     * 段落列表查询接口
     */
    @GetMapping("/getSequenceList")
    public Result getSequenceList(@RequestParam String articleId) {
        log.info("图谱构建-段落列表查询接口:/graph_api/v1/sequence/getSequenceList, articleId:{}", articleId);
        Result result = new Result();
        try {
            List<GraphSequenceDTO> sequenceList = graphSequenceService.getSequenceListByArticleId(articleId);
            result.successResult(sequenceList);
        } catch (Exception e) {
            result.failResult(e.getMessage());
            log.error("段落列表查询失败, articleId:{}", articleId, e);
        }
        return result;
    }

    /**
     * 3. 图谱构建-段落对应图谱保存提交接口
     * @param saveDTO 保存参数
     * @return 成功提示
     */
    @PostMapping("/saveGraph")
    public Result saveGraph(@RequestBody GraphSaveDTO saveDTO) {
        log.info("图谱构建-段落对应图谱保存提交接口:/graph_api/v1/sequence/saveGraph");
        Result result = new Result();
        try {
            graphCoreService.saveGraph(saveDTO);
            result.successResult();
        }catch (Exception e){
            result.failResult(e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 4. 图谱构建-段落对应图谱查询接口
     * @param sequenceId 段落ID
     * @return 图谱数据
     */
    @GetMapping("/getGraphBySequenceId")
    public Result getGraphBySequenceId(@RequestParam String sequenceId) {
        log.info("图谱构建-段落对应图谱查询接口:/graph_api/v1/sequence/getGraphBySequenceId");
        Result result = new Result();
        try {
            GraphResponseDTO res = graphCoreService.getGraphBySequenceId(sequenceId);
            result.successResult(res);
        }catch (Exception e){
            result.failResult(e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 5. 图谱构建-文章对应图谱查询接口
     * @param articleId 文章ID
     * @return 图谱数据（去重）
     */
    @GetMapping("/getGraphByArticleId")
    public Result getGraphByArticleId(@RequestParam String articleId) {
        log.info("图谱构建-文章对应图谱查询接口:/graph_api/v1/sequence/getGraphByArticleId");
        Result result = new Result();
        try {
            GraphResponseDTO res = graphCoreService.getGraphByArticleId(articleId);
            result.successResult(res);
        }catch (Exception e){
            result.failResult(e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 6. 图谱构建-专题对应图谱查询接口
     * @param topicId 专题ID
     * @return 图谱数据（去重）
     */
    @GetMapping("/getGraphByTopicId")
    public Result getGraphByTopicId(@RequestParam String topicId) {
        log.info("图谱构建-专题对应图谱查询接口:/graph_api/v1/sequence/getGraphByTopicId");
        Result result = new Result();
        try {
            GraphResponseDTO res = graphCoreService.getGraphByTopicId(topicId);
            result.successResult(res);
        }catch (Exception e){
            result.failResult(e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 7. 图谱构建-领域对应图谱查询接口
     * @param fieldId 领域ID
     * @return 图谱数据（去重）
     */
    @GetMapping("/getGraphByFieldId")
    public Result getGraphByFieldId(@RequestParam String fieldId) {
        log.info("图谱构建-领域对应图谱查询接口:/graph_api/v1/sequence/getGraphByFieldId");
        Result result = new Result();
        try {
            GraphResponseDTO res = graphCoreService.getGraphByFieldId(fieldId);
            result.successResult(res);
        }catch (Exception e){
            result.failResult(e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 8. 图谱构建-模糊查询节点名称接口
     * @param articleId 文章ID
     * @param nodeName 模糊名称
     * @return
     */
    @GetMapping("/getNodeNamesByArticleId")
    public Result getNamesByArticleId(@RequestParam String articleId,@RequestParam String nodeName) {
        log.info("图谱构建-文章对应模糊查询节点名称接口:/graph_api/v1/sequence/getNodeNamesByArticleId");
        Result result = new Result();
        try {
            result.successResult(graphNodeService.getNodeNamesByArticleId(articleId,nodeName));
        }catch (Exception e){
            result.failResult(e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 9. 图谱构建-模糊查询关系名称接口
     * @param articleId 文章ID
     * @param relationName 模糊名称
     * @return
     */
    @GetMapping("/getRelationNamesByArticleId")
    public Result getRelationNamesByArticleId(@RequestParam String articleId,@RequestParam String relationName) {
        log.info("图谱构建-文章对应模糊查询关系名称接口:/graph_api/v1/sequence/getRelationNamesByArticleId");
        Result result = new Result();
        try {
            result.successResult(graphRelationService.getRelationNamesByArticleId(articleId,relationName));
        }catch (Exception e){
            result.failResult(e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

}
