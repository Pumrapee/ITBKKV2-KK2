package sit.int221.kanbanapi.services;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import sit.int221.kanbanapi.entities.Task;
import sit.int221.kanbanapi.exceptions.BadRequestException;
import sit.int221.kanbanapi.exceptions.ItemNotFoundException;
import sit.int221.kanbanapi.repositories.TaskRepository;

import java.util.List;

@Service
public class TaskService {
    @Autowired
    private TaskRepository repository;

//    public List<Task> getAllTask() {
//        return repository.findAll();
//    }

//    public List<Task> getAllTaskSorted(String sortBy) {
//        try {
//            Sort sort = Sort.by(Sort.Order.asc(sortBy));
//            return repository.findAll(sort);
//        } catch (PropertyReferenceException e) {
//            throw new IllegalArgumentException("Invalid sortBy parameter: " + sortBy);
//        }
//    }
    public List<Task> getAllTaskFilteredSorted(List<String> filterStatuses, String sortBy) {
        Sort sort = Sort.by(Sort.Order.asc(sortBy != null ? sortBy : "id"));
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
    public Task createTask(Task task) { return repository.save(task); }

    @Transactional
    public Task removeTask(Integer id) {
        Task task = repository.findById(id).orElseThrow(() -> new ItemNotFoundException("NOT FOUND"));
        repository.delete(task);
        return task;
    }

    @Transactional
    public Task updateTask(Integer id, Task task) {
        Task existingTask = repository.findById(id).orElseThrow(() -> new ItemNotFoundException("NOT FOUND"));
        existingTask.setTitle(task.getTitle());
        existingTask.setDescription(task.getDescription());
        existingTask.setAssignees(task.getAssignees());
        existingTask.setStatus(task.getStatus());
        return repository.save(existingTask);
    }

    public void transferTaskStatus(Integer id, Integer newId) {
        try {
            repository.transferTaskStatus(id, newId);
        } catch (Exception ex) {
            throw new ItemNotFoundException("NOT FOUND");
        }
    }

    public boolean findTaskStatus(Integer id) {
        return (repository.existsByStatus(id) != 0);
    }
}
