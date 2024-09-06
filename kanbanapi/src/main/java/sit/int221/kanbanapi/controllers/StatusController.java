package sit.int221.kanbanapi.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import sit.int221.kanbanapi.databases.kanbandb.entities.Status;
import sit.int221.kanbanapi.services.BoardService;
import sit.int221.kanbanapi.services.StatusService;
import sit.int221.kanbanapi.services.TaskService;

import java.util.List;

@RestController
@RequestMapping("/boards/{boardId}/statuses")
@CrossOrigin(origins = {"http://ip23kk2.sit.kmutt.ac.th","http://localhost:5173","http://intproj23.sit.kmutt.ac.th"})
public class StatusController {
    @Autowired
    private StatusService statusService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private BoardService boardService;

    @GetMapping("")
    public ResponseEntity<List<Status>> getAllStatus(@PathVariable String boardId, @AuthenticationPrincipal UserDetails user) {
        List<Status> statuses = statusService.getAllBoardStatus(boardService.getBoardById(boardId), user.getUsername());
        return new ResponseEntity<>(statuses, HttpStatus.OK);
    }

    @GetMapping("/{statusId}")
    public ResponseEntity<Status> getStatusById(@PathVariable Integer statusId, @PathVariable String boardId, @AuthenticationPrincipal UserDetails user) {
        Status status = statusService.getStatusById(statusId, boardId, user.getUsername());
        return new ResponseEntity<>(status, HttpStatus.OK);
    }

    @PostMapping("")
    public ResponseEntity<Status> addNewStatus(@Valid @RequestBody Status status, @PathVariable String boardId, @AuthenticationPrincipal UserDetails user) {
        Status createdStatus = statusService.createStatus(status, boardId, user.getUsername());
        return new ResponseEntity<>(createdStatus, HttpStatus.CREATED);
    }

    @PutMapping("/{statusId}")
    public ResponseEntity<Status> updateStatus(@Valid @RequestBody Status status, @PathVariable Integer statusId, @PathVariable String boardId, @AuthenticationPrincipal UserDetails user) {
        Status updatedStatus = statusService.updateStatus(statusId, status, boardId, user.getUsername());
        return new ResponseEntity<>(updatedStatus, HttpStatus.OK);
    }

    @DeleteMapping("/{statusId}")
    public void removeStatus(@PathVariable Integer statusId, @PathVariable String boardId, @AuthenticationPrincipal UserDetails user) {
        statusService.removeStatus(statusId, boardId, user.getUsername());
    }

    @DeleteMapping("/{statusId}/{newStatusId}")
    public void transferStatus(@PathVariable Integer statusId, @PathVariable Integer newStatusId, @PathVariable String boardId, @AuthenticationPrincipal UserDetails user) {
        taskService.transferTaskStatus(statusId, newStatusId, boardId, user.getUsername());
        statusService.removeStatus(statusId, boardId, user.getUsername());
    }
}
