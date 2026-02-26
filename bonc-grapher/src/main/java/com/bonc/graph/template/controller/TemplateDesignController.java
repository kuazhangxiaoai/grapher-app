package com.bonc.graph.template.controller;

import com.bonc.graph.template.dto.AddToModelDTO;
import com.bonc.graph.template.dto.NodeTemplateSaveDTO;
import com.bonc.graph.template.dto.RelationTemplateSaveDTO;
import com.bonc.graph.template.service.TemplateDesignService;
import com.bonc.graph.user.domain.Result;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("/graph_api/v1/template")
public class TemplateDesignController {
    @Resource
    private TemplateDesignService templateDesignService;

    // 1. 节点/关系模版查询接口
    @GetMapping("/queryTemplate")
    public Result queryTemplate(@RequestParam String topicId) {
        Result result = new Result();
        try {
            Map<String, Object> resMap = templateDesignService.queryTemplateByTopicId(topicId);
            result.successResult(resMap);
        } catch (Exception e) {
            result.failResult(e.getMessage());
            e.printStackTrace();
        }
        return result;
    }


    // 2. 节点模版保存接口
    @PostMapping("/saveNodeTemplate")
    public Result saveNodeTemplate(@Validated @RequestBody NodeTemplateSaveDTO dto){
        Result result = new Result();
        try {
            templateDesignService.saveNodeTemplate(dto);
            result.successResult();
        } catch (Exception e) {
            result.failResult(e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    // 3. 关系模版保存接口
    @PostMapping("/saveRelationTemplate")
    public Result saveRelationTemplate(@Validated @RequestBody RelationTemplateSaveDTO dto) {
        Result result = new Result();
        try {
            templateDesignService.saveRelationTemplate(dto);
            result.successResult();
        } catch (Exception e) {
            result.failResult(e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    // 4. 节点模版删除接口
    @PostMapping("/deleteNodeTemplate")
    public Result deleteNodeTemplate(@RequestBody Map<String,Object> reqMap) {
        Result result = new Result();
        try {
            Object nodeTemplateIdObj = reqMap.get("nodeTemplateId");
            if (nodeTemplateIdObj == null) {
                throw new IllegalArgumentException("节点模板ID不能为空");
            }
            Long nodeTemplateId =  Long.valueOf((String)nodeTemplateIdObj);
            templateDesignService.deleteNodeTemplate(nodeTemplateId);
            result.successResult();
        } catch (Exception e) {
            result.failResult(e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    // 5. 关系模版删除接口
    @PostMapping("/deleteRelationTemplate")
    public Result deleteRelationTemplate(@RequestBody Map<String,Object> reqMap) {
        Result result = new Result();
        try {
            Object relationTemplateIdObj = reqMap.get("relationTemplateId");
            if (relationTemplateIdObj == null) {
                throw new IllegalArgumentException("节点模板ID不能为空");
            }
            Long relationTemplateId =  Long.valueOf((String)relationTemplateIdObj);
            templateDesignService.deleteRelationTemplate(relationTemplateId);
            result.successResult();
        } catch (Exception e) {
            result.failResult(e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    // 6. 组件库查询接口
    @GetMapping("/queryLibraryTemplate")
    public Result queryLibraryTemplate(@RequestParam String templateName) {
        Result result = new Result();
        try {
            Map<String, Object> resMap = templateDesignService.queryLibraryTemplate(templateName);
            result.successResult(resMap);
        } catch (Exception e) {
            result.failResult(e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    // 7. 添加到模型接口
    @PostMapping("/addToModel")
    public Result addToModel(@Validated @RequestBody AddToModelDTO dto) {
        Result result = new Result();
        try {
            templateDesignService.addToModel(dto);
            result.successResult();
        } catch (Exception e) {
            result.failResult(e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

}
