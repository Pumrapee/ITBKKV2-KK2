package sit.int221.kanbanapi.services;

import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sit.int221.kanbanapi.databases.kanbandb.entities.Board;
import sit.int221.kanbanapi.databases.kanbandb.entities.Collab;
import sit.int221.kanbanapi.databases.kanbandb.entities.CollabId;
import sit.int221.kanbanapi.databases.kanbandb.repositories.BoardRepository;
import sit.int221.kanbanapi.databases.kanbandb.repositories.CollabRepository;
import sit.int221.kanbanapi.databases.kanbandb.repositories.MsUserRepository;
import sit.int221.kanbanapi.databases.userdb.entities.AuthUser;
import sit.int221.kanbanapi.databases.userdb.entities.User;
import sit.int221.kanbanapi.databases.userdb.repositories.UserRepository;
import sit.int221.kanbanapi.dtos.*;
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
    private ModelMapper mapper;
    @Autowired
    private JwtUserDetailsService jwtUserDetailsService;
    @Autowired
    private MsUserRepository msUserRepository;

    public List<Collab> getAllBoardCollaborators(String boardId) {
        return collabRepository.findByBoardId(boardId);
    }

    public Collab getCollaborator(String boardId, String userOid) {
        return collabRepository.findByBoardIdAndUserOid(boardId, userOid).orElseThrow(() -> new ItemNotFoundException("Collaborator not found"));
    }

    @Transactional
    public CollabAddRespondDTO addCollaborator(String boardId, CollabAddRequestDTO collabAddRequestDTO) {
        String newCollabOid = null;
        Board board = boardRepository.findById(boardId).orElseThrow(() -> new ItemNotFoundException("Board not found"));
        User newCollab = userRepository.findByEmail(collabAddRequestDTO.getEmail()).orElse(null);
        if (newCollab == null) {
            newCollabOid = msUserRepository.findByMail(collabAddRequestDTO.getEmail()).orElseThrow(() -> new ItemNotFoundException("User not found")).getId();
        } else {
            newCollabOid = newCollab.getOid();
        }
        if (collabRepository.existsById(new CollabId(boardId, newCollabOid))) {
            throw new CollaboratorConflict("Collaborator already exist!!!");
        }
        if (board.getOwnerId().equals(newCollabOid)) {
            throw new CollaboratorConflict("You are a board owner.");
        }
        Collab collab = new Collab();
        collab.setBoardId(boardId);
        collab.setUserOid(newCollabOid);
        collab.setAccessRight(collabAddRequestDTO.getAccess_right());
        CollabAddRespondDTO collaboratorDTO = mapper.map(collabRepository.save(collab), CollabAddRespondDTO.class);
        mapper.map(newCollab, collaboratorDTO);
        return collaboratorDTO;
    }

    @Transactional
    public void inviteCollaborator(String boardId, BoardInvitationRequestDTO invitation) {
        AuthUser currentUser = jwtUserDetailsService.getCurrentUser();
        Collab collab = collabRepository.findByBoardIdAndUserOid(boardId, currentUser.getUserOid()).orElseThrow(() -> new ItemNotFoundException("Collaborator not found"));
        boolean isAccept = invitation.getInvitation().equals(Invitation.ACCEPT);
        if (isAccept) {
            collab.setStatus(Collab.Status.MEMBER);
            collabRepository.save(collab);
        } else {
            collabRepository.delete(collab);
        }
    }

    @Transactional
    public Collab collabAccess(String boardId, String userOid, CollabAccessEditRequestDTO collabAccessEditRequestDTO) {
        Collab collab = collabRepository.findByBoardIdAndUserOid(boardId, userOid).orElseThrow(() -> new ItemNotFoundException("Collaborator not found"));
        collab.setAccessRight(collabAccessEditRequestDTO.getAccess_right());
        return collabRepository.save(collab);
    }

    @Transactional
    public Collab deleteCollaborator(String boardId, String userOid) {
        Board board = boardRepository.findById(boardId).orElseThrow(() -> new ItemNotFoundException("Board not found"));
        Collab collab = collabRepository.findByBoardIdAndUserOid(boardId, userOid).orElseThrow(() -> new ItemNotFoundException("Collaborator not found"));
        AuthUser currentUser = jwtUserDetailsService.getCurrentUser();
        if (!currentUser.getUserOid().equals(board.getOwnerId()) && !currentUser.getUserOid().equals(userOid)) {
            throw new AuthenticationFailed("You do not have permission to perform this action.");
        }
        collabRepository.delete(collab);
        return collab;
    }
}
