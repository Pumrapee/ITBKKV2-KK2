package sit.int221.kanbanapi.controllers;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sit.int221.kanbanapi.dtos.TaskDTO;
import sit.int221.kanbanapi.dtos.TaskListDTO;
import sit.int221.kanbanapi.entities.Task;
import sit.int221.kanbanapi.services.TaskService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/tasks")
@CrossOrigin(origins = "http://localhost:5173/")
public class TaskController {
    @Autowired
    private TaskService service;
    @Autowired
    private ModelMapper modelMapper;

    @GetMapping("")
    public ResponseEntity<Object> getAllTask() {
        List<Task> tasks = service.getTask();
        return ResponseEntity.ok(tasks.stream().map(task -> modelMapper.map(task, TaskListDTO.class)).collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getTaskById(@PathVariable Integer id) {
        Task task = service.getTaskById(id);
        TaskDTO taskDTO = modelMapper.map(task, TaskDTO.class);
        return ResponseEntity.ok(taskDTO);
    }
}
