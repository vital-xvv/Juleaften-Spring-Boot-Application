package info.app.juleaften.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "http")
@Getter
public class HttpConfig {
    private final boolean secure;
    private final String sameSite;
    private final String clientBaseUrl;

    public HttpConfig(boolean secure, String sameSite, String clientBaseUrl) {
        this.secure = secure;
        this.sameSite = sameSite;
        this.clientBaseUrl = clientBaseUrl;
    }
}
