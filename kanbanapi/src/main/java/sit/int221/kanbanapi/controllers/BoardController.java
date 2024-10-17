package sit.int221.kanbanapi.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import sit.int221.kanbanapi.databases.kanbandb.entities.Board;
import sit.int221.kanbanapi.databases.userdb.entities.User;
import sit.int221.kanbanapi.dtos.*;
import sit.int221.kanbanapi.exceptions.AuthenticationFailed;
import sit.int221.kanbanapi.services.BoardService;
import sit.int221.kanbanapi.services.JwtUserDetailsService;
import sit.int221.kanbanapi.services.UserService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v3/boards")
@CrossOrigin(origins = {"http://ip23kk2.sit.kmutt.ac.th", "http://localhost:5173", "http://intproj23.sit.kmutt.ac.th", "https://intproj23.sit.kmutt.ac.th"})
public class BoardController {
    @Autowired
    BoardService boardService;
    @Autowired
    UserService userService;
    @Autowired
    ModelMapper mapper;

    @Autowired
    private JwtUserDetailsService jwtUserDetailsService;

    @GetMapping("")
    public ResponseEntity<List<BoardListDTO>> getAllBoard() {
        UserDetails user = jwtUserDetailsService.getCurrentUser();
        if (user == null) {
            throw new AuthenticationFailed("No user");
        }
        List<BoardListDTO> boardListDTOS = boardService.getUserBoards(user.getUsername());
        return ResponseEntity.ok(boardListDTOS);
    }

//    @GetMapping("")
//    public ResponseEntity<BoardAndCollabBoardListDTO> getAllBoard() {
//        UserDetails user = jwtUserDetailsService.getCurrentUser();
//        if (user == null) {
//            throw new AuthenticationFailed("No user");
//        }
//        BoardAndCollabBoardListDTO boardListDTOS = boardService.getUserBoards(user.getUsername());
//        return ResponseEntity.ok(boardListDTOS);
//    }

    @GetMapping("/{id}")
    public ResponseEntity<BoardResponseDTO> getBoardById(@PathVariable String id) {
        User owner = userService.getUserById(boardService.getBoardById(id).getOwnerId());
        Board board = boardService.getBoardById(id);
        BoardResponseDTO boardResponseDTO = new BoardResponseDTO(board.getBoardId(), board.getBoardName(), board.getVisibility(), new Owner(owner.getOid(), owner.getName()));
        return ResponseEntity.ok(boardResponseDTO);
    }

    @GetMapping("/{id}/statuses/maximum-task")
    public ResponseEntity<BoardTaskLimitDTO> getBoardTaskLimit(@PathVariable String id) {
        Board board = boardService.getBoardById(id);
        BoardTaskLimitDTO boardTaskLimitDTO = mapper.map(board, BoardTaskLimitDTO.class);
        return ResponseEntity.ok(boardTaskLimitDTO);
    }

    @PostMapping("")
    public ResponseEntity<BoardResponseDTO> createNewBoard(@Valid @RequestBody BoardCreateRequestDTO newBoard) {
        UserDetails user = jwtUserDetailsService.getCurrentUser();
        Board board = boardService.createBoard(user, newBoard.getName());
        BoardResponseDTO newBoardDTO = new BoardResponseDTO(board.getBoardId(), board.getBoardName(), board.getVisibility(), new Owner(board.getOwnerId(), user.getUsername()));
        return ResponseEntity.status(HttpStatus.CREATED).body(newBoardDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BoardResponseDTO> updateBoard(@Valid @RequestBody BoardCreateRequestDTO newBoard, @PathVariable String id) {
        User owner = userService.getUserById(boardService.getBoardById(id).getOwnerId());
        Board toUpdate = mapper.map(newBoard, Board.class);
        Board board = boardService.updateBoard(id, toUpdate);
        BoardResponseDTO newBoardDTO = new BoardResponseDTO(board.getBoardId(), board.getBoardName(), board.getVisibility(), new Owner(owner.getOid(), owner.getUsername()));
        return ResponseEntity.ok(newBoardDTO);
    }

    @PatchMapping("/{id}/statuses/maximum-task")
    public ResponseEntity<BoardTaskLimitDTO> updateBoardTaskLimit(@PathVariable String id, @RequestParam @NotNull Boolean taskLimitEnabled, @RequestParam @NotNull @Min(0) @Max(30) Integer maxTasksPerStatus) {
        Board board = boardService.getBoardById(id);
        board.setTaskLimitEnabled(taskLimitEnabled);
        board.setMaxTasksPerStatus(maxTasksPerStatus);
        Board newLimit = boardService.updateBoard(id, board);
        BoardTaskLimitDTO boardTaskLimitDTO = mapper.map(newLimit, BoardTaskLimitDTO.class);
        return ResponseEntity.ok(boardTaskLimitDTO);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<BoardVisibilityDTO> updateBoardVisibility(@PathVariable String id, @Valid @RequestBody BoardVisibilityDTO boardVisibilityRequestDTO) {
        Board board = boardService.getBoardById(id);
        board.setVisibility(boardVisibilityRequestDTO.getVisibility());
        Board newVisibility = boardService.updateBoard(id, board);
        BoardVisibilityDTO boardVisibilityResponseDTO = mapper.map(newVisibility, BoardVisibilityDTO.class);
        return ResponseEntity.ok(boardVisibilityResponseDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BoardResponseDTO> removeBoard(@PathVariable String id) {
        User owner = userService.getUserById(boardService.getBoardById(id).getOwnerId());
        Board board = boardService.removeBoard(id);
        BoardResponseDTO newBoardDTO = new BoardResponseDTO(board.getBoardId(), board.getBoardName(), board.getVisibility(), new Owner(owner.getOid(), owner.getUsername()));
        return ResponseEntity.ok(newBoardDTO);
    }
}
