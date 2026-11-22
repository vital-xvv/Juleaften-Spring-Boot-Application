package info.app.juleaften.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "http")
@Getter
public class HttpConfig {
    private final boolean secure;
    private final String clientBaseUrl;

    public HttpConfig(boolean secure, String clientBaseUrl) {
        this.secure = secure;
        this.clientBaseUrl = clientBaseUrl;
    }
}
