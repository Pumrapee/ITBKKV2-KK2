package sit.int221.kanbanapi.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sit.int221.kanbanapi.databases.kanbandb.entities.Status;
import sit.int221.kanbanapi.services.StatusService;
import sit.int221.kanbanapi.services.TaskService;

import java.util.List;

@RestController
@RequestMapping("/statuses")
@CrossOrigin(origins = {"http://ip23kk2.sit.kmutt.ac.th","http://localhost:5173","http://intproj23.sit.kmutt.ac.th"})
public class StatusController {
    @Autowired
    private StatusService statusService;
    @Autowired
    private TaskService taskService;

    @GetMapping("")
    public ResponseEntity<List<Status>> getAllStatus() {
        List<Status> statuses = statusService.getAllStatus();
        return new ResponseEntity(statuses, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Status> getStatusById(@PathVariable Integer id) {
        Status status = statusService.getStatusById(id);
        return new ResponseEntity(status, HttpStatus.OK);
    }

    @PostMapping("")
    public ResponseEntity<Status> addNewStatus(@Valid @RequestBody Status status) {
        Status createdStatus = statusService.createStatus(status);
        return new ResponseEntity(createdStatus, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Status> updateStatus(@Valid @RequestBody Status status, @PathVariable Integer id) {
        Status updatedStatus = statusService.updateStatus(id, status);
        return new ResponseEntity(updatedStatus, HttpStatus.OK);
    }
    @DeleteMapping("/{id}")
    public void removeStatus(@PathVariable Integer id) {
        statusService.removeStatus(id);
    }
    @DeleteMapping("/{id}/{newId}")
    public void transferStatus(@PathVariable Integer id, @PathVariable Integer newId) {
        taskService.transferTaskStatus(id, newId);
        statusService.removeStatus(id);
    }
}
