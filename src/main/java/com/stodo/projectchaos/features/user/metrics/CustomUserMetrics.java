package com.stodo.projectchaos.features.user.metrics;

import com.stodo.projectchaos.features.user.UserRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomUserMetrics implements MeterBinder {
    private final UserRepository userRepository;

    @Override
    public void bindTo(MeterRegistry registry) {
        Gauge.builder(
                "projectchaos.users.count",
                userRepository,
                UserRepository::count)
                .description("Total number of registered users")
                .register(registry);
    }
}
