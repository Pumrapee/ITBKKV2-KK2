package sit.int221.kanbanapi.controllers;

import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sit.int221.kanbanapi.dtos.TaskCreateUpdateDTO;
import sit.int221.kanbanapi.dtos.SimpleTaskDTO;
import sit.int221.kanbanapi.dtos.TaskDTO;
import sit.int221.kanbanapi.databases.kanbandb.entities.Task;
import sit.int221.kanbanapi.services.StatusService;
import sit.int221.kanbanapi.services.TaskService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/tasks")
@CrossOrigin(origins = {"http://ip23kk2.sit.kmutt.ac.th","http://localhost:5173","http://intproj23.sit.kmutt.ac.th"})
public class TaskController {
    @Autowired
    private TaskService taskService;
    @Autowired
    private StatusService statusService;
    @Autowired
    private ModelMapper modelMapper;

    @GetMapping("")
    public ResponseEntity<List<SimpleTaskDTO>> getAllTaskFilteredSorted(@RequestParam(required = false) List<String> filterStatuses, @RequestParam(required = false, defaultValue = "createdOn") String sortBy) {
        List<Task> tasks = taskService.getAllTaskFilteredSorted(filterStatuses, sortBy);
        List<SimpleTaskDTO> taskDTOS = tasks.stream()
                .map(task -> {
                    SimpleTaskDTO taskDTO = modelMapper.map(task, SimpleTaskDTO.class);
                    return taskDTO;
                }).collect(Collectors.toList());
        return new ResponseEntity(taskDTOS, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskDTO> getTaskById(@PathVariable Integer id) {
        Task task = taskService.getTaskById(id);
        TaskDTO taskDTO = modelMapper.map(task, TaskDTO.class);
        return new ResponseEntity(taskDTO, HttpStatus.OK);
    }

    @PostMapping("")
    public ResponseEntity<TaskCreateUpdateDTO> addNewTask(@Valid @RequestBody TaskCreateUpdateDTO task) {
        Task newTask = modelMapper.map(task, Task.class);
        newTask.setTaskStatus(statusService.getStatusByName(task.getStatus()));
        Task createdTask = taskService.createTask(newTask);
        TaskCreateUpdateDTO taskDTO = modelMapper.map(createdTask, TaskCreateUpdateDTO.class);
        return new ResponseEntity(taskDTO, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskCreateUpdateDTO> updateTask(@Valid @RequestBody TaskCreateUpdateDTO task, @PathVariable Integer id) {
        Task newTask = modelMapper.map(task, Task.class);
        newTask.setTaskStatus(statusService.getStatusByName(task.getStatus()));
        Task updatedTask = taskService.updateTask(id, newTask);
        TaskCreateUpdateDTO taskDTO = modelMapper.map(updatedTask, TaskCreateUpdateDTO.class);
        return new ResponseEntity(taskDTO, HttpStatus.OK);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<SimpleTaskDTO> removeTask(@PathVariable Integer id) {
        Task deletedTask = taskService.removeTask(id);
        SimpleTaskDTO taskDTO = modelMapper.map(deletedTask, SimpleTaskDTO.class);
        return new ResponseEntity(taskDTO, HttpStatus.OK);
    }

    @GetMapping("/status/{id}")
    public ResponseEntity<Boolean> findTaskStatus(@PathVariable Integer id) {
        boolean findStatus = taskService.findTaskStatus(id);
        if (findStatus) {
            return new ResponseEntity(HttpStatus.OK);
        } else {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
    }
}
