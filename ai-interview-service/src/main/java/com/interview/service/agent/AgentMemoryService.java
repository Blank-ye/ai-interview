package com.interview.service.agent;

import lombok.extern.slf4j.Slf4j;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Agent记忆服务
 * 记住用户的历史面试表现，用于后续面试参考
 */
@Slf4j
@Service
public class AgentMemoryService {

    private final StringRedisTemplate redisTemplate;

    private static final String MEMORY_KEY_PREFIX = "agent:memory:";
    private static final int MAX_MEMORY_SIZE = 50;
    private static final int MEMORY_EXPIRE_DAYS = 30;

    public AgentMemoryService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 添加记忆
     */
    public void addMemory(Long userId, String memory) {
        String key = MEMORY_KEY_PREFIX + userId;
        List<String> memories = getMemories(userId);
        memories.add(memory);

        // 限制记忆数量
        if (memories.size() > MAX_MEMORY_SIZE) {
            memories = memories.subList(memories.size() - MAX_MEMORY_SIZE, memories.size());
        }

        // 保存到Redis
        redisTemplate.delete(key);
        redisTemplate.opsForList().rightPushAll(key, memories);
        redisTemplate.expire(key, MEMORY_EXPIRE_DAYS, TimeUnit.DAYS);

        log.debug("添加记忆：userId={}, memory={}", userId, memory);
    }

    /**
     * 获取记忆
     */
    public String getMemory(Long userId) {
        String key = MEMORY_KEY_PREFIX + userId;
        List<String> memories = redisTemplate.opsForList().range(key, 0, -1);

        if (memories == null || memories.isEmpty()) {
            return "";
        }

        // 返回最近的记忆
        StringBuilder sb = new StringBuilder();
        int start = Math.max(0, memories.size() - 10);
        for (int i = start; i < memories.size(); i++) {
            sb.append("- ").append(memories.get(i)).append("\n");
        }
        return sb.toString();
    }

    /**
     * 获取所有记忆
     */
    private List<String> getMemories(Long userId) {
        String key = MEMORY_KEY_PREFIX + userId;
        List<String> memories = redisTemplate.opsForList().range(key, 0, -1);
        return memories != null ? new ArrayList<>(memories) : new ArrayList<>();
    }

    /**
     * 清除记忆
     */
    public void clearMemory(Long userId) {
        String key = MEMORY_KEY_PREFIX + userId;
        redisTemplate.delete(key);
        log.info("清除记忆：userId={}", userId);
    }
}
