package com.stodo.projectchaos.config;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

@Component
class InstallOpenTelemetryAppender implements InitializingBean {

    private final OpenTelemetry openTelemetry;

    InstallOpenTelemetryAppender(OpenTelemetry openTelemetry) {
        this.openTelemetry = openTelemetry;
    }

    @Override
    public void afterPropertiesSet() {
        // at startup, Spring created OpenTelemetryAppender from logback-spring.xml.
        // It does not yet, know where to send logs.

        // but, after this line, it does know
        OpenTelemetryAppender.install(this.openTelemetry);
    }

}