package sit.int221.kanbanapi.services;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import sit.int221.kanbanapi.databases.kanbandb.entities.Board;
import sit.int221.kanbanapi.databases.kanbandb.entities.Collab;
import sit.int221.kanbanapi.databases.kanbandb.repositories.BoardRepository;
import sit.int221.kanbanapi.databases.kanbandb.repositories.CollabRepository;
import sit.int221.kanbanapi.databases.userdb.entities.User;
import sit.int221.kanbanapi.databases.userdb.repositories.UserRepository;
import sit.int221.kanbanapi.dtos.CollabAccessEditRequestDTO;
import sit.int221.kanbanapi.dtos.CollabAddRequestDTO;
import sit.int221.kanbanapi.dtos.CollabAddRespondDTO;
import sit.int221.kanbanapi.exceptions.AuthenticationFailed;
import sit.int221.kanbanapi.exceptions.CollaboratorConflict;
import sit.int221.kanbanapi.exceptions.ItemNotFoundException;

import java.util.List;

@Service
public class CollabService {
    @Autowired
    private CollabRepository collabRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BoardRepository boardRepository;
    @Autowired
    ModelMapper mapper;
    @Autowired
    private JwtUserDetailsService jwtUserDetailsService;

    public List<Collab> getAllBoardCollaborators(String boardId) {
        return collabRepository.findByBoardId(boardId);
    }

    public Collab getCollaborator(String boardId, String userOid) {
        return collabRepository.findByBoardIdAndUserOid(boardId, userOid).orElseThrow(() -> new ItemNotFoundException("Collaborator not found"));
    }

    public CollabAddRespondDTO addCollaborator(String boardId, CollabAddRequestDTO collabAddRequestDTO) {
        Board board = boardRepository.findById(boardId).orElseThrow(() -> new ItemNotFoundException("Board not found"));
        User newCollab = userRepository.findByEmail(collabAddRequestDTO.getEmail()).orElseThrow(() -> new ItemNotFoundException("Collaborator not found"));
        User currentUser = userRepository.findByUsername(jwtUserDetailsService.getCurrentUser().getUsername());
        Boolean isOwner = currentUser.getOid().equals(board.getOwnerId());
        if (!isOwner) {
            throw new AuthenticationFailed("You do not have permission to perform this action.");
        }
        if (board.getOwnerId().equals(newCollab.getOid())) {
            throw new CollaboratorConflict("You are a board owner.");
        }
        Collab collab = new Collab();
        collab.setBoardId(boardId);
        collab.setUserOid(newCollab.getOid());
        collab.setAccessRight(collabAddRequestDTO.getAccess_right());
        CollabAddRespondDTO collaboratorDTO = mapper.map(collabRepository.save(collab), CollabAddRespondDTO.class);
        mapper.map(newCollab, collaboratorDTO);
        return collaboratorDTO;
    }

    public Collab collabAccess(String boardId, String userOid, CollabAccessEditRequestDTO collabAccessEditRequestDTO) {
        Collab collab = collabRepository.findByBoardIdAndUserOid(boardId, userOid).orElseThrow(() -> new ItemNotFoundException("Collaborator not found"));
        Board board = boardRepository.findById(boardId).orElseThrow(() -> new ItemNotFoundException("Board not found"));
        User currentUser = userRepository.findByUsername(jwtUserDetailsService.getCurrentUser().getUsername());
        if (currentUser.getOid().equals(board.getOwnerId())) {
            throw new AuthenticationFailed("You do not have permission to perform this action.");
        }
        collab.setAccessRight(collabAccessEditRequestDTO.getAccess_right());
        return collabRepository.save(collab);
    }

    public Collab deleteCollaborator(String boardId, String userOid) {
        Board board = boardRepository.findById(boardId).orElseThrow(() -> new ItemNotFoundException("Board not found"));
        Collab collab = collabRepository.findByBoardIdAndUserOid(boardId, userOid).orElseThrow(() -> new ItemNotFoundException("Collaborator not found"));
        User currentUser = userRepository.findByUsername(jwtUserDetailsService.getCurrentUser().getUsername());
        if (currentUser.getOid().equals(board.getOwnerId()) || !currentUser.getOid().equals(userOid)) {
            throw new AuthenticationFailed("You do not have permission to perform this action.");
        }
        collabRepository.delete(collab);
        return collab;
    }
}
