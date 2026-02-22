package com.stodo.projectchaos.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.web.filter.ForwardedHeaderFilter;

@Configuration
public class WebConfig {

    /**
     * ForwardedHeaderFilter for cloud deployment behind reverse proxy/load balancer.
     * 
     * Cloud Run flow: Client (HTTPS) → Google Load Balancer → App Container (HTTP)
     * Load balancer adds X-Forwarded-* headers to preserve original request info.
     * 
     * Without this filter:
     * - request.getScheme() returns "http" instead of "https"
     * - OAuth2 callbacks fail (Google requires HTTPS URLs)
     * - Spring Security CSRF/cookie issues
     * 
     * With filter: App correctly sees original HTTPS request details.
     */
    @Profile("prod")
    @Bean
    public FilterRegistrationBean<ForwardedHeaderFilter> forwardedHeaderFilter() {
        FilterRegistrationBean<ForwardedHeaderFilter> bean = new FilterRegistrationBean<>(new ForwardedHeaderFilter());
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return bean;
    }
}
