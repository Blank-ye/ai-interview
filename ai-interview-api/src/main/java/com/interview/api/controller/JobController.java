package com.interview.api.controller;

import com.interview.api.dto.response.JobMatchVO;
import com.interview.api.dto.response.JobVO;
import com.interview.common.result.Result;
import com.interview.dao.entity.Job;
import com.interview.dao.mapper.JobMapper;
import com.interview.service.job.JobService;
import com.interview.service.match.JobMatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 职位管理接口
 * 提供职位CRUD和简历-职位匹配功能
 */
@RestController
@RequestMapping("/api/job")
@RequiredArgsConstructor
public class JobController {

    private final JobMapper jobMapper;
    private final JobService jobService;
    private final JobMatchService jobMatchService;

    /**
     * 添加职位
     */
    @PostMapping
    public Result<JobVO> createJob(@RequestBody Job job) {
        Job created = jobService.createJob(job);
        return Result.success(toVO(created));
    }

    /**
     * 更新职位
     */
    @PutMapping("/{id}")
    public Result<JobVO> updateJob(@PathVariable Long id, @RequestBody Job job) {
        Job existing = jobMapper.selectById(id);
        if (existing == null) {
            return Result.error(404, "职位不存在");
        }

        job.setId(id);
        Job updated = jobService.updateJob(job);
        return Result.success(toVO(updated));
    }

    /**
     * 删除职位
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteJob(@PathVariable Long id) {
        jobService.deleteJob(id);
        return Result.success();
    }

    @GetMapping("/list")
    public Result<List<JobVO>> getJobList() {
        List<Job> jobs = jobMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Job>()
                        .eq("status", 1)
                        .orderByDesc("create_time"));

        List<JobVO> voList = jobs.stream().map(this::toVO).toList();
        return Result.success(voList);
    }

    @GetMapping("/{id}")
    public Result<JobVO> getJob(@PathVariable Long id) {
        Job job = jobMapper.selectById(id);
        if (job == null) {
            return Result.error(404, "职位不存在");
        }
        return Result.success(toVO(job));
    }

    @GetMapping("/match")
    public Result<List<JobMatchVO>> matchJobs(@RequestParam Long resumeId,
                                               @RequestParam(defaultValue = "10") int limit) {
        List<JobMatchService.JobMatchResult> results = jobMatchService.matchJobs(resumeId, limit);

        List<JobMatchVO> voList = results.stream().map(result -> {
            JobMatchVO vo = new JobMatchVO();
            vo.setJobId(result.getJobId());
            vo.setJobTitle(result.getJobTitle());
            vo.setCompany(result.getCompany());
            vo.setScore(result.getScore());
            return vo;
        }).toList();

        return Result.success(voList);
    }

    private JobVO toVO(Job job) {
        JobVO vo = new JobVO();
        vo.setId(job.getId());
        vo.setTitle(job.getTitle());
        vo.setCompany(job.getCompany());
        vo.setDescription(job.getDescription());
        vo.setRequirements(job.getRequirements());
        vo.setSalaryRange(job.getSalaryRange());
        vo.setLocation(job.getLocation());
        return vo;
    }
}
