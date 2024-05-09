package sit.int221.kanbanapi.services;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import sit.int221.kanbanapi.entities.Status;
import sit.int221.kanbanapi.exceptions.ItemNotFoundException;
import sit.int221.kanbanapi.repositories.StatusRepository;

import java.util.List;

@Service
public class StatusService {
    @Autowired
    private StatusRepository repository;

    public List<Status> getStatus() {
        return repository.findAll();
    }

    public Status getStatusById(Integer id) {
        return repository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Status id " + id + " does not exist !!!"));
    }

    @Transactional
    public Status createStatus(Status status) {
        try {
            return repository.save(status);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    @Transactional
    public Status removeStatus(Integer id) {
        Status status = repository.findById(id).orElseThrow(() -> new ItemNotFoundException("NOT FOUND"));
        repository.delete(status);
        return status;
    }

    @Transactional
    public Status updateStatus(Integer id, Status status) {
        Status existingStatus = repository.findById(id).orElseThrow(() -> new ItemNotFoundException("NOT FOUND"));
        existingStatus.setName(status.getName());
        existingStatus.setDescription(status.getDescription());
        return repository.save(existingStatus);
    }

    public Status getStatusByName(String statusName) {
        try {
            if (statusName == null) {
                return repository.findByName("No Status");
            }
            return repository.findByName(statusName);
        } catch (Exception ex) {
            throw new ItemNotFoundException("NOT FOUND");
        }
    }
}
