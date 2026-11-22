package info.app.juleaften.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "email")
@Getter
public class EmailConfig {

    private final String from;

    public EmailConfig(String from) {
        this.from = from;
    }
}
