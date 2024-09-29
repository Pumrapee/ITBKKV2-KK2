package sit.int221.kanbanapi.services;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import sit.int221.kanbanapi.configs.StatusConfig;
import sit.int221.kanbanapi.databases.kanbandb.entities.Board;
import sit.int221.kanbanapi.databases.kanbandb.entities.Status;
import sit.int221.kanbanapi.databases.kanbandb.repositories.BoardRepository;
import sit.int221.kanbanapi.exceptions.BadRequestException;
import sit.int221.kanbanapi.exceptions.ItemNotFoundException;
import sit.int221.kanbanapi.databases.kanbandb.repositories.StatusRepository;

import java.util.List;

@Service
public class StatusService {

    @Autowired
    private StatusRepository repository;

    @Autowired
    private BoardService boardService;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private StatusConfig configuration;

    public List<Status> getAllStatus() {
        return repository.findAll();
    }

    public List<Status> getAllBoardStatus(Board board) {
        return repository.findAllByBoard(board);
    }

    public Status getStatusById(String boardId, Integer id) {
        Status status = checkBoardStatus(boardId, id);
        return status;
    }

    @Transactional
    public Status createStatus(Status status, String boardId) {
        Board board = boardService.getBoardById(boardId);
        if (repository.existsByNameAndBoard(status.getName(), board)) {
            throw new BadRequestException("Status name must be unique within the board");
        }
        status.setBoard(board);
        return repository.save(status);
    }

    @Transactional
    public Status removeStatus(String boardId, Integer id) {
        Status status = checkBoardStatus(boardId, id);
        if (configuration.getNonLimitedUpdatableDeletableStatuses().contains(status.getName())) {
            throw new BadRequestException("The status name '"+ status.getName() + "' cannot be deleted.");
        } else {
            repository.delete(status);
            return status;
        }
    }

    @Transactional
    public Status updateStatus(Integer id, Status status, String boardId) {
        Board board = boardService.getBoardById(boardId);
        Status existingStatus = checkBoardStatus(boardId, id);
        if (configuration.getNonLimitedUpdatableDeletableStatuses().contains(existingStatus.getName())) {
            throw new BadRequestException("The status name '" + existingStatus.getName() + "' cannot be changed.");
        }
        if (repository.existsByNameAndIdNotAndBoard(status.getName(), id, board)) {
            throw new BadRequestException("Status name must be unique within the board");
        }
        existingStatus.setName(status.getName());
        existingStatus.setDescription(status.getDescription());
        existingStatus.setColor(status.getColor());
        return repository.save(existingStatus);
    }

    public Status getStatusByName(String statusName, String boardId) {
        try {
            Integer statusId = Integer.parseInt(statusName);
            return checkBoardStatus(boardId, statusId);
        } catch (NumberFormatException e) {
            Board board = boardService.getBoardById(boardId);
            if (statusName == null || statusName.isBlank()) {
                return repository.findByNameAndBoard("No Status", board).orElseThrow(() -> new BadRequestException("Status " + statusName + " does not exist"));
            }
            return repository.findByNameAndBoard(statusName, board).orElseThrow(() -> new BadRequestException("Status " + statusName + " does not exist"));
        }
    }

    private Status checkBoardStatus(String boardId, Integer id) {
        Board board = boardService.getBoardById(boardId);
        Status status = repository.findById(id).orElseThrow(() -> new ItemNotFoundException("Status " + id + " does not exist !!!"));
        if (!status.getBoard().equals(board)) {
            throw new BadRequestException("Status " + id + " is not belong to board " + board.getId() + " !!!");
        }
        return status;
    }
}
