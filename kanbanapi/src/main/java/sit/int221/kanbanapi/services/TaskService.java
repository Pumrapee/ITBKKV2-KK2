package sit.int221.kanbanapi.services;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.stereotype.Service;
import sit.int221.kanbanapi.configs.StatusConfiguration;
import sit.int221.kanbanapi.entities.Status;
import sit.int221.kanbanapi.entities.Task;
import sit.int221.kanbanapi.exceptions.BadRequestException;
import sit.int221.kanbanapi.exceptions.ItemNotFoundException;
import sit.int221.kanbanapi.exceptions.TaskLimitExceededException;
import sit.int221.kanbanapi.repositories.StatusRepository;
import sit.int221.kanbanapi.repositories.TaskRepository;

import java.util.List;

@Service
public class TaskService {
    @Autowired
    private TaskRepository repository;
    @Autowired
    private StatusRepository statusRepository;
    @Autowired
    private StatusConfiguration configuration;

    public List<Task> getAllTask() {
        return repository.findAll();
    }

    public List<Task> getAllTaskFilteredSorted(List<String> filterStatuses, String sortBy) {
        Sort sort = Sort.by(Sort.Order.asc(sortBy != null ? sortBy : "createdOn"));
        try {
            if (filterStatuses != null && !filterStatuses.isEmpty()) {
                return repository.findByStatusNamesSorted(filterStatuses, sort);
            } else {
                return repository.findAll(sort);
            }
        } catch (PropertyReferenceException e) {
            throw new BadRequestException("Invalid sortBy parameter: " + sortBy);
        }
    }


    public Task getTaskById(Integer id) {
        return repository.findById(id).orElseThrow(
                () -> new ItemNotFoundException("Task id " + id + " does not exist !!!"));
    }

    @Transactional
    public Task createTask(Task task) {
        if (configuration.getNonLimitedUpdatableDeletableStatuses().contains(task.getStatus().getName())
                || statusLimitCheck(countTasksByStatus(task.getStatus().getId()) + 1)) {
            return repository.save(task);
        } else {
            throw new TaskLimitExceededException("Task limit exceeded!!!");
        }
    }

    @Transactional
    public Task removeTask(Integer id) {
        Task task = repository.findById(id).orElseThrow(() -> new ItemNotFoundException("NOT FOUND"));
        repository.delete(task);
        return task;
    }

    @Transactional
    public Task updateTask(Integer id, Task task) {
        if (configuration.getNonLimitedUpdatableDeletableStatuses().contains(task.getStatus().getName()) || statusLimitCheck(countTasksByStatus(task.getStatus().getId() + 1))) {
            Task existingTask = repository.findById(id).orElseThrow(() -> new ItemNotFoundException("NOT FOUND"));
            existingTask.setTitle(task.getTitle());
            existingTask.setDescription(task.getDescription());
            existingTask.setAssignees(task.getAssignees());
            existingTask.setStatus(task.getStatus());
            return repository.save(existingTask);
        } else {
            throw new TaskLimitExceededException("Task limit exceeded!!!");
        }
    }

    @Transactional
    public void transferTaskStatus(Integer id, Integer newId) {
        Status oldStatus = statusRepository.findById(id).orElseThrow(() -> new ItemNotFoundException("NOT FOUND"));
        Status newStatus = statusRepository.findById(newId).orElseThrow(() -> new ItemNotFoundException("NOT FOUND"));
        if (configuration.getNonLimitedUpdatableDeletableStatuses().contains(oldStatus.getName())){
            throw new BadRequestException("Bad Request");
        }
        if (!configuration.getNonLimitedUpdatableDeletableStatuses().contains(oldStatus.getName())
                && configuration.getNonLimitedUpdatableDeletableStatuses().contains(newStatus.getName())
                || statusLimitCheck(countTasksByStatus(id) + countTasksByStatus(newId))){
            try {
                repository.transferTaskStatus(id, newId);
            } catch (Exception ex) {
                throw new ItemNotFoundException("NOT FOUND");
            }
        } else {
            throw new TaskLimitExceededException("Task limit exceeded!!!");
        }
    }

    public boolean findTaskStatus(Integer id) {
        return (repository.countTasksByStatus(id) != 0);
    }


    public Integer countTasksByStatus(Integer id) { return repository.countTasksByStatus(id); }
    public Boolean statusLimitCheck(Integer count) {
        if (configuration.getTaskLimitEnabled()) {
            return count <= configuration.getMaxTasksPerStatus();
        } else {
            return true;
        }
    }

    public boolean allStatusLimitCheck(StatusConfiguration newConfig) {
        if (newConfig.getTaskLimitEnabled()) {
            return repository.countTasksByStatus().stream().allMatch(tasks -> tasks <= newConfig.getMaxTasksPerStatus());
        } else {
            return true;
        }
    }
}
