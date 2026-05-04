package com.ruoyi.project.integration;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * 番组计划 (bgm.tv) 开放 API 客户端集成组件
 */
@Slf4j
@Component
public class BangumiApiClient {

    @Value("${bangumi.api.base-url}")
    private String baseUrl;

    @Value("${bangumi.api.user-agent}")
    private String userAgent;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 根据 bgm_id 获取番剧详情
     * @param subjectId 条目 ID
     * @return JSONObject 包含条目的详细信息
     */
    public JSONObject getSubjectDetails(Integer subjectId) {
        String url = baseUrl + "/v0/subjects/" + subjectId;
        
        HttpHeaders headers = new HttpHeaders();
        // 官方要求必须带上标识应用身份的 User-Agent
        headers.set(HttpHeaders.USER_AGENT, userAgent);
        
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            log.info("Requesting Bangumi API: {}", url);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return JSON.parseObject(response.getBody());
            }
        } catch (Exception e) {
            log.error("Failed to fetch data from Bangumi API for subject {}: {}", subjectId, e.getMessage());
        }
        return null;
    }

    /**
     * 获取每日放送（新番时间表）
     * @return JSONArray 包含每日放送数据的数组
     */
    public com.alibaba.fastjson2.JSONArray getCalendar() {
        String url = baseUrl + "/calendar";
        
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.USER_AGENT, userAgent);
        
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            log.info("Requesting Bangumi API: {}", url);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return JSON.parseArray(response.getBody());
            }
        } catch (Exception e) {
            log.error("Failed to fetch calendar from Bangumi API: {}", e.getMessage());
        }
        return new com.alibaba.fastjson2.JSONArray();
    }

    /**
     * 根据关键词搜索番剧
     * @param keywords 关键词
     * @return JSONObject 包含搜索结果
     */
    public JSONObject searchSubject(String keywords) {
        // 使用 legacy API 进行搜索，因为 v0 搜索还在 beta 且 legacy 支持 type 过滤
        String url = baseUrl + "/search/subject/" + keywords + "?type=2&responseGroup=large";
        
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.USER_AGENT, userAgent);
        
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            log.info("Requesting Bangumi Search API: {}", url);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return JSON.parseObject(response.getBody());
            }
        } catch (Exception e) {
            log.error("Failed to search from Bangumi API for keywords {}: {}", keywords, e.getMessage());
        }
        return null;
    }
}
