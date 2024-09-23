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
import sit.int221.kanbanapi.databases.kanbandb.entities.Board;
import sit.int221.kanbanapi.databases.userdb.entities.User;
import sit.int221.kanbanapi.dtos.*;
import sit.int221.kanbanapi.services.BoardService;
import sit.int221.kanbanapi.services.JwtUserDetailsService;
import sit.int221.kanbanapi.services.UserService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v3/boards")
@CrossOrigin(origins = {"http://ip23kk2.sit.kmutt.ac.th","http://localhost:5173","http://intproj23.sit.kmutt.ac.th"})
public class BoardController {
    @Autowired
    BoardService boardService;
    @Autowired
    UserService userService;
    @Autowired
    ModelMapper mapper;
    @Qualifier("securityFilterChain")
    @Autowired
    private SecurityFilterChain securityFilterChain;
    @Autowired
    private JwtUserDetailsService jwtUserDetailsService;

    @GetMapping("")
    public ResponseEntity<List<Board>> getAllBoard() {
        UserDetails user = jwtUserDetailsService.getCurrentUser();
        List<Board> boards = boardService.getUserBoards(user.getUsername());
        List<BoardListDTO> boardListDTOS = boards.stream().map(board -> {
            BoardListDTO boardListDTO = mapper.map(board, BoardListDTO.class);
            return boardListDTO;
        }).collect(Collectors.toList());
        return new ResponseEntity(boardListDTOS, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Board> getBoardById(@PathVariable String id, HttpServletRequest request) {
        User owner = boardService.checkBoardOwnership(id, request.getMethod());
        Board board = boardService.getBoardById(id);
        BoardResponseDTO boardResponseDTO = new BoardResponseDTO(board.getBoardId(), board.getBoardName(), board.getVisibility(), new Owner(owner.getOid(), owner.getName()));
        return new ResponseEntity(boardResponseDTO, HttpStatus.OK);
    }

    @GetMapping("/{id}/statuses/maximum-task")
    public ResponseEntity<Board> getBoardTaskLimit(@PathVariable String id, HttpServletRequest request) {
        User owner = boardService.checkBoardOwnership(id, request.getMethod());
        Board board = boardService.getBoardById(id);
        BoardTaskLimitDTO boardTaskLimitDTO = mapper.map(board, BoardTaskLimitDTO.class);
        return new ResponseEntity(boardTaskLimitDTO, HttpStatus.OK);
    }

    @PostMapping("")
    public ResponseEntity<Board> createNewBoard(@Valid @RequestBody BoardCreateRequestDTO newBoard) {
        UserDetails user = jwtUserDetailsService.getCurrentUser();
        Board board = boardService.createBoard(user, newBoard.getName());
        BoardResponseDTO newBoardDTO = new BoardResponseDTO(board.getBoardId(), board.getBoardName(), board.getVisibility(), new Owner(board.getOwnerId(), user.getUsername()));
        return new ResponseEntity(newBoardDTO, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Board> updateBoard(@Valid @RequestBody BoardCreateRequestDTO newBoard, @PathVariable String id, HttpServletRequest request) {
        User owner = boardService.checkBoardOwnership(id, request.getMethod());
        Board toUpdate = mapper.map(newBoard, Board.class);
        Board board = boardService.updateBoard(id, toUpdate);
        BoardResponseDTO newBoardDTO = new BoardResponseDTO(board.getBoardId(), board.getBoardName(), board.getVisibility(), new Owner(owner.getOid(), owner.getUsername()));
        return new ResponseEntity(newBoardDTO, HttpStatus.OK);
    }

    @PatchMapping("/{id}/statuses/maximum-task")
    public ResponseEntity<Board> updateBoardTaskLimit(@PathVariable String id, @RequestParam @NotNull Boolean taskLimitEnabled, @RequestParam @NotNull @Min(0) @Max(30) Integer maxTasksPerStatus, HttpServletRequest request) {
        boardService.checkBoardOwnership(id, request.getMethod());
        Board board = boardService.getBoardById(id);
        board.setTaskLimitEnabled(taskLimitEnabled);
        board.setMaxTasksPerStatus(maxTasksPerStatus);
        Board newLimit = boardService.updateBoard(id, board);
        BoardTaskLimitDTO boardTaskLimitDTO = mapper.map(newLimit, BoardTaskLimitDTO.class);
        return new ResponseEntity(boardTaskLimitDTO, HttpStatus.OK);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Board> updateBoardVisibility(@PathVariable String id, @Valid @RequestBody BoardVisibilityDTO boardVisibilityRequestDTO, HttpServletRequest request) {
        boardService.checkBoardOwnership(id, request.getMethod());
        Board board = boardService.getBoardById(id);
        board.setVisibility(boardVisibilityRequestDTO.getVisibility());
        Board newVisibility = boardService.updateBoard(id, board);
        BoardVisibilityDTO boardVisibilityResponseDTO = mapper.map(newVisibility, BoardVisibilityDTO.class);
        return new ResponseEntity(boardVisibilityResponseDTO, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Board> removeBoard(@PathVariable String id, HttpServletRequest request) {
        User owner = boardService.checkBoardOwnership(id, request.getMethod());
        Board board = boardService.removeBoard(id);
        BoardResponseDTO newBoardDTO = new BoardResponseDTO(board.getBoardId(), board.getBoardName(), board.getVisibility(), new Owner(owner.getOid(), owner.getUsername()));
        return new ResponseEntity(newBoardDTO, HttpStatus.OK);
    }
}
