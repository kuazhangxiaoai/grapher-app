package com.bonc.graph.project.service.impl;

import com.bonc.common.utils.DateUtils;
import com.bonc.common.utils.uuid.UUID;
import com.bonc.graph.project.domain.Article;
import com.bonc.graph.project.domain.Topic;
import com.bonc.graph.project.mapper.GraphArticleMapper;
import com.bonc.graph.project.mapper.GraphTopicMapper;
import com.bonc.graph.project.service.GraphTopicService;
import lombok.Data;
import net.sf.jsqlparser.statement.select.Top;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class GraphTopicServiceImpl implements GraphTopicService {

    @Autowired
    private GraphTopicMapper graphTopicMapper;
    @Autowired
    private GraphArticleMapper graphArticleMapper;

    /* 增加主题 */
    @Override
    public int addTopic(Topic topic) {
        String uuid = UUID.randomUUID().toString();
        topic.setTopicId(uuid);
        topic.setCreateTime(DateUtils.getNowDate());
        topic.setDelFlag("0");
        return graphTopicMapper.addTopic(topic);
    }

    /* 根据条件查询*/
    @Override
    public List<Map<String, Object>> selectTopicByCondition(String condition, String fieldId) {
        return graphTopicMapper.selectTopicByCondition(condition,fieldId);
    }
    /* 删除主题 */
    @Override
    public int deleteBytopicId(Topic topic) {
        topic.setUpdateTime(DateUtils.getNowDate());
        topic.setDelFlag("2");
        return graphTopicMapper.deleteBytopicId(topic);
    }

    /*复制主题*/
    @Override
    @Transactional
    public String copyTopic(String topicId, String newFieldId,String topicName, String userName) {
        String newTopicId = UUID.randomUUID().toString();
        //根据主题id进行查找
        Topic oldTopic = graphTopicMapper.selectTopicById(topicId);

        Topic newTopic = new Topic();
        newTopic.setTopicId(newTopicId);
        newTopic.setCreateBy(userName);
        if(!"".equals(topicName) && topicName !=null ){
            newTopic.setTopicName(topicName);
        }else {
            newTopic.setTopicName(oldTopic.topicName);
        }
        // 如果传进来领域id 就是复制领域时调用的该方法
        if(newFieldId!=null){
            newTopic.setFieldId(newFieldId);
        }
        newTopic.setFieldId(oldTopic.getFieldId());
        newTopic.setCreateTime(DateUtils.getNowDate());
        newTopic.setDelFlag("0");

        graphTopicMapper.addTopic(newTopic);

        copyArticle(topicId,newTopicId,userName);
        return newTopicId;
    }

    /*复制图谱*/
    public void copyArticle(String oldTopicId,String newTopicId,String username){
        // 1. 根据旧的topicId查询所有文章
        List<Article> oldArticles = graphArticleMapper.selectArticlesByTopicId(oldTopicId);

        if (oldArticles == null || oldArticles.isEmpty()) {
            return; // 没有文章需要复制，直接返回
        }

        // 2. 批量构建新文章列表
        List<Article> newArticles = new ArrayList<>(oldArticles.size());
        for (Article oldArticle : oldArticles) {
            Article newArticle = new Article();
            // 生成新的文章ID
            newArticle.setArticleId(UUID.randomUUID().toString());
            // 复制基础字段
            newArticle.setArticleName(oldArticle.getArticleName());
            newArticle.setCreateMethod(oldArticle.getCreateMethod());
            newArticle.setFileName(oldArticle.getFileName());
            newArticle.setFileType(oldArticle.getFileType());
            newArticle.setFileSize(oldArticle.getFileSize());
            newArticle.setFileUrl(oldArticle.getFileUrl());
            // 关联到新专题
            newArticle.setTopicId(newTopicId);
            newArticle.setCreateBy(username);
            newArticles.add(newArticle);
        }

        if (!newArticles.isEmpty()) {
            graphArticleMapper.batchInsertArticles(newArticles);
        }
    }


}
