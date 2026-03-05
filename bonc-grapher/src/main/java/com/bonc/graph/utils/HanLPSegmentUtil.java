package com.bonc.graph.utils;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.Segment;
import com.hankcs.hanlp.seg.common.Term;

import java.util.*;
import java.util.stream.Collectors;

/**
 * HanLP 分词工具类
 */
public class HanLPSegmentUtil {

    // 定义需要过滤的词性集合（标点+助词+连词+介词+语气词+拟声词+叹词+状态词）
    private static final Set<String> FILTERED_NATURES = new HashSet<>(
            Arrays.asList("w", "u", "c", "p", "y", "o", "e", "zg")
    );

    /**
     * 段落分词（过滤指定无用词性）
     * @param paragraph 待分词的段落文本
     * @return 过滤后保留的有效分词列表
     */
    public static List<String> segmentParagraph(String paragraph) {
        // 1. 使用 HanLP 进行标准分词
        List<Term> termList = HanLP.segment(paragraph);

        // 2. 过滤指定词性，仅保留有效词汇
        return termList.stream()
                .filter(term -> !isFilteredNature(term.nature.toString())) // 过滤指定词性
                .map(term -> term.word) // 直接访问word字段（若报错可替换为term.getWord()）
                .collect(Collectors.toList());
    }

    /**
     * 段落分词（过滤指定无用词性）- 返回 List<Map> 格式的分词结果（包含正确偏移量）
     * 适配 HanLP portable-1.8.4 版本
     * @param paragraph 待分词的段落文本
     * @return 过滤后保留的有效分词列表，每个Map包含 "word"（词汇）和 "offset"（偏移量）两个key
     */
    public static List<Map<String, Object>> segmentParagraphWithOffset(String paragraph) {
        // 空值&空文本校验
        if (paragraph == null || paragraph.trim().length() == 0) {
            return Collections.emptyList();
        }

        // 1. 初始化1.8.4版本专用分词器（强制开启偏移量，核心修复）
        Segment segment = HanLP.newSegment();
        segment.enableOffset(true); // 显式开启偏移量计算（1.8.4版本必须手动开启）
        segment.enableCustomDictionary(false); // 关闭自定义词典（避免干扰基础分词）
        segment.enablePlaceRecognize(true);   // 可选：开启地名识别，提升分词准确性
        segment.enableNameRecognize(true);    // 可选：开启人名识别，匹配你的测试文本场景

        // 2. 执行分词，获取带正确偏移量的Term列表（1.8.4版本seg方法返回正确offset）
        List<Term> termList = segment.seg(paragraph);

        // 3. 过滤+封装Map（严格适配1.8.4版本的Term属性）
        return termList.stream()
                // 过滤：增加nature非空校验+过滤指定词性
                .filter(term -> term.nature != null && !isFilteredNature(term.nature.toString()))
                .map(term -> {
                    Map<String, Object> wordMap = new HashMap<>(2);
                    // 1.8.4版本Term的offset字段为public，可直接访问
                    wordMap.put("offset", term.offset);
                    wordMap.put("word", term.word);
                    return wordMap;
                })
                .collect(Collectors.toList());
    }

    /**
     * 判断词性是否需要过滤
     * @param nature 词性标签（如：w、u、c等）
     * @return true=需要过滤，false=保留
     */
    private static boolean isFilteredNature(String nature) {
        // 核心逻辑：判断词性是否在过滤集合中
        // 兼容词性标签带后缀的情况（如：wkz、ug等），仅取前1-2位匹配
        String naturePrefix = nature.length() > 2 ? nature.substring(0, 2) : nature;
        return FILTERED_NATURES.contains(nature) || FILTERED_NATURES.contains(naturePrefix);
    }
}
