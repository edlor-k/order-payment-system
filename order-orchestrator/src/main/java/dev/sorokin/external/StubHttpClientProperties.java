package dev.sorokin.external;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "clients.stub")
public class StubHttpClientProperties {
    private Duration  connectTimeout;
    private Duration readTimeout;
    private String baseUrl;
}
