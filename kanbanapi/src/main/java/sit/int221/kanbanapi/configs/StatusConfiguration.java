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
    @NotNull(message = "taskLimitEnabled cannot be null")
    private Boolean taskLimitEnabled;
    @NotNull(message = "maxTasksPerStatus cannot be null")
    @Max(value = 30, message = "maxTasksPerStatus must be less than or equal to 30")
    private Integer maxTasksPerStatus;
}