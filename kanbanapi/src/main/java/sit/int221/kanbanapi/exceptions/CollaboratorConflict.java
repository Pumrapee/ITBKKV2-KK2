package sit.int221.kanbanapi.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class CollaboratorConflict extends RuntimeException{
    public CollaboratorConflict(String message) {
        super(message);
    }
}
