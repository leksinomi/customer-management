package com.example.customermanagement.config;

import com.example.customermanagement.filter.RequestBodyCachingFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<RequestBodyCachingFilter> requestBodyCachingFilter() {
        FilterRegistrationBean<RequestBodyCachingFilter> registrationBean = new FilterRegistrationBean<>();

        registrationBean.setFilter(new RequestBodyCachingFilter());
        registrationBean.setOrder(1);
        registrationBean.addUrlPatterns("/api/customers/*");

        return registrationBean;
    }
}
