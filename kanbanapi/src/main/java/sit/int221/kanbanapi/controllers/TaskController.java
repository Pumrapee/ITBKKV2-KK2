package sit.int221.kanbanapi.controllers;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sit.int221.kanbanapi.dtos.TaskCreateUpdateDTO;
import sit.int221.kanbanapi.dtos.SimpleTaskDTO;
import sit.int221.kanbanapi.dtos.TaskDTO;
import sit.int221.kanbanapi.entities.Task;
import sit.int221.kanbanapi.services.StatusService;
import sit.int221.kanbanapi.services.TaskService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/tasks")
@CrossOrigin(origins = "http://ip23kk2.sit.kmutt.ac.th")
public class TaskController {
    @Autowired
    private TaskService taskService;
    @Autowired
    private StatusService statusService;
    @Autowired
    private ModelMapper modelMapper;

    @GetMapping("")
    public ResponseEntity<Object> getAllTask() {
        List<Task> tasks = taskService.getTask();
        List<SimpleTaskDTO> taskDTOS = tasks.stream()
                .map(task -> {
                    SimpleTaskDTO taskDTO = modelMapper.map(task, SimpleTaskDTO.class);
                    taskDTO.setStatus(task.getStatus().getName());
                    return taskDTO;
                }).collect(Collectors.toList());
        return new ResponseEntity<>(taskDTOS, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getTaskById(@PathVariable Integer id) {
        Task task = taskService.getTaskById(id);
        TaskDTO taskDTO = modelMapper.map(task, TaskDTO.class);
        taskDTO.setStatus(task.getStatus().getName());
        return new ResponseEntity<>(taskDTO, HttpStatus.OK);
    }

    @PostMapping("")
    public ResponseEntity<Object> addNewTask(@RequestBody TaskCreateUpdateDTO task) {
        Task newTask = modelMapper.map(task, Task.class);
        newTask.setStatus(statusService.getStatusByName(task.getStatus()));
        Task createdTask = taskService.createTask(newTask);
        TaskCreateUpdateDTO taskDTO = modelMapper.map(createdTask, TaskCreateUpdateDTO.class);
        taskDTO.setStatus(createdTask.getStatus().getName());
        return new ResponseEntity<>(taskDTO, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateTask(@RequestBody TaskCreateUpdateDTO task, @PathVariable Integer id) {
        Task newTask = modelMapper.map(task, Task.class);
        newTask.setStatus(statusService.getStatusByName(task.getStatus()));
        Task updatedTask = taskService.updateTask(id, newTask);
        TaskCreateUpdateDTO taskDTO = modelMapper.map(updatedTask, TaskCreateUpdateDTO.class);
        taskDTO.setStatus(updatedTask.getStatus().getName());
        return new ResponseEntity<>(taskDTO, HttpStatus.OK);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> removeTask(@PathVariable Integer id) {
        Task deletedTask = taskService.removeTask(id);
        SimpleTaskDTO taskDTO = modelMapper.map(deletedTask, SimpleTaskDTO.class);
        return new ResponseEntity<>(taskDTO, HttpStatus.OK);
    }
}
