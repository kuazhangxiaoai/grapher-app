package com.bonc.graph.project.controller;

import com.bonc.common.core.domain.model.GraphUser;
import com.bonc.common.core.domain.model.PPTLoginUser;
import com.bonc.graph.project.domain.Article;
import com.bonc.graph.project.domain.Field;
import com.bonc.graph.project.dto.ArticleDto;
import com.bonc.graph.project.service.GraphArticleService;
import com.bonc.graph.user.domain.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/graph_api/v1/article")
public class GraphArticleController {

    @Autowired
    private GraphArticleService graphArticleService;

    /** 查找图谱 */
    @GetMapping("/selectArticle")
    public ResponseEntity<Object> selectArticle(@RequestParam(required = false) String condition, @RequestParam(required = false) String topicId){
        Map<String, Object> result = new HashMap<String, Object>();
        try {
            result.put("data", graphArticleService.selectArticle(condition,topicId));
            result.put("code", 200);
            return new ResponseEntity<Object>(result, HttpStatus.OK);
        }catch (Exception e){
            result.put("data", null);
            result.put("message", e.getMessage());
            result.put("code", 500);
            e.printStackTrace();
            return new ResponseEntity<Object>(result, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /** 新增图谱 */
    @PostMapping("/addArticle")
    public ResponseEntity<Object> addArticle(ArticleDto articleDto, @AuthenticationPrincipal PPTLoginUser loginUser){
        Map<String, Object> result = new HashMap<String, Object>();
        try {
            GraphUser user = loginUser.getUser();
            String userName = user.getUserName();
            result.put("data", graphArticleService.addArticle(articleDto,userName));
            result.put("code", 200);
            return new ResponseEntity<Object>(result, HttpStatus.OK);
        }catch (Exception e){
            result.put("data", null);
            result.put("message", e.getMessage());
            result.put("code", 500);
            e.printStackTrace();
            return new ResponseEntity<Object>(result, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /** 修改图谱 */
    @GetMapping("/updateArticle")
    public ResponseEntity<Object> updateArticle(@RequestParam String articleId,@RequestParam String articleName,@AuthenticationPrincipal PPTLoginUser loginUser){
        Map<String, Object> result = new HashMap<String, Object>();
        try {
            GraphUser user = loginUser.getUser();
            String updateBy = user.getUserName();
            Article article = new Article();
            article.setUpdateBy(updateBy);
            article.setArticleId(articleId);
            article.setArticleName(articleName);
            result.put("data", graphArticleService.updateArticle(article));
            result.put("code", 200);
            return new ResponseEntity<Object>(result, HttpStatus.OK);
        }catch (Exception e){
            result.put("data", null);
            result.put("message", e.getMessage());
            result.put("code", 500);
            e.printStackTrace();
            return new ResponseEntity<Object>(result, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /** 删除图谱 */
    @GetMapping("/deleteArticle")
    public ResponseEntity<Object> deleteArticle(@RequestParam String articleId,@AuthenticationPrincipal PPTLoginUser loginUser){
        Map<String, Object> result = new HashMap<String, Object>();
        try {
            GraphUser user = loginUser.getUser();
            String updateBy = user.getUserName();
            Article article = new Article();
            article.setUpdateBy(updateBy);
            article.setArticleId(articleId);
            result.put("data", graphArticleService.deleteArticle(article));
            result.put("code", 200);
            return new ResponseEntity<Object>(result, HttpStatus.OK);
        }catch (Exception e){
            result.put("data", null);
            result.put("message", e.getMessage());
            result.put("code", 500);
            e.printStackTrace();
            return new ResponseEntity<Object>(result, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /** 查询文件地址 */
    @GetMapping("/getFileUrl")
    public ResponseEntity<Object> getFileUrl(@RequestParam String articleId){
        Map<String, Object> result = new HashMap<String, Object>();
        try {
            String url = graphArticleService.getFileUrl(articleId);
            if(url == null || url.isEmpty()) {
                //NOT FOUND
                result.put("data", null);
                result.put("code", 404);
                return new ResponseEntity<Object>(result, HttpStatus.NOT_FOUND);
            }
            else {
                //SUCCESS
                result.put("data", url);
                result.put("code", 200);
                return new ResponseEntity<Object>(result, HttpStatus.OK);
            }

        }catch (Exception e){
            //ERROR
            result.put("data", null);
            result.put("message", e.getMessage());
            result.put("code", 500);
            e.printStackTrace();
            return new ResponseEntity<Object>(result, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
