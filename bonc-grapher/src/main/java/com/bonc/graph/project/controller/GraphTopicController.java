package com.bonc.graph.project.controller;


import com.bonc.common.core.domain.model.GraphUser;
import com.bonc.common.core.domain.model.PPTLoginUser;
import com.bonc.graph.project.domain.Topic;
import com.bonc.graph.project.service.GraphTopicService;
import com.bonc.graph.user.domain.Result;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.statement.select.Top;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 领域
 */
@Slf4j
@RestController
@RequestMapping("/graph_api/v1/topic")
public class GraphTopicController {

    @Autowired
    private GraphTopicService graphTopicService;

    /**
     * 增加领域
     * @param topic
     * @param loginUser
     * @return
     */
    @PostMapping("/addTopic")
    public Result addtopic(@RequestBody Topic topic, @AuthenticationPrincipal PPTLoginUser loginUser){
        Result result = new Result();
        try {
            GraphUser user = loginUser.getUser();
            String userName = user.getUserName();
            topic.setCreateBy(userName);
            result.successResult(graphTopicService.addTopic(topic));
        }catch (Exception e){
            result.failResult(e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 根据条件查询
     * @param condition 条件
     * @return
     */
    @GetMapping(value = "/selectTopicByCondition")
    public Result topicSearch(@RequestParam(required = false) String condition,@RequestParam(required = false) String fieldId)
    {
        Result result = new Result();
        try {
            result.successResult(graphTopicService.selectTopicByCondition(condition,fieldId));
        }catch (Exception e){
            result.failResult(e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 删除专题
     * @param topicId 领域ID
     * @return
     */
    @GetMapping("/remove")
    public Result remove(@RequestParam("topicId") String topicId,@AuthenticationPrincipal PPTLoginUser loginUser)
    {
        Result result = new Result();
        try {
            log.info("开始调用专题删除接口:/graph_api/v1/topic/remove");
            Topic topic = new Topic();
            GraphUser user = loginUser.getUser();
            String userName = user.getUserName();
            topic.setUpdateBy(userName);
            topic.setTopicId(topicId);
            result.successResult(graphTopicService.deleteBytopicId(topic));
        }catch (Exception e){
            result.failResult(e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    @GetMapping("/copyTopic")
    public Result copyTopic(@RequestParam("topicId") String topicId,
                            @RequestParam(required = false, value = "topicName") String topicName,@AuthenticationPrincipal PPTLoginUser loginUser){
        Result result = new Result();
        try {
            GraphUser user = loginUser.getUser();
            String userName = user.getUserName();
            result.successResult(graphTopicService.copyTopic(topicId,null,topicName,userName));
        }catch (Exception e){
            result.failResult(e.getMessage());
            e.printStackTrace();
        }
        return result;
    }
}
