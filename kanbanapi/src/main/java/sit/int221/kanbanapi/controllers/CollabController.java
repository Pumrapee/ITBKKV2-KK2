package sit.int221.kanbanapi.controllers;

import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sit.int221.kanbanapi.databases.kanbandb.entities.Collab;
import sit.int221.kanbanapi.databases.userdb.entities.User;
import sit.int221.kanbanapi.dtos.*;
import sit.int221.kanbanapi.services.CollabService;
import sit.int221.kanbanapi.services.UserService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v3/boards/{boardId}/collabs")
@CrossOrigin(origins = {"http://ip23kk2.sit.kmutt.ac.th", "http://localhost:5173", "http://intproj23.sit.kmutt.ac.th", "https://intproj23.sit.kmutt.ac.th"})
public class CollabController {
    @Autowired
    CollabService collabService;

    @Autowired
    UserService userService;

    @Autowired
    ModelMapper mapper;

    @GetMapping
    public ResponseEntity<List<CollaboratorDTO>> getCollaborators(@PathVariable String boardId) {
        List<Collab> collaborators = collabService.getAllBoardCollaborators(boardId);
        List<CollaboratorDTO> collaboratorDTOS = collaborators.stream().map(collab -> {
            User user = userService.getUserById(collab.getUserOid());
            CollaboratorDTO collaboratorDTO = mapper.map(collab, CollaboratorDTO.class);
            mapper.map(user, collaboratorDTO);
            return collaboratorDTO;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(collaboratorDTOS);
    }

    @GetMapping("/{collab_oid}")
    public ResponseEntity<CollaboratorDTO> getCollaborator(@PathVariable String boardId, @PathVariable String collab_oid) {
        Collab collab = collabService.getCollaborator(boardId, collab_oid);
        User user = userService.getUserById(collab.getUserOid());
        CollaboratorDTO collaboratorDTO = mapper.map(collab, CollaboratorDTO.class);
        mapper.map(user, collaboratorDTO);
        return ResponseEntity.ok(collaboratorDTO);
    }

    @PostMapping
    public ResponseEntity<CollabAddRespondDTO> addCollaborator(@PathVariable String boardId, @Valid @RequestBody CollabAddRequestDTO collabAddRequestDTO) {
        CollabAddRespondDTO collaboratorDTO = collabService.addCollaborator(boardId, collabAddRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(collaboratorDTO);
    }

    @PostMapping("/invitations")
    public void collabStatus(@PathVariable String boardId, @Valid @RequestBody BoardInvitationRequestDTO boardInvitationRequestDTO) {
        collabService.inviteCollaborator(boardId, boardInvitationRequestDTO);
    }

    @PatchMapping("/{collab_oid}")
    public ResponseEntity<CollaboratorDTO> collabAccess(@PathVariable String boardId, @PathVariable String collab_oid, @Valid @RequestBody CollabAccessEditRequestDTO collabAccessEditRequestDTO) {
        Collab collab = collabService.collabAccess(boardId, collab_oid, collabAccessEditRequestDTO);
        User user = userService.getUserById(collab.getUserOid());
        CollaboratorDTO collaboratorDTO = mapper.map(collab, CollaboratorDTO.class);
        mapper.map(user, collaboratorDTO);
        return ResponseEntity.ok(collaboratorDTO);
    }

    @DeleteMapping("/{collab_oid}")
    public ResponseEntity<CollaboratorDTO> deleteCollaborator(@PathVariable String boardId, @PathVariable String collab_oid) {
        Collab collab = collabService.deleteCollaborator(boardId, collab_oid);
        User user = userService.getUserById(collab.getUserOid());
        CollaboratorDTO collaboratorDTO = mapper.map(collab, CollaboratorDTO.class);
        mapper.map(user, collaboratorDTO);
        return ResponseEntity.ok(collaboratorDTO);
    }
}