package com.bonc.graph.project.service.impl;

import com.bonc.common.config.RuoYiConfig;
import com.bonc.common.utils.DateUtils;
import com.bonc.graph.project.domain.Article;
import com.bonc.graph.project.dto.ArticleDto;
import com.bonc.graph.project.mapper.GraphArticleMapper;
import com.bonc.graph.project.service.GraphArticleService;
import com.bonc.graph.sequence.domain.GraphSequence;
import com.bonc.graph.sequence.mapper.GraphSequenceMapper;
import com.bonc.graph.sequence.service.GraphNodeService;
import com.bonc.graph.sequence.service.GraphRelationService;
import com.bonc.graph.sequence.service.GraphSequenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class GraphArticleServiceImpl implements GraphArticleService {

    @Autowired
    private GraphArticleMapper graphArticleMapper;
    @Resource
    private GraphSequenceMapper graphSequenceMapper;
    @Autowired
    private GraphNodeService graphNodeService;
    @Autowired
    private GraphRelationService graphRelationService;
    @Autowired
    private GraphSequenceService graphSequenceService;

    @Value("${fileSave.path}")
    private String FILE_SAVE;
    @Value("${img.url}")
    private String BASE_URL;
    @Override
    public List<Map<String, Object>> selectArticle(String condition,String topicId) {
        return graphArticleMapper.selectArticle(condition,topicId);
    }

    @Override
    public String getFileUrl(String articleId) {
        String url = graphArticleMapper.getFileUrl(articleId);
        if(url == null || url.isEmpty() )
        {
            return null;
        }
        else {
            return BASE_URL+url;
        }

    }

    /*新增图谱*/
    @Override
    public String addArticle(ArticleDto articleDto, String userName) {
        Article article = new Article();
        // 图谱id
        String articleId = UUID.randomUUID().toString();
        article.setArticleId(articleId);

        // 图谱名字
        String articleName = articleDto.getArticleName();
        article.setArticleName(articleName);

        // 创建方式
        String createMethod = articleDto.getCreateMethod();
        createMethod = (createMethod == null) ? "" : createMethod.trim();
        article.setCreateMethod(createMethod);


        // 处理文件上传
        MultipartFile file = articleDto.getMultipartFile();
        if (file != null && !file.isEmpty()) { // 非空校验
            // 获取文件保存基础路径
            String fileSavePath = RuoYiConfig.getUploadPath();
            // 获取原始文件名
            String originalFilename = file.getOriginalFilename();
            originalFilename = (originalFilename == null) ? "" : originalFilename;

            // 获取文件后缀名
            String suffix = getFileSuffix(originalFilename);

            // 生成新的唯一文件名
            String newFileName = UUID.randomUUID().toString() + suffix;
            // 拼接绝对路径
            String filePath = FILE_SAVE + File.separator + newFileName;

            // 将文件写入到文件夹中
            File destFile = new File(filePath);
            try {
                if (!destFile.getParentFile().exists()) {
                    destFile.getParentFile().mkdirs();
                }
                file.transferTo(destFile);
            } catch (IOException e) {
                throw new RuntimeException("文件上传失败：" + e.getMessage());
            }

            article.setFileName(originalFilename); // 原始文件名
            article.setFileUrl(newFileName);          // 文件相对路径
            article.setFileType(suffix);           // 文件后缀
            article.setFileSize(String.valueOf(file.getSize())); // 实际文件大小（字节）
        }


        article.setTopicId(articleDto.getTopicId());
        article.setCreateBy(userName);

        // 将article保存到数据库
        graphArticleMapper.addArticle(article);
        return articleId;
    }

    /*更新图谱*/
    @Override
    public int updateArticle(Article article) {
        article.setUpdateTime(DateUtils.getNowDate());
        return graphArticleMapper.updateArticle(article);
    }

    /*删除图谱*/
    @Override
    public int deleteArticle(Article article) {
        //根据articleId查找所有的sequenceId
        List<GraphSequence> graphSequences = graphSequenceMapper.selectByArticleId(article.getArticleId());
        if(graphSequences.size()>0){
            for(GraphSequence graphSequence:graphSequences){
                //删除段落
                graphSequenceService.deleteSequence(graphSequence.getSequenceId());
                //删除节点
                graphNodeService.deleteNodesBySequenceId(graphSequence.getSequenceId());
                //删除关系
                graphRelationService.deleteRelationsBySequenceId(graphSequence.getSequenceId());
            }
        }

        Article fileArticle = graphArticleMapper.selectByArticleId(article.getArticleId());
        if(fileArticle != null && fileArticle.getFileName()!=null&&!"".equals(fileArticle.getFileName())){
            // 获取文件存储的完整路径（和上传时的路径保持一致）
            String fileUrl = fileArticle.getFileUrl(); // 上传时保存的相对路径（newFileName）
            if (fileUrl != null && !fileUrl.isEmpty()) {
                // 拼接完整文件路径（和addArticle中的FILE_SAVE对应）
                String fullFilePath = FILE_SAVE + File.separator + fileUrl;
                File fileToDelete = new File(fullFilePath);

                // 删除文件（避免文件不存在/权限问题导致报错）
                if (fileToDelete.exists()) { // 先判断文件是否存在
                    boolean isDeleted = fileToDelete.delete();
                    if (!isDeleted) {
                        throw new RuntimeException("文件删除失败：" + fullFilePath);
                    }
                } else {
                    // 文件不存在，仅打印日志（不中断流程）
                    System.out.println("文件不存在，无需删除，路径：" + fullFilePath);
                }
            }
        }


        return graphArticleMapper.deleteArticle(article.getArticleId());
    }



    /**安全文件后缀名*/
    private String getFileSuffix(String originalFilename) {
        if (originalFilename == null || originalFilename.isEmpty()) {
            return "";
        }
        int lastDotIndex = originalFilename.lastIndexOf(".");
        if (lastDotIndex <= 0 || lastDotIndex == originalFilename.length() - 1) {
            return "";
        }
        return originalFilename.substring(lastDotIndex);
    }
}