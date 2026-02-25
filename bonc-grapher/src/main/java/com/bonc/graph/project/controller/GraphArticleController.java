package com.bonc.graph.project.controller;

import com.bonc.common.core.domain.model.GraphUser;
import com.bonc.common.core.domain.model.PPTLoginUser;
import com.bonc.graph.project.domain.Article;
import com.bonc.graph.project.domain.Field;
import com.bonc.graph.project.dto.ArticleDto;
import com.bonc.graph.project.service.GraphArticleService;
import com.bonc.graph.user.domain.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/graph_api/v1/article")
public class GraphArticleController {

    @Autowired
    private GraphArticleService graphArticleService;

    /** 查找图谱 */
    @PostMapping("/selectArticle")
    public Result selectArticle(){
        Result result = new Result();
        try {
            result.successResult(graphArticleService.selectArticle());
        }catch (Exception e){
            result.failResult(e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    /** 新增图谱 */
    @PostMapping("/addArticle")
    public Result addArticle(ArticleDto articleDto, @AuthenticationPrincipal PPTLoginUser loginUser){
        Result result = new Result();
        try {
            GraphUser user = loginUser.getUser();
            String userName = user.getUserName();
            result.successResult(graphArticleService.addArticle(articleDto,userName));
        }catch (Exception e){
            result.failResult(e.getMessage());
            e.printStackTrace();
        }
        return result;
    }


    /** 修改图谱 */
    @GetMapping("/updateArticle")
    public Result updateArticle(@RequestParam String articleId,@RequestParam String articleName,@AuthenticationPrincipal PPTLoginUser loginUser){
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
        Result result = new Result();
        try {
            GraphUser user = loginUser.getUser();
            String updateBy = user.getUserName();
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


}
