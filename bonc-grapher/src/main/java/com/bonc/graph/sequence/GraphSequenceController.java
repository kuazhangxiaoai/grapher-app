package com.bonc.graph.sequence;

import com.bonc.common.utils.StringUtils;
import com.bonc.graph.user.domain.Result;
import com.bonc.graph.utils.HanLPSegmentUtil;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/graph_api/v1/sequence")
public class GraphSequenceController {

    // 分词接口
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
}
