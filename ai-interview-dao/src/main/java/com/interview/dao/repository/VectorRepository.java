package com.interview.dao.repository;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.VectorFactory;
import io.qdrant.client.VectorsFactory;
import io.qdrant.client.grpc.Collections;
import io.qdrant.client.grpc.JsonWithInt;
import io.qdrant.client.grpc.Points;
import io.qdrant.client.grpc.Points.Filter;
import io.qdrant.client.grpc.Points.PointStruct;
import io.qdrant.client.grpc.Points.ScoredPoint;
import io.qdrant.client.grpc.Points.SearchPoints;
import io.qdrant.client.grpc.Points.Vectors;
import io.qdrant.client.grpc.Points.Vector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Qdrant向量数据库操作封装
 * 用于存储和检索简历、职位的向量表示，实现语义匹配
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class VectorRepository {

    private final QdrantClient qdrantClient;

    /**
     * 创建集合（如果不存在）
     * @param collectionName 集合名称
     * @param vectorSize 向量维度
     */
    public void createCollectionIfNotExists(String collectionName, int vectorSize) {
        try {
            // 检查集合是否存在
            List<String> collections = qdrantClient.listCollectionsAsync().get();
            boolean exists = collections.contains(collectionName);

            if (!exists) {
                // 创建集合
                var params = Collections.VectorParams.newBuilder()
                        .setDistance(Collections.Distance.Cosine)
                        .setSize(vectorSize)
                        .build();

                qdrantClient.createCollectionAsync(collectionName, params).get();
                log.info("创建Qdrant集合：{}", collectionName);
            } else {
                log.info("Qdrant集合已存在：{}", collectionName);
            }
        } catch (Exception e) {
            log.error("创建集合失败：{}", collectionName, e);
            throw new RuntimeException("创建集合失败", e);
        }
    }

    /**
     * 存储向量到Qdrant
     * @param collectionName 集合名称（resumes/jobs）
     * @param vector 向量数据
     * @param payload 附加元数据（如resumeId、jobId）
     * @return 向量ID，用于后续更新或删除
     */
    public String storeVector(String collectionName, List<Float> vector, Map<String, JsonWithInt.Value> payload) {
        String pointId = UUID.randomUUID().toString();

        Vectors vectors = VectorsFactory.vectors(vector);

        PointStruct point = PointStruct.newBuilder()
                .setId(Points.PointId.newBuilder().setUuid(pointId).build())
                .setVectors(vectors)
                .putAllPayload(payload)
                .build();

        try {
            qdrantClient.upsertAsync(collectionName, List.of(point)).get();
        } catch (Exception e) {
            log.error("存储向量失败", e);
            throw new RuntimeException("存储向量失败", e);
        }

        return pointId;
    }

    /**
     * 搜索相似向量（Top N）
     */
    public List<ScoredPoint> searchSimilar(String collectionName, List<Float> vector, int limit) {
        return searchSimilar(collectionName, vector, limit, null);
    }

    /**
     * 搜索相似向量（带过滤条件）
     * 使用余弦相似度计算，返回最相似的N个结果
     */
    public List<ScoredPoint> searchSimilar(String collectionName, List<Float> vector, int limit, Filter filter) {
        // 转换向量格式


        SearchPoints.Builder searchBuilder = SearchPoints.newBuilder()
                .setCollectionName(collectionName)
                .addAllVector(vector)
                .setLimit(limit)
                .setWithPayload(Points.WithPayloadSelector.newBuilder().setEnable(true).build());

        if (filter != null) {
            searchBuilder.setFilter(filter);
        }

        try {
            return qdrantClient.searchAsync(searchBuilder.build()).get();
        } catch (Exception e) {
            log.error("搜索向量失败", e);
            throw new RuntimeException("搜索向量失败", e);
        }
    }

    /**
     * 删除向量
     */
    public void deleteVector(String collectionName, String pointId) {
        try {
            qdrantClient.deleteAsync(collectionName,
                    List.of(Points.PointId.newBuilder().setUuid(pointId).build())
            ).get();
        } catch (Exception e) {
            log.error("删除向量失败", e);
            throw new RuntimeException("删除向量失败", e);
        }
    }

    /**
     * 删除集合
     */
    public void deleteCollection(String collectionName) {
        try {
            qdrantClient.deleteCollectionAsync(collectionName).get();
            log.info("删除Qdrant集合：{}", collectionName);
        } catch (Exception e) {
            log.error("删除集合失败：{}", collectionName, e);
            throw new RuntimeException("删除集合失败", e);
        }
    }
}
