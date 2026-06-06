package com.interview.service.resume;

import com.interview.dao.repository.VectorRepository;
import io.qdrant.client.grpc.JsonWithInt;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 简历向量化服务
 * 使用Spring AI的EmbeddingModel将简历文本转为向量，存储到Qdrant
 * 用于后续的语义匹配
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeVectorService {

    private final VectorRepository vectorRepository;
    private final EmbeddingModel embeddingModel;

    private static final String COLLECTION_NAME = "resumes";

    /**
     * 向量化并存储简历
     */
    public String vectorizeAndStore(Long resumeId, String content) {
        // 1. 文本向量化
        float[] embedding = embeddingModel.embed(content);

        List<Float> vector= new ArrayList<>(embedding.length);
        for(float v:embedding){
            vector.add(v);
        }
        // 2. 构建payload
        Map<String, JsonWithInt.Value> payload = new HashMap<>();
        payload.put("resumeId", JsonWithInt.Value.newBuilder()
                .setIntegerValue(resumeId)
                .build());

        // 3. 存储到Qdrant
        return vectorRepository.storeVector(COLLECTION_NAME, vector, payload);
    }

    /**
     * 搜索相似简历
     */
    public List<Long> searchSimilarResumes(String content, int limit) {
        // 1. 文本向量化
        float[] embedding = embeddingModel.embed(content);

        List<Float> vector= new ArrayList<>(embedding.length);
        for(float v:embedding){
            vector.add(v);
        }
        // 2. 搜索相似向量
        var results = vectorRepository.searchSimilar(COLLECTION_NAME, vector, limit);

        // 3. 提取简历ID
        return results.stream()
                .map(point -> point.getPayloadMap().get("resumeId").getIntegerValue())
                .toList();
    }
}
