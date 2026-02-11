package com.bonc.graph.project.controller;

import com.bonc.common.core.domain.model.GraphUser;
import com.bonc.common.core.domain.model.LoginUser;
import com.bonc.common.core.domain.model.PPTLoginUser;
import com.bonc.graph.project.domain.Field;
import com.bonc.graph.project.service.GraphFieldService;
import com.bonc.graph.user.domain.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 领域
 */
@RestController
@RequestMapping("/graph/field")
public class GraphFieldController {

    @Autowired
    private GraphFieldService graphFieldService;

    /**
     * 增加领域
     * @param field
     * @param loginUser
     * @return
     */
    @PostMapping("/addField")
    public Result addField(@RequestBody Field field,  @AuthenticationPrincipal PPTLoginUser loginUser){
        Result result = new Result();
        try {
//            GraphUser user = loginUser.getUser();
//            String userName = user.getUserName();
//            field.setCreateBy(userName);
            result.successResult(graphFieldService.addField(field));
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
    @GetMapping(value = "/selectFieldByCondition")
    public Result fieldSearch(@RequestParam(required = false) String condition)
    {
        Result result = new Result();
        try {
            result.successResult(graphFieldService.selectFieldByCondition(condition));
        }catch (Exception e){
            result.failResult(e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 删除领域
     * @param fieldId 领域ID
     * @return
     */
    @GetMapping("/remove")
    public Result remove(@RequestParam("fieldId") String fieldId)
    {
        Result result = new Result();
        try {
            result.successResult(graphFieldService.deleteByFieldId(fieldId));
        }catch (Exception e){
            result.failResult(e.getMessage());
            e.printStackTrace();
        }
        return result;
    }


}
