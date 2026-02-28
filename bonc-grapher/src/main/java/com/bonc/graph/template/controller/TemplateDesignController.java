package com.bonc.graph.template.controller;

import com.bonc.graph.template.dto.AddToModelDTO;
import com.bonc.graph.template.dto.NodeTemplateSaveDTO;
import com.bonc.graph.template.dto.RelationTemplateSaveDTO;
import com.bonc.graph.template.service.TemplateDesignService;
import com.bonc.graph.user.domain.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/graph_api/v1/template")
public class TemplateDesignController {
    @Resource
    private TemplateDesignService templateDesignService;

    // 1. 节点/关系模版查询接口
    @GetMapping("/queryTemplate")
    public Result queryTemplate(@RequestParam String topicId) {
        log.info("节点/关系模版查询接口:/graph_api/v1/template/queryTemplate");
        log.info("topicId:"+ topicId);
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
        log.info("节点模版保存接口:/graph_api/v1/template/saveNodeTemplate");
        log.info("dto:"+ dto.toString());
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
        log.info("关系模版保存接口:/graph_api/v1/template/saveRelationTemplate");
        log.info("dto:"+ dto.toString());
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
        log.info("节点模版删除接口:/graph_api/v1/template/deleteNodeTemplate");
        Result result = new Result();
        try {
            Object nodeTemplateIdObj = reqMap.get("nodeTemplateId");
            if (nodeTemplateIdObj == null) {
                throw new IllegalArgumentException("节点模板ID不能为空");
            }
            Long nodeTemplateId =  Long.valueOf((Integer)nodeTemplateIdObj);
            log.info("nodeTemplateId:"+ nodeTemplateId);
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
        log.info("关系模版删除接口:/graph_api/v1/template/deleteRelationTemplate");
        Result result = new Result();
        try {
            Object relationTemplateIdObj = reqMap.get("relationTemplateId");
            if (relationTemplateIdObj == null) {
                throw new IllegalArgumentException("节点模板ID不能为空");
            }
            Long relationTemplateId =  Long.valueOf((Integer)relationTemplateIdObj);
            log.info("relationTemplateId:"+ relationTemplateId);
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
        log.info("组件库查询接口:/graph_api/v1/template/queryLibraryTemplate");
        log.info("templateName:"+ templateName);
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
        log.info("添加到模型接口:/graph_api/v1/template/addToModel");
        log.info("dto:"+ dto.toString());
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
