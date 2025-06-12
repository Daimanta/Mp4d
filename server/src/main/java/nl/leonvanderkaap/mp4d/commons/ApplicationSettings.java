package nl.leonvanderkaap.mp4d.commons;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@ConfigurationProperties(prefix = "application")
@EnableScheduling
@Getter
@Setter
public class ApplicationSettings {
    private String basepath;
}
