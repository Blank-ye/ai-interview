package com.interview.api.controller;

import com.interview.api.dto.response.ResumeVO;
import com.interview.common.result.Result;
import com.interview.common.util.UserContext;
import com.interview.dao.entity.Resume;
import com.interview.dao.mapper.ResumeMapper;
import com.interview.service.resume.ResumeParseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 简历管理接口
 * 提供简历上传、查询功能，上传后异步解析
 */
@RestController
@RequestMapping("/api/resume")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeParseService resumeParseService;
    private final ResumeMapper resumeMapper;

    @PostMapping("/upload")
    public Result<ResumeVO> uploadResume(@RequestParam MultipartFile file) {
        Long userId = UserContext.getUserId();
        Resume resume = resumeParseService.uploadResume(userId, file);

        // 异步解析
        new Thread(() -> resumeParseService.parseResume(resume.getId())).start();

        ResumeVO vo = new ResumeVO();
        vo.setId(resume.getId());
        vo.setFileName(resume.getFileName());
        vo.setStatus(resume.getStatus());
        return Result.success(vo);
    }

    @GetMapping("/{id}")
    public Result<ResumeVO> getResume(@PathVariable Long id) {
        Resume resume = resumeMapper.selectById(id);
        if (resume == null) {
            return Result.error(1004, "简历不存在");
        }

        ResumeVO vo = new ResumeVO();
        vo.setId(resume.getId());
        vo.setFileName(resume.getFileName());
        vo.setStatus(resume.getStatus());
        vo.setStructuredContent(resume.getStructuredContent());
        return Result.success(vo);
    }

    @GetMapping("/list")
    public Result<List<ResumeVO>> getResumeList() {
        Long userId = UserContext.getUserId();
        List<Resume> resumes = resumeMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Resume>()
                        .eq("user_id", userId)
                        .orderByDesc("create_time"));

        List<ResumeVO> voList = resumes.stream().map(resume -> {
            ResumeVO vo = new ResumeVO();
            vo.setId(resume.getId());
            vo.setFileName(resume.getFileName());
            vo.setStatus(resume.getStatus());
            return vo;
        }).toList();

        return Result.success(voList);
    }
}
