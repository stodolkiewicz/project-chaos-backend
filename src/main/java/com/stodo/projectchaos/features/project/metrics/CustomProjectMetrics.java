package com.stodo.projectchaos.features.project.metrics;

import com.stodo.projectchaos.features.project.ProjectRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomProjectMetrics implements MeterBinder {
    private final ProjectRepository projectRepository;

    @Override
    public void bindTo(MeterRegistry registry) {
        Gauge.builder(
                "projectchaos.projects.count",
                projectRepository,
                repo -> repo.count()
        )
        .description("Total number of projects")
        .register(registry);
    }
}
