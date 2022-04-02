package com.example.controlalarm.websocket.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author linjiankai
 */
@Configuration
public class WebViewMvcConfig implements WebMvcConfigurer {

    /**
     * 为ws.html 提供便捷的路径映射。
     * @param registry
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/ws").setViewName("/ws");
    }

}
