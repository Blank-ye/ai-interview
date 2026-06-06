package com.interview.api.controller;

import com.interview.api.dto.response.InterviewVO;
import com.interview.common.result.Result;
import com.interview.common.util.UserContext;
import com.interview.dao.entity.Interview;
import com.interview.dao.mapper.InterviewMapper;
import com.interview.service.interview.InterviewEngine;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 面试管理接口
 * 提供面试查询、报告获取功能
 * 面试交互通过WebSocket进行
 */
@RestController
@RequestMapping("/api/interview")
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewMapper interviewMapper;
    private final InterviewEngine interviewEngine;

    @GetMapping("/{id}")
    public Result<InterviewVO> getInterview(@PathVariable Long id) {
        Interview interview = interviewMapper.selectById(id);
        if (interview == null) {
            return Result.error(1006, "面试不存在");
        }

        InterviewVO vo = new InterviewVO();
        vo.setId(interview.getId());
        vo.setUserId(interview.getUserId());
        vo.setResumeId(interview.getResumeId());
        vo.setJobId(interview.getJobId());
        vo.setStatus(interview.getStatus());
        vo.setTotalScore(interview.getTotalScore());
        vo.setStartTime(interview.getStartTime());
        vo.setEndTime(interview.getEndTime());
        return Result.success(vo);
    }

    @GetMapping("/{id}/report")
    public Result<String> getReport(@PathVariable Long id) {
        Interview interview = interviewMapper.selectById(id);
        if (interview == null) {
            return Result.error(1006, "面试不存在");
        }

        String report = interviewEngine.generateReport(id);
        return Result.success(report);
    }

    @GetMapping("/list")
    public Result<List<InterviewVO>> getInterviewList() {
        Long userId = UserContext.getUserId();
        List<Interview> interviews = interviewMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Interview>()
                        .eq("user_id", userId)
                        .orderByDesc("create_time"));

        List<InterviewVO> voList = interviews.stream().map(interview -> {
            InterviewVO vo = new InterviewVO();
            vo.setId(interview.getId());
            vo.setResumeId(interview.getResumeId());
            vo.setJobId(interview.getJobId());
            vo.setStatus(interview.getStatus());
            vo.setTotalScore(interview.getTotalScore());
            vo.setStartTime(interview.getStartTime());
            vo.setEndTime(interview.getEndTime());
            return vo;
        }).toList();

        return Result.success(voList);
    }
}
