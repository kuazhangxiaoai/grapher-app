package com.bonc.graph.sequence.controller;

import com.bonc.common.utils.StringUtils;
import com.bonc.graph.sequence.domain.GraphSequence;
import com.bonc.graph.sequence.dto.GraphResponseDTO;
import com.bonc.graph.sequence.dto.GraphSaveDTO;
import com.bonc.graph.sequence.service.GraphCoreService;
import com.bonc.graph.sequence.service.GraphSequenceService;
import com.bonc.graph.user.domain.Result;
import com.bonc.graph.utils.HanLPSegmentUtil;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 图谱核心接口
 */
@RestController
@RequestMapping("/graph_api/v1/sequence")
public class GraphController {

    @Resource
    private GraphSequenceService graphSequenceService;
    @Resource
    private GraphCoreService graphCoreService;

    // 1. 图谱构建-段落分词接口开发
    @PostMapping("/segmentSequence")
    public Result segmentSequence(@RequestBody Map<String,Object> reqMap) {
        Result result = new Result();
        try {
            String sequenceContent = (String) reqMap.get("sequenceContent");
            if (StringUtils.isEmpty(sequenceContent)) {
                throw new IllegalArgumentException("内容不能为空");
            }
            // 调用分词工具类处理
            List<String> segmentResult = HanLPSegmentUtil.segmentParagraph(sequenceContent);
            result.successResult(segmentResult);
        } catch (Exception e) {
            result.failResult(e.getMessage());
            e.printStackTrace();
        }
        return result;
    }


    /**
     * 2. 图谱构建-段落列表查询接口
     * @param articleId 文章ID
     * @return 段落列表
     */
    @GetMapping("/getSequenceList")
    public Result getSequenceList(@RequestParam String articleId) {
        Result result = new Result();
        try {
            List<GraphSequence> resList = graphSequenceService.getSequenceListByArticleId(articleId);
            result.successResult(resList);
        }catch (Exception e){
            result.failResult(e.getMessage());
            e.printStackTrace();
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
}
