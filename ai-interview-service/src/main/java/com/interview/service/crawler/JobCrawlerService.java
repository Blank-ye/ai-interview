package com.interview.service.crawler;

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
 * 职位爬虫服务
 * 负责爬取职位数据，存储到RAG文档，向量化存入Qdrant
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JobCrawlerService {

    private final RagStorage ragStorage;
    private final VectorRepository vectorRepository;
    private final EmbeddingModel embeddingModel;

    private static final String JOB_COLLECTION = "jobs";

    /**
     * 执行爬取任务（全量更新）
     */
    public void crawlAndUpdate() {
        log.info("开始爬取职位数据...");

        // 1. 爬取职位数据
        List<JobData> jobs = crawlFromBoss();

        if (jobs.isEmpty()) {
            log.warn("爬取到的职位数据为空，跳过更新");
            return;
        }

        // 2. 清空旧数据
        ragStorage.clear();
        vectorRepository.deleteCollection(JOB_COLLECTION);
        vectorRepository.createCollectionIfNotExists(JOB_COLLECTION, 1536);

        // 3. 保存到RAG文档
        ragStorage.save(jobs);

        // 4. 向量化存入Qdrant
        vectorizeJobs(jobs);

        log.info("职位数据更新完成，数量：{}", jobs.size());
    }

    /**
     * 从BOSS直聘爬取数据（预留接口）
     */
    private List<JobData> crawlFromBoss() {
        // TODO: 实现BOSS爬虫
        // 1. 发送HTTP请求
        // 2. 解析HTML/JSON
        // 3. 提取职位信息
        // 4. 封装为JobData

        log.info("爬取BOSS直聘数据（待实现）");
        return new ArrayList<>();
    }

    /**
     * 向量化职位数据
     */
    private void vectorizeJobs(List<JobData> jobs) {
        log.info("开始向量化{}个职位", jobs.size());

        for (JobData job : jobs) {
            try {
                // 构建文本
                String text = buildJobText(job);

                // 向量化
                float[] embedding = embeddingModel.embed(text);
                List<Float> vector = new ArrayList<>(embedding.length);
                for (float v : embedding) {
                    vector.add(v);
                }

                // 存入Qdrant
                Map<String, JsonWithInt.Value> payload = new HashMap<>();
                payload.put("jobId", JsonWithInt.Value.newBuilder()
                        .setStringValue(job.getJobId())
                        .build());

                vectorRepository.storeVector(JOB_COLLECTION, vector, payload);

            } catch (Exception e) {
                log.error("向量化职位失败：{}", job.getJobId(), e);
            }
        }

        log.info("职位向量化完成");
    }

    /**
     * 构建职位文本（用于向量化）
     */
    private String buildJobText(JobData job) {
        StringBuilder sb = new StringBuilder();
        sb.append(job.getTitle()).append(" ");
        sb.append(job.getCompany()).append(" ");
        sb.append(job.getDescription()).append(" ");
        sb.append(job.getRequirements()).append(" ");
        if (job.getSkills() != null) {
            sb.append(String.join(" ", job.getSkills()));
        }
        return sb.toString();
    }

    /**
     * 加载RAG数据到Qdrant（启动时调用）
     */
    public void loadRagToQdrant() {
        List<JobData> jobs = ragStorage.loadAll();
        if (jobs.isEmpty()) {
            log.info("RAG文档为空，跳过加载");
            return;
        }

        log.info("从RAG加载{}个职位到Qdrant", jobs.size());
        vectorRepository.createCollectionIfNotExists(JOB_COLLECTION, 1536);
        vectorizeJobs(jobs);
    }
}
