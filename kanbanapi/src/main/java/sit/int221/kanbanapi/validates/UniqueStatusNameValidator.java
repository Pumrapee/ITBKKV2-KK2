package sit.int221.kanbanapi.validates;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sit.int221.kanbanapi.repositories.StatusRepository;

@Component
public class UniqueStatusNameValidator implements ConstraintValidator<UniqueStatusName, String> {

    @Autowired
    private StatusRepository statusRepository;

    @Override
    public boolean isValid(String statusName, ConstraintValidatorContext context) {
        if (statusName == null || statusName.isBlank()) {
            return true; // Not the responsibility of this validator to check for null/blank
        }
        return !statusRepository.existsByName(statusName);
    }
}
