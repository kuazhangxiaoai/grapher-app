package com.bonc.graph.project.controller;

import com.bonc.common.core.domain.model.GraphUser;
import com.bonc.common.core.domain.model.PPTLoginUser;
import com.bonc.graph.project.domain.Article;
import com.bonc.graph.project.domain.Field;
import com.bonc.graph.project.dto.ArticleDto;
import com.bonc.graph.project.service.GraphArticleService;
import com.bonc.graph.user.domain.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/graph_api/v1/article")
public class GraphArticleController {

    @Autowired
    private GraphArticleService graphArticleService;

    /** 查找图谱 */
    @GetMapping("/selectArticle")
    public Result selectArticle(@RequestParam(required = false) String condition, @RequestParam(required = false) String topicId){
        Result result = new Result();
        try {
            result.successResult(graphArticleService.selectArticle(condition,topicId));
        }catch (Exception e){
            result.setResult("0001",e.getMessage(),null);
            e.printStackTrace();
        }
        return result;
    }

    /** 新增图谱 */
    @PostMapping("/addArticle")
    public Result addArticle(@ModelAttribute ArticleDto articleDto, @AuthenticationPrincipal PPTLoginUser loginUser){
        Result result = new Result();
        try {
            GraphUser user = loginUser.getUser();
            String userName = user.getUserName();
            String topicId = articleDto.getTopicId();
            if(topicId==null||"".equals(topicId)){
                throw new IllegalArgumentException("主题ID不能为空");
            }
            result.successResult(graphArticleService.addArticle(articleDto,userName));
        }catch (Exception e){
            result.setResult("0001",e.getMessage(),null);
            e.printStackTrace();
        }
        return result;
    }

    /** 修改图谱 */
    @GetMapping("/updateArticle")
    public Result updateArticle(@RequestParam String articleId,@RequestParam String articleName,@AuthenticationPrincipal PPTLoginUser loginUser){
        log.info("图谱修改接口:/graph_api/v1/article/updateArticle");
        Result result = new Result();
        try {
            GraphUser user = loginUser.getUser();
            String updateBy = user.getUserName();
            Article article = new Article();
            article.setUpdateBy(updateBy);
            article.setArticleId(articleId);
            article.setArticleName(articleName);
            result.successResult(graphArticleService.updateArticle(article));
        }catch (Exception e){
            result.failResult(e.getMessage());
            e.printStackTrace();
        }
        return result;
    }


    /** 删除图谱 */
    @GetMapping("/deleteArticle")
    public Result deleteArticle(@RequestParam String articleId,@AuthenticationPrincipal PPTLoginUser loginUser){
        log.info("图谱删除接口:/graph_api/v1/article/deleteArticle");
        Result result = new Result();
        try {
            GraphUser user = loginUser.getUser();
            String updateBy = user.getUserName();
            if (articleId == null) {
                throw new IllegalArgumentException("图谱ID不能为空");
            }
            Article article = new Article();
            article.setUpdateBy(updateBy);
            article.setArticleId(articleId);
            result.successResult(graphArticleService.deleteArticle(article));
        }catch (Exception e){
            result.failResult(e.getMessage());
            e.printStackTrace();
        }
        return result;
    }


    /** 查询文件地址 */
    @GetMapping("/getFileUrl")
    public Result getFileUrl(@RequestParam String articleId){
        log.info("查询文件地址接口:/graph_api/v1/article/getFileUrl");
        Result result = new Result();
        try {
            result.successResult(graphArticleService.getFileUrl(articleId));
        }catch (Exception e){
            result.failResult(e.getMessage());
            e.printStackTrace();
        }
        return result;
    }
}
