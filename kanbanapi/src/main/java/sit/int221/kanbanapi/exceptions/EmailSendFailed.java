package sit.int221.kanbanapi.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class EmailSendFailed extends RuntimeException {
    public EmailSendFailed(String message) {
        super(message);
    }
}