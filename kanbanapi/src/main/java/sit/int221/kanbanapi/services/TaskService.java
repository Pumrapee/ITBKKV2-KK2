package sit.int221.kanbanapi.services;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.stereotype.Service;
import sit.int221.kanbanapi.configs.StatusConfig;
import sit.int221.kanbanapi.databases.kanbandb.entities.Board;
import sit.int221.kanbanapi.databases.kanbandb.entities.Status;
import sit.int221.kanbanapi.databases.kanbandb.entities.Task;
import sit.int221.kanbanapi.exceptions.BadRequestException;
import sit.int221.kanbanapi.exceptions.ItemNotFoundException;
import sit.int221.kanbanapi.exceptions.TaskLimitExceededException;
import sit.int221.kanbanapi.databases.kanbandb.repositories.StatusRepository;
import sit.int221.kanbanapi.databases.kanbandb.repositories.TaskRepository;

import java.util.List;

import static java.util.Arrays.stream;

@Service
public class TaskService {

    @Autowired
    private TaskRepository repository;

    @Autowired
    private StatusRepository statusRepository;

    @Autowired
    private BoardService boardService;

    @Autowired
    private StatusConfig configuration;

    public List<Task> getAllTask() {
        return repository.findAll();
    }

    public List<Task> getAllTaskFilteredSorted(List<String> filterStatuses, String sortBy, String boardId) {
        Board board = boardService.getBoardById(boardId);
        String sortProperty = sortBy != null ? sortBy : "createdOn";
        Sort sort = Sort.by(Sort.Order.asc(sortProperty));
        try {
            if (filterStatuses != null && !filterStatuses.isEmpty()) {
                return repository.findByStatusNamesSorted(filterStatuses, board, sort);
            } else {
                return repository.findByBoardSorted(board, sort);
            }
        } catch (PropertyReferenceException e) {
            throw new BadRequestException("Invalid sortBy parameter: " + sortProperty);
        }
    }

    public Task getTaskById(String boardId,Integer id) {
        Task task = checkBoardTask(boardId, id);
        return task;
    }

    @Transactional
    public Task createTask(Task task, String boardId) {
        Board board = boardService.getBoardById(boardId);
        task.setBoard(board);
        if (configuration.getNonLimitedUpdatableDeletableStatuses().contains(task.getTaskStatus().getName())
                || statusLimitCheck(boardId, countTasksByStatus(task.getTaskStatus().getId(), board) + 1)) {
            return repository.save(task);
        } else {
            throw new TaskLimitExceededException("The status " + task.getTaskStatus().getName() + " has reached the task limit.");
        }
    }

    @Transactional
    public Task removeTask(String boardId, Integer id) {
        Task task = checkBoardTask(boardId, id);
        repository.delete(task);
        return task;
    }

    @Transactional
    public Task updateTask(Integer id, Task task, String boardId) {
        checkBoardTask(boardId, id);
        Board board = boardService.getBoardById(boardId);

        if (configuration.getNonLimitedUpdatableDeletableStatuses().contains(task.getTaskStatus().getName())
                || statusLimitCheck(boardId, countTasksByStatus(task.getTaskStatus().getId(), board) + 1)) {
            Task existingTask = repository.findById(id).orElseThrow(() -> new BadRequestException("Task id " + id + " does not exist !!!"));
            existingTask.setTitle(task.getTitle());
            existingTask.setDescription(task.getDescription());
            existingTask.setAssignees(task.getAssignees());
            existingTask.setTaskStatus(task.getTaskStatus());
            return repository.save(existingTask);
        } else {
            throw new TaskLimitExceededException("The status " + task.getTaskStatus().getName() + " has reached the task limit.");
        }
    }

    @Transactional
    public void transferTaskStatus(Integer id, Integer newId, String boardId) {
        if (id.equals(newId)) {
            throw new BadRequestException("Destination status for task transfer must be different from the current status");
        }
        Board board = boardService.getBoardById(boardId);
        Status oldStatus = statusRepository.findById(id).orElseThrow(() -> new BadRequestException("The specified status for task transfer does not exist"));
        Status newStatus = statusRepository.findById(newId).orElseThrow(() -> new BadRequestException("The specified status for task transfer does not exist"));
        if (!oldStatus.getBoard().equals(board) || !newStatus.getBoard().equals(board)) {
            throw new BadRequestException("Status " + id + "or" + newId + " is not belong to board " + board.getId() + " !!!");
        }
        if (configuration.getNonLimitedUpdatableDeletableStatuses().contains(oldStatus.getName())){
            throw new BadRequestException("The status name '"+ oldStatus.getName() + "' cannot be transferred");
        }
        if (!configuration.getNonLimitedUpdatableDeletableStatuses().contains(oldStatus.getName())
                && configuration.getNonLimitedUpdatableDeletableStatuses().contains(newStatus.getName())
                || statusLimitCheck(boardId, countTasksByStatus(id, board) + countTasksByStatus(newId, board))){
            repository.transferTaskStatus(id, newId, board);
        } else {
            throw new TaskLimitExceededException("The destination status cannot be over the limit after transfer");
        }
    }

    public boolean findTaskStatus(Integer id, String boardId) {
        Board board = boardService.getBoardById(boardId);
        return (repository.countTasksByStatus(id, board) != 0);
    }

    public Integer countTasksByStatus(Integer id, Board board) {
        return repository.countTasksByStatus(id, board);
    }

    public Boolean statusLimitCheck(String boardId, Integer count) {
        Board board = boardService.getBoardById(boardId);
        if (board.getTaskLimitEnabled()) {
            return count <= board.getMaxTasksPerStatus();
        } else {
            return true;
        }
    }

    public Boolean allStatusLimitCheck(StatusConfig newConfig, String boardId) {
        Board board = boardService.getBoardById(boardId);
        if (newConfig.getTaskLimitEnabled()) {
            return repository.countTasksByStatus(board).stream().allMatch(tasks -> tasks <= newConfig.getMaxTasksPerStatus());
        } else {
            return true;
        }
    }
    private Task checkBoardTask(String boardId, Integer id) {
        Board board = boardService.getBoardById(boardId);
        Task task = repository.findById(id).orElseThrow(() -> new ItemNotFoundException("Task " + id + " does not exist !!!"));
        if (!task.getBoard().equals(board)) {
            throw new BadRequestException("Task " + id + " is not belong to board " + board.getId() + " !!!");
        }
        return task;
    }

}

