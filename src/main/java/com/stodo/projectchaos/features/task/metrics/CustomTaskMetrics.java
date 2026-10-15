package com.stodo.projectchaos.features.task.metrics;

import com.stodo.projectchaos.features.task.TaskRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomTaskMetrics implements MeterBinder {
    private final TaskRepository taskRepository;

    @Override
    public void bindTo(MeterRegistry registry) {
        Gauge.builder(
                "projectchaos.tasks.count",
                taskRepository,
                repo -> repo.count()
        )
        .description("Total number of tasks")
        .register(registry);
    }
}
