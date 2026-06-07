package com.interview.service.crawler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RAG文档存储服务
 * 职位数据以JSON格式存储在本地文件系统
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RagStorage {

    private final ObjectMapper objectMapper;

    @Value("${crawler.storage-path:data/jobs}")
    private String storagePath;

    private static final String JOBS_FILE = "jobs.json";

    /**
     * 保存职位数据（全量覆盖）
     */
    public void save(List<JobData> jobs) {
        try {
            Path dir = Paths.get(storagePath);
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }

            // 转换为Map结构，jobId作为key
            Map<String, JobData> jobMap = new HashMap<>();
            for (JobData job : jobs) {
                jobMap.put(job.getJobId(), job);
            }

            Path filePath = dir.resolve(JOBS_FILE);
            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(filePath.toFile(), jobMap);

            log.info("保存职位数据成功，数量：{}，路径：{}", jobs.size(), filePath);
        } catch (IOException e) {
            log.error("保存职位数据失败", e);
            throw new RuntimeException("保存职位数据失败", e);
        }
    }

    /**
     * 加载所有职位数据
     */
    public List<JobData> loadAll() {
        Path filePath = Paths.get(storagePath, JOBS_FILE);
        if (!Files.exists(filePath)) {
            log.warn("职位数据文件不存在：{}", filePath);
            return new ArrayList<>();
        }

        try {
            Map<String, JobData> jobMap = objectMapper.readValue(
                    filePath.toFile(),
                    new TypeReference<Map<String, JobData>>() {}
            );
            return new ArrayList<>(jobMap.values());
        } catch (IOException e) {
            log.error("加载职位数据失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 根据jobId列表获取职位数据
     */
    public List<JobData> getByIds(List<String> jobIds) {
        Path filePath = Paths.get(storagePath, JOBS_FILE);
        if (!Files.exists(filePath)) {
            return new ArrayList<>();
        }

        try {
            Map<String, JobData> jobMap = objectMapper.readValue(
                    filePath.toFile(),
                    new TypeReference<Map<String, JobData>>() {}
            );

            List<JobData> result = new ArrayList<>();
            for (String jobId : jobIds) {
                JobData job = jobMap.get(jobId);
                if (job != null) {
                    result.add(job);
                }
            }
            return result;
        } catch (IOException e) {
            log.error("获取职位数据失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 清空职位数据
     */
    public void clear() {
        try {
            Path filePath = Paths.get(storagePath, JOBS_FILE);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("清空职位数据成功");
            }
        } catch (IOException e) {
            log.error("清空职位数据失败", e);
        }
    }

    /**
     * 获取职位数量
     */
    public int count() {
        return loadAll().size();
    }
}
