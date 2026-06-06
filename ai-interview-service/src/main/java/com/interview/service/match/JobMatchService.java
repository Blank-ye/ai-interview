package com.interview.service.match;

import com.interview.dao.entity.Job;
import com.interview.dao.entity.Resume;
import com.interview.dao.mapper.JobMapper;
import com.interview.dao.mapper.ResumeMapper;
import com.interview.dao.repository.VectorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 职位匹配服务
 * 基于向量相似度匹配简历和职位：
 * 1. 将简历/职位文本向量化
 * 2. 存储到Qdrant
 * 3. 搜索最相似的职位/简历
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JobMatchService {

    private final JobMapper jobMapper;
    private final ResumeMapper resumeMapper;
    private final VectorRepository vectorRepository;
    private final EmbeddingModel embeddingModel;

    private static final String JOB_COLLECTION = "jobs";

    /**
     * 匹配职位
     */
    public List<JobMatchResult> matchJobs(Long resumeId, int limit) {
        Resume resume = resumeMapper.selectById(resumeId);
        if (resume == null || resume.getRawContent() == null) {
            return new ArrayList<>();
        }

        // 1. 向量化简历内容
        float[] embedding = embeddingModel.embed(resume.getRawContent());
        List<Float> vector= new ArrayList<>(embedding.length);
        for(float v:embedding){
            vector.add(v);
        }

        // 2. 搜索相似职位
        var results = vectorRepository.searchSimilar(JOB_COLLECTION, vector, limit);

        // 3. 构建匹配结果
        List<JobMatchResult> matchResults = new ArrayList<>();
        for (var point : results) {
            Long jobId = point.getPayloadMap().get("jobId").getIntegerValue();
            Job job = jobMapper.selectById(jobId);
            if (job != null) {
                matchResults.add(JobMatchResult.builder()
                        .jobId(jobId)
                        .jobTitle(job.getTitle())
                        .company(job.getCompany())
                        .score((double) point.getScore())
                        .build());
            }
        }

        return matchResults;
    }

    /**
     * 索引职位
     */
    public void indexJob(Long jobId) {
        Job job = jobMapper.selectById(jobId);
        if (job == null) {
            return;
        }

        // 构建职位文本
        String text = buildJobText(job);

        // 向量化
        float[] embedding = embeddingModel.embed(text);

        List<Float> vector= new ArrayList<>(embedding.length);
        for(float v:embedding){
            vector.add(v);
        }

        // 存储到Qdrant
        Map<String, io.qdrant.client.grpc.JsonWithInt.Value> payload = new java.util.HashMap<>();
        payload.put("jobId", io.qdrant.client.grpc.JsonWithInt.Value.newBuilder()
                .setIntegerValue(jobId)
                .build());

        String vectorId = vectorRepository.storeVector(JOB_COLLECTION, vector, payload);

        // 更新职位向量ID
        job.setVectorId(vectorId);
        jobMapper.updateById(job);
    }

    private String buildJobText(Job job) {
        StringBuilder sb = new StringBuilder();
        sb.append(job.getTitle()).append(" ");
        if (job.getDescription() != null) {
            sb.append(job.getDescription()).append(" ");
        }
        if (job.getRequirements() != null) {
            sb.append(job.getRequirements()).append(" ");
        }
        if (job.getSkills() != null) {
            sb.append(job.getSkills());
        }
        return sb.toString();
    }

    @lombok.Data
    @lombok.Builder
    public static class JobMatchResult {
        private Long jobId;
        private String jobTitle;
        private String company;
        private Double score;
    }
}
