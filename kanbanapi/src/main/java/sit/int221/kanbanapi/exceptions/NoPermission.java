package sit.int221.kanbanapi.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class NoPermission extends RuntimeException {
    public NoPermission(String message) {
        super(message);
    }
}
