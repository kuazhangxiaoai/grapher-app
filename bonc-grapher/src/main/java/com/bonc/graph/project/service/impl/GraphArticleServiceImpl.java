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
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import javax.annotation.Resource;
import javax.validation.ValidationException;
import java.io.*;
import java.net.URL;
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
        //查询是否有相同名称的图谱
        String oldArticleName = articleDto.getArticleName();
        if(oldArticleName==null||"".equals(oldArticleName)){
            throw new ValidationException("图谱名称不能为空，请输入图谱名称");
        }
        String oldTopicId = articleDto.getTopicId();
        Article oldArticle= graphArticleMapper.selectByArticleName(oldArticleName,oldTopicId);
        if(oldArticle!=null){
            throw new ValidationException("该专题下已存在名称为【" + oldArticleName + "】的图谱，请勿重复创建");
        }
        //新增图谱
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
        if("0".equals(createMethod)){
            if(file==null || file.isEmpty()){
                throw new ValidationException("您选择的是自文本创建，文件不能为空");
            }else{
                // 获取原始文件名
                String originalFilename = file.getOriginalFilename();
                originalFilename = (originalFilename == null) ? "" : originalFilename;
                // 获取文件后缀名
                String suffix = getFileSuffix(originalFilename);
                suffix = suffix.replace(".", "").toLowerCase();

                if(!"txt".equals(suffix)&&!"pdf".equals(suffix)){
                    throw new ValidationException("只能上传txt或者pdf文件,请重新选择");
                }
                // 生成新的唯一文件名
                String newFileName = UUID.randomUUID().toString() +"."+ suffix;
                // 拼接绝对路径
                String filePath = FILE_SAVE + File.separator + newFileName;

                if("txt".equals(suffix)){
                    //将txt转成pdf
                    try {
                        // 生成 PDF 文件名
                        String pdfFileName = UUID.randomUUID().toString() + ".pdf";
                        String pdfPath = FILE_SAVE + File.separator + pdfFileName;

                        // 确保目录存在
                        File pdfFile = new File(pdfPath);
                        if (!pdfFile.getParentFile().exists()) {
                            pdfFile.getParentFile().mkdirs();
                        }

                        // 执行转换pdf + 保存
                        txtToPdfAndSave(file.getInputStream(), pdfPath);

                        // 更新文件信息
                        newFileName = pdfFileName;
                        suffix = "pdf";

                    } catch (Exception e) {
                        throw new RuntimeException("TXT 转 PDF 失败：" + e.getMessage());
                    }
                }else {
                    //传入的是pdf 不用转
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
                }
                article.setFileName(originalFilename); // 原始文件名
                article.setFileUrl(newFileName);          // 文件相对路径
                article.setFileType(suffix);           // 文件后缀
                article.setFileSize(String.valueOf(file.getSize())); // 实际文件大小（字节）
            }
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

    public static void txtToPdfAndSave(InputStream inputStream, String outputPath) throws Exception {
        // 创建 PDF
        PdfWriter writer = new PdfWriter(outputPath);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);
        InputStream fontStream = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream("fonts/STFANGSO.TTF");

        if (fontStream == null) {
            throw new RuntimeException("字体文件没找到！");
        }


        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = fontStream.read(buffer)) != -1) {
            baos.write(buffer, 0, len);
        }

        byte[] fontBytes = baos.toByteArray();

        PdfFont font = PdfFontFactory.createFont(
                fontBytes,
                PdfEncodings.IDENTITY_H
        );

        document.setFont(font);

        // 读取 txt
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        String line;

        while ((line = reader.readLine()) != null) {
            document.add(new Paragraph(line));
        }

        reader.close();
        document.close();
    }
}