package com.interview.service.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * AI大模型调用封装
 * 基于Spring AI Alibaba调用通义千问，提供单轮/多轮/流式对话能力
 */
@Slf4j
@Service
public class AiClientService {

    private final ChatClient chatClient;

    public AiClientService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    /**
     * 单轮对话
     */
    public String chat(String prompt) {
        log.info("调用AI，prompt长度：{}", prompt.length());
        String response = chatClient.prompt(prompt).call().content();
        log.info("AI响应长度：{}", response.length());
        return response;
    }

    /**
     * 多轮对话
     */
    public String chat(List<Message> messages) {
        log.info("调用AI，消息数量：{}", messages.size());
        String response = chatClient.prompt(new Prompt(messages)).call().content();
        log.info("AI响应长度：{}", response.length());
        return response;
    }

    /**
     * 流式对话
     */
    public Flux<String> chatStream(String prompt) {
        log.info("调用AI（流式），prompt长度：{}", prompt.length());
        return chatClient.prompt(prompt).stream().content();
    }
}
