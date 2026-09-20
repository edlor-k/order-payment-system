package dev.sorokin.application.payment;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@ConfigurationProperties(prefix = "task-execution.dispatcher")
@Getter
@Setter
public class TaskDispatcherProperties {
    private Duration retryDelay;
    private int maxAttempts;
}
