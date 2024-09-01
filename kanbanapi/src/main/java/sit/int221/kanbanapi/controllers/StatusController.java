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
@RequestMapping("/board/{id}/statuses")
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

    @GetMapping("/{statusId}")
    public ResponseEntity<Status> getStatusById(@PathVariable Integer statusId) {
        Status status = statusService.getStatusById(statusId);
        return new ResponseEntity(status, HttpStatus.OK);
    }

    @PostMapping("")
    public ResponseEntity<Status> addNewStatus(@Valid @RequestBody Status status) {
        Status createdStatus = statusService.createStatus(status);
        return new ResponseEntity(createdStatus, HttpStatus.CREATED);
    }

    @PutMapping("/{statusId}")
    public ResponseEntity<Status> updateStatus(@Valid @RequestBody Status status, @PathVariable Integer statusId) {
        Status updatedStatus = statusService.updateStatus(statusId, status);
        return new ResponseEntity(updatedStatus, HttpStatus.OK);
    }
    @DeleteMapping("/{statusId}")
    public void removeStatus(@PathVariable Integer statusId) {
        statusService.removeStatus(statusId);
    }
    @DeleteMapping("/{statusId}/{newStatusId}")
    public void transferStatus(@PathVariable Integer statusId, @PathVariable Integer newStatusId) {
        taskService.transferTaskStatus(statusId, newStatusId);
        statusService.removeStatus(statusId);
    }
}
