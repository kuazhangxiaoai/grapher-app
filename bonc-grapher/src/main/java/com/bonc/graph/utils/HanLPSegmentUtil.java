package com.bonc.graph.utils;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
