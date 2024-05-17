package sit.int221.kanbanapi.configs;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "status")
public class StatusConfiguration {
    private Boolean taskLimitEnabled;
    private Integer maxTasksPerStatus;
}