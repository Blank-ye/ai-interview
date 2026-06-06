package com.interview.service.job;

import com.interview.dao.entity.Job;
import com.interview.dao.mapper.JobMapper;
import com.interview.dao.repository.VectorRepository;
import io.qdrant.client.grpc.JsonWithInt;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 职位服务
 * 提供职位CRUD和向量化功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JobService {

    private final JobMapper jobMapper;
    private final VectorRepository vectorRepository;
    private final EmbeddingModel embeddingModel;

    private static final String JOB_COLLECTION = "jobs";

    /**
     * 添加职位（自动向量化）
     */
    public Job createJob(Job job) {
        job.setStatus(1);
        job.setCreateTime(LocalDateTime.now());
        jobMapper.insert(job);

        // 向量化
        String vectorId = vectorizeJob(job);
        job.setVectorId(vectorId);
        jobMapper.updateById(job);

        log.info("职位创建成功，jobId: {}", job.getId());
        return job;
    }

    /**
     * 更新职位（重新向量化）
     */
    public Job updateJob(Job job) {
        job.setUpdateTime(LocalDateTime.now());
        jobMapper.updateById(job);

        // 重新向量化
        String vectorId = vectorizeJob(job);
        job.setVectorId(vectorId);
        jobMapper.updateById(job);

        log.info("职位更新成功，jobId: {}", job.getId());
        return job;
    }

    /**
     * 删除职位
     */
    public void deleteJob(Long jobId) {
        Job job = jobMapper.selectById(jobId);
        if (job != null && job.getVectorId() != null) {
            vectorRepository.deleteVector(JOB_COLLECTION, job.getVectorId());
        }
        jobMapper.deleteById(jobId);
        log.info("职位删除成功，jobId: {}", jobId);
    }

    /**
     * 向量化单个职位
     */
    public String vectorizeJob(Job job) {
        String text = buildJobText(job);
        float[] embedding = embeddingModel.embed(text);

        List<Float> vector = new ArrayList<>(embedding.length);
        for (float v : embedding) {
            vector.add(v);
        }

        Map<String, JsonWithInt.Value> payload = new HashMap<>();
        payload.put("jobId", JsonWithInt.Value.newBuilder()
                .setIntegerValue(job.getId())
                .build());

        // 如果已有向量，先删除
        if (job.getVectorId() != null) {
            try {
                vectorRepository.deleteVector(JOB_COLLECTION, job.getVectorId());
            } catch (Exception e) {
                log.warn("删除旧向量失败，jobId: {}", job.getId());
            }
        }

        return vectorRepository.storeVector(JOB_COLLECTION, vector, payload);
    }

    /**
     * 批量向量化所有职位（启动时调用）
     */
    public void vectorizeAllJobs() {
        List<Job> jobs = jobMapper.selectList(null);
        log.info("开始向量化{}个职位", jobs.size());

        for (Job job : jobs) {
            try {
                String vectorId = vectorizeJob(job);
                job.setVectorId(vectorId);
                jobMapper.updateById(job);
                log.debug("职位向量化成功，jobId: {}", job.getId());
            } catch (Exception e) {
                log.error("职位向量化失败，jobId: {}", job.getId(), e);
            }
        }

        log.info("职位向量化完成");
    }

    /**
     * 构建职位文本（用于向量化）
     */
    private String buildJobText(Job job) {
        StringBuilder sb = new StringBuilder();
        sb.append(job.getTitle()).append(" ");
        if (job.getCompany() != null) {
            sb.append(job.getCompany()).append(" ");
        }
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
}
