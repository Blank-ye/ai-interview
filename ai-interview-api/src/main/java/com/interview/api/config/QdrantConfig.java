package com.interview.api.config;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Qdrant向量数据库配置
 * 创建QdrantClient Bean，连接gRPC端口（默认6334）
 */
@Configuration
public class QdrantConfig {

    @Value("${interview.qdrant.host}")
    private String host;

    @Value("${interview.qdrant.port}")
    private int port;

    @Bean
    public QdrantClient qdrantClient() {
        return new QdrantClient(
                QdrantGrpcClient.newBuilder(host, port, false)
                        .build()
        );
    }
}
