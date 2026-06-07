package com.interview.api.config;

import com.interview.dao.repository.VectorRepository;
import com.interview.service.crawler.JobCrawlerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Qdrant初始化配置
 * 应用启动时：
 * 1. 创建所需的集合
 * 2. 从RAG文档加载职位数据到Qdrant
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QdrantInitConfig implements CommandLineRunner {

    private final VectorRepository vectorRepository;
    private final JobCrawlerService jobCrawlerService;

    // 向量维度，根据使用的Embedding模型调整
    private static final int VECTOR_SIZE = 1536;

    @Override
    public void run(String... args) {
        log.info("初始化Qdrant集合...");
        vectorRepository.createCollectionIfNotExists("resumes", VECTOR_SIZE);
        vectorRepository.createCollectionIfNotExists("jobs", VECTOR_SIZE);
        log.info("Qdrant集合初始化完成");

        // 从RAG加载职位数据到Qdrant
        log.info("从RAG加载职位数据...");
        jobCrawlerService.loadRagToQdrant();
        log.info("职位数据加载完成");
    }
}
