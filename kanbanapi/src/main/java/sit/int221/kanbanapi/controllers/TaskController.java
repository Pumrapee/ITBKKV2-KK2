package sit.int221.kanbanapi.controllers;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import sit.int221.kanbanapi.dtos.TaskDTO;
import sit.int221.kanbanapi.dtos.TaskListDTO;
import sit.int221.kanbanapi.entities.Task;
import sit.int221.kanbanapi.services.TaskService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/tasks")
@CrossOrigin(origins = {"http://localhost:5173/","http://ip23kk2.sit.kmutt.ac.th:80/"})
public class TaskController {
    @Autowired
    private TaskService service;
    @Autowired
    private ModelMapper modelMapper;

    @GetMapping("")
    public ResponseEntity<Object> getAllTask() {
        List<Task> tasks = service.getTask();
        return new ResponseEntity<>(tasks.stream().map(task -> modelMapper.map(task, TaskListDTO.class)).collect(Collectors.toList()), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getTaskById(@PathVariable Integer id) {
        Task task = service.getTaskById(id);
        TaskDTO taskDTO = modelMapper.map(task, TaskDTO.class);
        return new ResponseEntity<>(taskDTO, HttpStatus.OK);
    }

    @PostMapping("")
    public ResponseEntity<Object> addNewTask(@RequestBody Task task) {
        Task newTask = service.createTask(task);
        TaskListDTO taskDTO = modelMapper.map(newTask, TaskListDTO.class);
        return new ResponseEntity<>(taskDTO, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateTask(@RequestBody Task task, @PathVariable Integer id) {
        Task updatedTask = service.updateTask(id, task);
        TaskListDTO taskDTO = modelMapper.map(updatedTask, TaskListDTO.class);
        return new ResponseEntity<>(taskDTO, HttpStatus.OK);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> removeTask(@PathVariable Integer id) {
        Task deletedTask = service.removeTask(id);
        TaskListDTO taskDTO = modelMapper.map(deletedTask, TaskListDTO.class);
        return new ResponseEntity<>(taskDTO, HttpStatus.OK);
    }

}
