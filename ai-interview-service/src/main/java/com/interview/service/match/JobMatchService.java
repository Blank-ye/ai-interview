package com.interview.service.match;

import com.interview.dao.entity.Resume;
import com.interview.dao.mapper.ResumeMapper;
import com.interview.dao.repository.VectorRepository;
import com.interview.service.crawler.JobData;
import com.interview.service.crawler.RagStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 职位匹配服务
 * 从Qdrant检索相似职位，从RAG文档获取详情
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JobMatchService {

    private final ResumeMapper resumeMapper;
    private final VectorRepository vectorRepository;
    private final EmbeddingModel embeddingModel;
    private final RagStorage ragStorage;

    private static final String JOB_COLLECTION = "jobs";

    /**
     * 匹配职位（返回RAG中的职位数据）
     */
    public List<JobMatchResult> matchJobs(Long resumeId, int limit) {
        Resume resume = resumeMapper.selectById(resumeId);
        if (resume == null || resume.getRawContent() == null) {
            return new ArrayList<>();
        }

        // 1. 向量化简历内容
        float[] embedding = embeddingModel.embed(resume.getRawContent());
        List<Float> vector = new ArrayList<>(embedding.length);
        for (float v : embedding) {
            vector.add(v);
        }

        // 2. 从Qdrant搜索相似职位
        var results = vectorRepository.searchSimilar(JOB_COLLECTION, vector, limit);

        // 3. 提取jobId列表
        List<String> jobIds = new ArrayList<>();
        for (var point : results) {
            String jobId = point.getPayloadMap().get("jobId").getStringValue();
            jobIds.add(jobId);
        }

        // 4. 从RAG文档获取职位详情
        List<JobData> jobs = ragStorage.getByIds(jobIds);

        // 5. 构建匹配结果
        List<JobMatchResult> matchResults = new ArrayList<>();
        for (var point : results) {
            String jobId = point.getPayloadMap().get("jobId").getStringValue();
            JobData job = jobs.stream()
                    .filter(j -> j.getJobId().equals(jobId))
                    .findFirst()
                    .orElse(null);

            if (job != null) {
                matchResults.add(JobMatchResult.builder()
                        .jobId(job.getJobId())
                        .jobTitle(job.getTitle())
                        .company(job.getCompany())
                        .salary(job.getSalary())
                        .skills(job.getSkills())
                        .score((double) point.getScore())
                        .build());
            }
        }

        return matchResults;
    }

    /**
     * 匹配结果
     */
    @lombok.Data
    @lombok.Builder
    public static class JobMatchResult {
        private String jobId;
        private String jobTitle;
        private String company;
        private String salary;
        private List<String> skills;
        private Double score;
    }
}
