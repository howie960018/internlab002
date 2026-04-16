package com.ctbc.assignment2;

import com.ctbc.assignment2.security.JwtAuthenticationFilter;
import com.ctbc.assignment2.security.JwtService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestSecurityBeans {

    @Bean
    public JwtService jwtService() {
        // 測試用固定密鑰與過期時間，避免依賴外部設定
        return new JwtService("changeit-changeit-changeit-changeit", 1800000L);
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtService jwtService) {
        return new JwtAuthenticationFilter(jwtService);
    }
}
