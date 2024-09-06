package sit.int221.kanbanapi.controllers;

import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import sit.int221.kanbanapi.dtos.TaskCreateUpdateDTO;
import sit.int221.kanbanapi.dtos.SimpleTaskDTO;
import sit.int221.kanbanapi.dtos.TaskDTO;
import sit.int221.kanbanapi.databases.kanbandb.entities.Task;
import sit.int221.kanbanapi.services.BoardService;
import sit.int221.kanbanapi.services.StatusService;
import sit.int221.kanbanapi.services.TaskService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/boards/{boardId}/tasks")
@CrossOrigin(origins = {"http://ip23kk2.sit.kmutt.ac.th","http://localhost:5173","http://intproj23.sit.kmutt.ac.th"})
public class TaskController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private StatusService statusService;

    @Autowired
    private BoardService boardService;

    @Autowired
    private ModelMapper modelMapper;

    @GetMapping("")
    public ResponseEntity<List<SimpleTaskDTO>> getAllTaskFilteredSorted(
            @RequestParam(required = false) List<String> filterStatuses,
            @RequestParam(required = false, defaultValue = "createdOn") String sortBy,
            @PathVariable String boardId,
            @AuthenticationPrincipal UserDetails user) {
        List<Task> tasks = taskService.getAllTaskFilteredSorted(filterStatuses, sortBy, boardId, user.getUsername());
        List<SimpleTaskDTO> taskDTOS = tasks.stream()
                .map(task -> modelMapper.map(task, SimpleTaskDTO.class))
                .collect(Collectors.toList());
        return new ResponseEntity<>(taskDTOS, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskDTO> getTaskById(@PathVariable Integer id, @PathVariable String boardId, @AuthenticationPrincipal UserDetails user) {
        Task task = taskService.getTaskById(id, boardId, user.getUsername());
        TaskDTO taskDTO = modelMapper.map(task, TaskDTO.class);
        return new ResponseEntity<>(taskDTO, HttpStatus.OK);
    }

    @PostMapping("")
    public ResponseEntity<TaskCreateUpdateDTO> addNewTask(@Valid @RequestBody TaskCreateUpdateDTO task, @PathVariable String boardId, @AuthenticationPrincipal UserDetails user) {
        Task newTask = modelMapper.map(task, Task.class);
        newTask.setTaskStatus(statusService.getStatusByName(task.getStatus(), boardId, user.getUsername()));
        Task createdTask = taskService.createTask(newTask, boardId, user.getUsername());
        TaskCreateUpdateDTO taskDTO = modelMapper.map(createdTask, TaskCreateUpdateDTO.class);
        return new ResponseEntity<>(taskDTO, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskCreateUpdateDTO> updateTask(
            @Valid @RequestBody TaskCreateUpdateDTO task,
            @PathVariable Integer id,
            @PathVariable String boardId,
            @AuthenticationPrincipal UserDetails user) {
        Task newTask = modelMapper.map(task, Task.class);
        newTask.setTaskStatus(statusService.getStatusByName(task.getStatus(), boardId, user.getUsername()));
        Task updatedTask = taskService.updateTask(id, newTask, boardId, user.getUsername());
        TaskCreateUpdateDTO taskDTO = modelMapper.map(updatedTask, TaskCreateUpdateDTO.class);
        return new ResponseEntity<>(taskDTO, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<SimpleTaskDTO> removeTask(@PathVariable Integer id, @PathVariable String boardId, @AuthenticationPrincipal UserDetails user) {
        Task deletedTask = taskService.removeTask(id, boardId, user.getUsername());
        SimpleTaskDTO taskDTO = modelMapper.map(deletedTask, SimpleTaskDTO.class);
        return new ResponseEntity<>(taskDTO, HttpStatus.OK);
    }

    @GetMapping("/status/{id}")
    public ResponseEntity<Boolean> findTaskStatus(@PathVariable Integer id, @PathVariable String boardId) {
        boolean findStatus = taskService.findTaskStatus(id, boardId);
        return new ResponseEntity<>(findStatus ? HttpStatus.OK : HttpStatus.NOT_FOUND);
    }
}