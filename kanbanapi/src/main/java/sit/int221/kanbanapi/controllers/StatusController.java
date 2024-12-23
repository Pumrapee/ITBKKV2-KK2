package sit.int221.kanbanapi.controllers;

import jakarta.servlet.http.HttpServletRequest;
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
@RequestMapping("/v3/boards/{boardId}/statuses")
@CrossOrigin(origins = {"http://ip23kk2.sit.kmutt.ac.th", "http://localhost:5173", "http://intproj23.sit.kmutt.ac.th", "https://intproj23.sit.kmutt.ac.th"})
public class StatusController {
    @Autowired
    private StatusService statusService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private BoardService boardService;

    @GetMapping("")
    public ResponseEntity<List<Status>> getAllStatus(@PathVariable String boardId) {
        List<Status> statuses = statusService.getAllBoardStatus(boardService.getBoardById(boardId));
        return ResponseEntity.ok(statuses);
    }

    @GetMapping("/{statusId}")
    public ResponseEntity<Status> getStatusById(@PathVariable Integer statusId,
                                                @PathVariable String boardId) {
        Status status = statusService.getStatusById(boardId, statusId);
        return ResponseEntity.ok(status);
    }

    @PostMapping("")
    public ResponseEntity<Status> addNewStatus(@Valid @RequestBody Status status,
                                               @PathVariable String boardId) {
        Status createdStatus = statusService.createStatus(status, boardId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdStatus);
    }

    @PutMapping("/{statusId}")
    public ResponseEntity<Status> updateStatus(@Valid @RequestBody Status status,
                                               @PathVariable Integer statusId,
                                               @PathVariable String boardId) {
        Status updatedStatus = statusService.updateStatus(statusId, status, boardId);
        return ResponseEntity.ok(updatedStatus);
    }

    @DeleteMapping("/{statusId}")
    public void removeStatus(@PathVariable Integer statusId,
                             @PathVariable String boardId) {
        statusService.removeStatus(boardId, statusId);
    }

    @DeleteMapping("/{statusId}/{newStatusId}")
    public void transferStatus(@PathVariable Integer statusId,
                               @PathVariable Integer newStatusId,
                               @PathVariable String boardId) {
        taskService.transferTaskStatus(statusId, newStatusId, boardId);
        statusService.removeStatus(boardId, statusId);
    }
}
