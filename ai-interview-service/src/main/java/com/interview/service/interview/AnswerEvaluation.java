package com.interview.service.interview;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 回答评估结果
 * AI评估输出：
 * - score: 总分（0-100）
 * - completeness: 完整性（0-1），关键点覆盖比例
 * - depth: 深度（0-1），回答详细程度
 * - evaluation: 文字评价
 * - keyPointsHit/Miss: 命中/遗漏的关键点
 */
@Data
@Builder
public class AnswerEvaluation {
    private Double score;
    private Double completeness;
    private Double depth;
    private String evaluation;
    private List<String> keyPointsHit;
    private List<String> keyPointsMiss;
}
