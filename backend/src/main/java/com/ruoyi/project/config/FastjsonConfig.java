package com.ruoyi.project.config;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.support.config.FastJsonConfig;
import com.alibaba.fastjson2.support.spring6.http.converter.FastJsonHttpMessageConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

/**
 * Fastjson2 全局配置
 * 遵循用户准则：JSON 必须使用 Fastjson2
 */
@Configuration
public class FastjsonConfig implements WebMvcConfigurer {

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        FastJsonHttpMessageConverter converter = new FastJsonHttpMessageConverter();
        
        FastJsonConfig config = new FastJsonConfig();
        config.setDateFormat("yyyy-MM-dd HH:mm:ss");
        config.setCharset(StandardCharsets.UTF_8);
        
        // 关键配置：
        // 1. WriteLongAsString: 解决雪花 ID (Long) 在前端 JS 精度丢失问题
        // 2. WriteEnumsUsingName: 枚举序列化为名称
        config.setWriterFeatures(
                JSONWriter.Feature.WriteLongAsString,
                JSONWriter.Feature.WriteEnumsUsingName,
                JSONWriter.Feature.BrowserCompatible
        );
        
        config.setReaderFeatures(
                JSONReader.Feature.SupportSmartMatch
        );

        converter.setFastJsonConfig(config);
        converter.setDefaultCharset(StandardCharsets.UTF_8);
        converter.setSupportedMediaTypes(Collections.singletonList(MediaType.APPLICATION_JSON));
        
        // 将 Fastjson2 转换器添加到最前面，使其优先级高于 Jackson
        converters.add(0, converter);
    }
}
