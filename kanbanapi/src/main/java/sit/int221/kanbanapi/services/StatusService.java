package sit.int221.kanbanapi.services;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import sit.int221.kanbanapi.configs.StatusConfiguration;
import sit.int221.kanbanapi.entities.Status;
import sit.int221.kanbanapi.exceptions.BadRequestException;
import sit.int221.kanbanapi.exceptions.ItemNotFoundException;
import sit.int221.kanbanapi.repositories.StatusRepository;

import java.util.List;

@Service
public class StatusService {
    @Autowired
    private StatusRepository repository;

    @Autowired
    private StatusConfiguration configuration;

    public List<Status> getStatus() {
        return repository.findAll();
    }

    public Status getStatusById(Integer id) {
        return repository.findById(id).orElseThrow(
                () -> new ItemNotFoundException("Status id " + id + " does not exist !!!"));
    }

    @Transactional
    public Status createStatus(Status status) {
        return repository.save(status);
    }

    @Transactional
    public Status removeStatus(Integer id) {
        Status status = repository.findById(id).orElseThrow(() -> new ItemNotFoundException("NOT FOUND"));
        if (configuration.getNonLimitedUpdatableDeletableStatuses().contains(status.getName())) {
            throw new BadRequestException("Deletion of record with No Status is not allowed.");
        } else {
            repository.delete(status);
            return status;
        }
    }

    @Transactional
    public Status updateStatus(Integer id, Status status) {
        Status existingStatus = repository.findById(id).orElseThrow(() -> new ItemNotFoundException("NOT FOUND"));
        if (configuration.getNonLimitedUpdatableDeletableStatuses().contains(status.getName())) {
            throw new BadRequestException("Updation of record with No Status is not allowed.");
        } else {
            existingStatus.setName(status.getName());
            existingStatus.setDescription(status.getDescription());
            existingStatus.setColor(status.getColor());
            return repository.save(existingStatus);
        }
    }

    public Status getStatusByName(String statusName) {
        try {
            Integer statusId = Integer.parseInt(statusName);
            return repository.findById(statusId).orElseThrow(() -> new BadRequestException("BAD REQUEST"));
        } catch (NumberFormatException e) {
            if (statusName == null || statusName.isBlank()) {
                return repository.findByName("No Status").orElseThrow(() -> new BadRequestException("BAD REQUEST"));
            }
            return repository.findByName(statusName).orElseThrow(() -> new BadRequestException("BAD REQUEST"));
        }
    }
}
