package sit.int221.kanbanapi.services;

import io.viascom.nanoid.NanoId;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import sit.int221.kanbanapi.databases.kanbandb.entities.Board;
import sit.int221.kanbanapi.databases.kanbandb.entities.Status;
import sit.int221.kanbanapi.databases.kanbandb.repositories.BoardRepository;
import sit.int221.kanbanapi.databases.kanbandb.repositories.StatusRepository;
import sit.int221.kanbanapi.databases.userdb.entities.User;
import sit.int221.kanbanapi.databases.userdb.repositories.UserRepository;
import sit.int221.kanbanapi.exceptions.AuthenticationFailed;
import sit.int221.kanbanapi.exceptions.BadRequestException;
import sit.int221.kanbanapi.exceptions.ItemNotFoundException;
import sit.int221.kanbanapi.exceptions.NoPermission;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class BoardService {
    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StatusRepository statusRepository;

    @Autowired
    private JwtUserDetailsService jwtUserDetailsService;

    public List<Board> getUserBoards(String user) {
        return boardRepository.findByOwnerId(userRepository.findByUsername(user).getOid());
    }

    public Board getBoardById(String boardId) {
        return  boardRepository.findById(boardId).orElseThrow(() -> new ItemNotFoundException("Board "+ boardId + " does not exist"));
    }

    @Transactional
    public Board createBoard(UserDetails owner, String boardName) {
        Board board = new Board();
        String boardId = NanoId.generate(10);
        while (boardRepository.existsById(boardId)) {
            boardId = NanoId.generate(10);
        }
        board.setBoardId(boardId);
        board.setBoardName(boardName);
        board.setOwnerId(userRepository.findByUsername(owner.getUsername()).getOid());
        board.setTaskLimitEnabled(Boolean.FALSE);
        board.setMaxTasksPerStatus(10);
        Board savedBoard = boardRepository.save(board);
        createDefaultStatuses(savedBoard);
        return savedBoard;
    }

    @Transactional
    public Board removeBoard(String boardId) {
        Board board = boardRepository.findById(boardId).orElseThrow(() -> new BadRequestException("Board "+ boardId + " does not exist"));
        boardRepository.delete(board);
        return board;
    }

    @Transactional
    public Board updateBoard(String boardId, Board newBoard) {
        Board board = getBoardById(boardId);
        board.setBoardName(newBoard.getBoardName());
        board.setTaskLimitEnabled(newBoard.getTaskLimitEnabled());
        board.setMaxTasksPerStatus(newBoard.getMaxTasksPerStatus());
        return boardRepository.save(board);
    }

    public User checkBoardOwnership(String boardId, String requestMethod, String userOid) {
        Board board = getBoardById(boardId);
        User currentUser = userRepository.findById(userOid).orElseThrow(() -> new AuthenticationFailed("User " + userOid + " does not exist"));
        User owner = userRepository.findById(board.getOwnerId()).orElseThrow(() -> new AuthenticationFailed("User " + board.getOwnerId() + " does not exist"));
        if (currentUser != null) {
            String username = currentUser.getUsername();
            Boolean isOwner = owner.getUsername().equals(username);
            if (!isOwner && board.getVisibility().equals("PRIVATE")) {
                throw new NoPermission("You do not have permission to perform this action.");
            }
            if (!isOwner && board.getVisibility().equals("PUBLIC") && !requestMethod.equals("GET")) {
                throw new NoPermission("You do not have permission to perform this action.");
            }
        } else if (board.getVisibility().equals("PRIVATE") || !requestMethod.equals("GET")){
            throw new NoPermission("You do not have permission to perform this action.");
        }
        return owner;
    }

    private void createDefaultStatuses(Board board) {
        Status noStatus = new Status("No Status", "Default status", "#ffffff", board);
        Status toDo = new Status("To Do", "Initial task status", "#478CCF", board);
        Status doing = new Status("Doing", "Task is in progress", "#FEB941", board);
        Status done = new Status("Done", "Task is completed", "#77E4C8", board);
        statusRepository.saveAll(Arrays.asList(noStatus, toDo, doing, done));
    }
}
