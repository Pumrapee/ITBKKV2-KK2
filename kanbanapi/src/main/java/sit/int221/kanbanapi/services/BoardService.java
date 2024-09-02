package sit.int221.kanbanapi.services;

import io.viascom.nanoid.NanoId;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import sit.int221.kanbanapi.databases.kanbandb.entities.Board;
import sit.int221.kanbanapi.databases.kanbandb.repositories.BoardRepository;
import sit.int221.kanbanapi.databases.userdb.repositories.UserRepository;
import sit.int221.kanbanapi.exceptions.BadRequestException;

import java.util.List;

@Service
public class BoardService {
    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    UserRepository userRepository;

    public List<Board> getUserBoards(String user) {
        return boardRepository.findByOwnerId(userRepository.findByUsername(user).getOid());
    }

    public Board getBoardById(String boardId) {
        return  boardRepository.findById(boardId).orElseThrow(() -> new BadRequestException("Board "+ boardId + " does not exist"));
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
        return boardRepository.save(board);
    }

    @Transactional
    public Board removeBoard(String id) {
        Board board = boardRepository.findById(id).orElseThrow(() -> new BadRequestException("Board "+ id + " does not exist"));
        boardRepository.delete(board);
        return board;
    }

    @Transactional
    public Board updateBoard(String id, String boardName) {
        Board board = getBoardById(id);
        board.setBoardName(boardName);
        return boardRepository.save(board);
    }
}
