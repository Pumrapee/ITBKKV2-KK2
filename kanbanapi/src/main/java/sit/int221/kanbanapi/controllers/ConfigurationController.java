package sit.int221.kanbanapi.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import sit.int221.kanbanapi.configs.StatusConfiguration;
import sit.int221.kanbanapi.exceptions.BadRequestException;
import sit.int221.kanbanapi.exceptions.TaskLimitExceededException;
import sit.int221.kanbanapi.services.ConfigService;
import sit.int221.kanbanapi.services.TaskService;

import java.io.IOException;

@RestController
@RequestMapping("/statuses")
@CrossOrigin(origins = {"http://ip23kk2.sit.kmutt.ac.th","http://localhost:5173","http://intproj23.sit.kmutt.ac.th"})
public class ConfigurationController {
    @Autowired
    private ConfigService configService;

    @GetMapping("/maximum-task")
    public ResponseEntity<StatusConfiguration> getConfig() {
        StatusConfiguration config = configService.getConfiguration();
        return new ResponseEntity(config, HttpStatus.OK);
    }

    @PatchMapping("/maximum-task")
    public ResponseEntity<StatusConfiguration> updateConfig(@RequestParam @NotNull Boolean taskLimitEnabled, @RequestParam @NotNull @Min(0) @Max(30) Integer maxTasksPerStatus) {
        try {
            configService.updateTaskConfiguration(taskLimitEnabled, maxTasksPerStatus);
            return new ResponseEntity(configService.getConfiguration(), HttpStatus.OK);
        } catch (IOException ex) {
            if (configService.getConfiguration().getTaskLimitEnabled() == taskLimitEnabled && configService.getConfiguration().getMaxTasksPerStatus() == maxTasksPerStatus) {
                return new ResponseEntity(configService.getConfiguration(), HttpStatus.OK);
            } else {
                throw new BadRequestException("config wrong");
            }
        }
    }
}
