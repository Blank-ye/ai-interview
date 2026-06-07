package com.interview.api.config;

import com.interview.service.crawler.JobCrawlerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * 爬虫定时任务配置
 * 每周凌晨3点执行全量更新
 */
@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class CrawlerScheduleConfig {

    private final JobCrawlerService jobCrawlerService;

    /**
     * 每周日凌晨3点执行
     */
    @Scheduled(cron = "0 0 3 ? * SUN")
    public void weeklyCrawl() {
        log.info("开始执行每周职位爬取任务...");
        try {
            jobCrawlerService.crawlAndUpdate();
            log.info("每周职位爬取任务完成");
        } catch (Exception e) {
            log.error("每周职位爬取任务失败", e);
        }
    }
}
