package sit.int221.kanbanapi.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import sit.int221.kanbanapi.configs.StatusConfig;
import sit.int221.kanbanapi.databases.kanbandb.entities.Board;
import sit.int221.kanbanapi.databases.userdb.entities.User;
import sit.int221.kanbanapi.dtos.*;
import sit.int221.kanbanapi.exceptions.BadRequestException;
import sit.int221.kanbanapi.services.BoardService;
import sit.int221.kanbanapi.services.UserService;

import java.io.IOException;
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

    @GetMapping("")
    public ResponseEntity<List<Board>> getAllBoard(@AuthenticationPrincipal UserDetails user) {
        List<Board> boards = boardService.getUserBoards(user.getUsername());
        List<BoardListDTO> boardListDTOS = boards.stream().map(board -> {
            BoardListDTO boardListDTO = mapper.map(board, BoardListDTO.class);
            return boardListDTO;
        }).collect(Collectors.toList());
        return new ResponseEntity(boardListDTOS, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Board> getBoardById(@PathVariable String id, @AuthenticationPrincipal UserDetails user) {
        User owner = boardService.checkBoardOwnership(id, user.getUsername());
        Board board = boardService.getBoardById(id);
        BoardResponseDTO boardResponseDTO = new BoardResponseDTO(board.getBoardId(), board.getBoardName(), new Owner(owner.getOid(), owner.getName()));
        return new ResponseEntity(boardResponseDTO, HttpStatus.OK);
    }

    @GetMapping("/{id}/statuses/maximum-task")
    public ResponseEntity<Board> getBoardTaskLimit(@PathVariable String id, @AuthenticationPrincipal UserDetails user) {
        User owner = boardService.checkBoardOwnership(id, user.getUsername());
        Board board = boardService.getBoardById(id);
        BoardTaskLimitDTO boardTaskLimitDTO = mapper.map(board, BoardTaskLimitDTO.class);
        return new ResponseEntity(boardTaskLimitDTO, HttpStatus.OK);
    }

    @PostMapping("")
    public ResponseEntity<Board> createNewBoard(@AuthenticationPrincipal UserDetails user, @Valid @RequestBody BoardCreateRequestDTO newBoard) {
        Board board = boardService.createBoard(user, newBoard.getName());
        BoardResponseDTO newBoardDTO = new BoardResponseDTO(board.getBoardId(), board.getBoardName(), new Owner(board.getOwnerId(), user.getUsername()));
        return new ResponseEntity(newBoardDTO, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Board> updateBoard(@AuthenticationPrincipal UserDetails user, @Valid @RequestBody BoardCreateRequestDTO newBoard, @PathVariable String id) {
        Board toUpdate = mapper.map(newBoard, Board.class);
        Board board = boardService.updateBoard(id, toUpdate, user.getUsername());
        BoardResponseDTO newBoardDTO = new BoardResponseDTO(board.getBoardId(), board.getBoardName(), new Owner(board.getOwnerId(), user.getUsername()));
        return new ResponseEntity(newBoardDTO, HttpStatus.OK);
    }

    @PatchMapping("/{id}/statuses/maximum-task")
    public ResponseEntity<Board> updateBoardTaskLimit(@AuthenticationPrincipal UserDetails user, @PathVariable String id, @RequestParam @NotNull Boolean taskLimitEnabled, @RequestParam @NotNull @Min(0) @Max(30) Integer maxTasksPerStatus) {
        Board board = boardService.getBoardById(id);
        board.setTaskLimitEnabled(taskLimitEnabled);
        board.setMaxTasksPerStatus(maxTasksPerStatus);
        Board newLimit = boardService.updateBoard(id, board, user.getUsername());
        BoardTaskLimitDTO boardTaskLimitDTO = mapper.map(newLimit, BoardTaskLimitDTO.class);
        return new ResponseEntity(boardTaskLimitDTO, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Board> removeBoard(@AuthenticationPrincipal UserDetails user, @PathVariable String id) {
        Board board = boardService.removeBoard(id, user.getUsername());
        BoardResponseDTO newBoardDTO = new BoardResponseDTO(board.getBoardId(), board.getBoardName(), new Owner(board.getOwnerId(), user.getUsername()));
        return new ResponseEntity(newBoardDTO, HttpStatus.OK);
    }
}
