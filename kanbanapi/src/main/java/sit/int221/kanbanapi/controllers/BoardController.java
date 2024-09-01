package sit.int221.kanbanapi.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import sit.int221.kanbanapi.databases.kanbandb.entities.Board;
import sit.int221.kanbanapi.databases.userdb.entities.User;
import sit.int221.kanbanapi.databases.userdb.repositories.UserRepository;
import sit.int221.kanbanapi.dtos.BoardCreateRequestDTO;
import sit.int221.kanbanapi.dtos.BoardResponseDTO;
import sit.int221.kanbanapi.dtos.Owner;
import sit.int221.kanbanapi.services.BoardService;
import sit.int221.kanbanapi.services.UserService;

import java.util.List;

@RestController
@RequestMapping("/boards")
@CrossOrigin(origins = {"http://ip23kk2.sit.kmutt.ac.th","http://localhost:5173","http://intproj23.sit.kmutt.ac.th"})
public class BoardController {
    @Autowired
    BoardService boardService;
    @Autowired
    UserService userService;

    @GetMapping("")
    public ResponseEntity<List<Board>> getAllBoard(@AuthenticationPrincipal UserDetails user) {
        List<Board> boards = boardService.getUserBoards(user.getUsername());
        return new ResponseEntity(boards, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Board> getBoardById(@PathVariable String id) {
        Board board = boardService.getBoardById(id);
        User user = userService. getUserById(board.getOwnerId());
        BoardResponseDTO boardResponseDTO = new BoardResponseDTO(board.getBoardId(), board.getBoardName(), new Owner(user.getOid(), user.getName()));
        return new ResponseEntity(boardResponseDTO, HttpStatus.OK);
    }

    @PostMapping("")
    public ResponseEntity<List<Board>> createNewBoard(@AuthenticationPrincipal UserDetails user, @Valid @RequestBody BoardCreateRequestDTO newBoard) {
        Board board = boardService.createBoard(user, newBoard.getName());
        BoardResponseDTO newBoardDTO = new BoardResponseDTO(board.getBoardId(), board.getBoardName(), new Owner(board.getOwnerId(), user.getUsername()));
        return new ResponseEntity(newBoardDTO, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<List<Board>> removeBoard(@AuthenticationPrincipal UserDetails user, @PathVariable String id) {
        Board board = boardService.removeBoard(id);
        BoardResponseDTO newBoardDTO = new BoardResponseDTO(board.getBoardId(), board.getBoardName(), new Owner(board.getOwnerId(), user.getUsername()));
        return new ResponseEntity(newBoardDTO, HttpStatus.OK);
    }
}
