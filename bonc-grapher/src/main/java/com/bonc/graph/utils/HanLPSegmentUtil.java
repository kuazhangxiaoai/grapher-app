package com.bonc.graph.utils;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;

import java.util.List;
import java.util.stream.Collectors;

/**
 * HanLP 分词工具类
 */
public class HanLPSegmentUtil {

    /**
     * 段落分词（过滤标点符号）
     * @param paragraph 待分词的段落文本
     * @return 过滤标点后的分词列表
     */
    public static List<String> segmentParagraph(String paragraph) {
        // 1. 使用 HanLP 进行分词（标准分词）
        List<Term> termList = HanLP.segment(paragraph);

        // 2. 过滤标点符号：保留非标点的词汇
        return termList.stream()
                .filter(term -> !isPunctuation(term.nature.toString())) // 过滤标点词性
                .map(term -> term.word) // 直接访问 word 字段，或使用 Term::getWord
                .collect(Collectors.toList());
    }

    /**
     * 判断是否为标点符号词性
     * HanLP 中标点的词性标签主要是：w（标点符号）、wkz（左括号）、wky（右括号）、wyz（右引号）、wyz（左引号）等
     * @param nature 词性标签
     * @return true=是标点，false=非标点
     */
    private static boolean isPunctuation(String nature) {
        // 核心判断：以 "w" 开头的词性都是标点相关
        return nature.startsWith("w");
    }
}
