package com.interview.api.config;

import com.interview.dao.repository.VectorRepository;
import com.interview.service.job.JobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Qdrant初始化配置
 * 应用启动时：
 * 1. 创建所需的集合
 * 2. 将已有职位数据向量化
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QdrantInitConfig implements CommandLineRunner {

    private final VectorRepository vectorRepository;
    private final JobService jobService;

    // 向量维度，根据使用的Embedding模型调整
    private static final int VECTOR_SIZE = 1536;

    @Override
    public void run(String... args) {
        log.info("初始化Qdrant集合...");
        vectorRepository.createCollectionIfNotExists("resumes", VECTOR_SIZE);
        vectorRepository.createCollectionIfNotExists("jobs", VECTOR_SIZE);
        log.info("Qdrant集合初始化完成");

        // 向量化已有职位数据
        log.info("开始向量化已有职位数据...");
        jobService.vectorizeAllJobs();
        log.info("职位数据向量化完成");
    }
}
