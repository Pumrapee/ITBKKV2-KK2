package sit.int221.kanbanapi.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sit.int221.kanbanapi.configs.StatusConfiguration;
import sit.int221.kanbanapi.exceptions.BadRequestException;
import sit.int221.kanbanapi.exceptions.TaskLimitExceededException;
import sit.int221.kanbanapi.services.ConfigService;
import sit.int221.kanbanapi.services.TaskService;

import java.io.IOException;

@RestController
@RequestMapping("")
@CrossOrigin(origins = {"http://ip23kk2.sit.kmutt.ac.th","http://localhost:5173","http://intproj23.sit.kmutt.ac.th"})
public class ConfigurationController {
    @Autowired
    private ConfigService configService;

    @Autowired
    private TaskService taskService;

    @GetMapping("/statuses/limitTask")
    public ResponseEntity<Object> getConfig() {
        StatusConfiguration config = configService.getConfiguration();
        return new ResponseEntity(config, HttpStatus.OK);
    }

    @PatchMapping("/statuses/limitTask")
    public ResponseEntity<Object> updateConfig( @RequestParam boolean taskLimitEnabled, @RequestParam Integer maxTasksPerStatus) {
        try {
            configService.updateTaskConfiguration(taskLimitEnabled, maxTasksPerStatus);
            return new ResponseEntity(HttpStatus.OK);
        } catch (IOException ex) {
            throw new BadRequestException("config wrong");
        }
    }
}
