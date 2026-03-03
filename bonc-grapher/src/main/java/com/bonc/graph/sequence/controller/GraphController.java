package com.bonc.graph.sequence.controller;

import com.bonc.graph.sequence.domain.GraphSequence;
import com.bonc.graph.sequence.dto.GraphResponseDTO;
import com.bonc.graph.sequence.dto.GraphSaveDTO;
import com.bonc.graph.sequence.service.GraphCoreService;
import com.bonc.graph.sequence.service.GraphSequenceService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

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

    /**
     * 1. 段落列表查询接口
     * @param articleId 文章ID
     * @return 段落列表
     */
    @GetMapping("/getSequenceList")
    public List<GraphSequence> getSequenceList(@RequestParam String articleId) {
        return graphSequenceService.getSequenceListByArticleId(articleId);
    }

    /**
     * 2. 图谱保存提交接口
     * @param saveDTO 保存参数
     * @return 成功提示
     */
    @PostMapping("/saveGraph")
    public String saveGraph(@RequestBody GraphSaveDTO saveDTO) {
        graphCoreService.saveGraph(saveDTO);
        return "success";
    }

    /**
     * 3. 根据sequenceId查询图谱接口
     * @param sequenceId 段落ID
     * @return 图谱数据
     */
    @GetMapping("/getGraphBySequenceId")
    public GraphResponseDTO getGraphBySequenceId(@RequestParam String sequenceId) {
        return graphCoreService.getGraphBySequenceId(sequenceId);
    }

    /**
     * 4. 根据articleId查询图谱接口
     * @param articleId 文章ID
     * @return 图谱数据（去重）
     */
    @GetMapping("/getGraphByArticleId")
    public GraphResponseDTO getGraphByArticleId(@RequestParam String articleId) {
        return graphCoreService.getGraphByArticleId(articleId);
    }
}
