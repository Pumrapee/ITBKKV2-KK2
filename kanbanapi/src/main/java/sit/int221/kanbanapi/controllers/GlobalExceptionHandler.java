package sit.int221.kanbanapi.controllers;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.context.request.WebRequest;
import sit.int221.kanbanapi.exceptions.ErrorResponse;
import sit.int221.kanbanapi.exceptions.ItemNotFoundException;
import sit.int221.kanbanapi.exceptions.TaskLimitExceededException;

@RestControllerAdvice
@CrossOrigin(origins = {"http://ip23kk2.sit.kmutt.ac.th","http://localhost:5173","http://intproj23.sit.kmutt.ac.th"})
public class GlobalExceptionHandler {


//    @ExceptionHandler(HandlerMethodValidationException.class)
//    public ResponseEntity<ErrorResponse> handleHandlerMethodValidationException(
//            HandlerMethodValidationException exception, WebRequest request) {
//        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST.value(),
//                "Validation error. Check 'errors' field for details.", request.getDescription(false));
//        List<ParameterValidationResult> paramNames = exception.getAllValidationResults();
//        for (ParameterValidationResult param : paramNames) {
//            errorResponse.addValidationError(param.getMethodParameter().getParameterName(),
//                    param.getResolvableErrors().get(0).getDefaultMessage()
//                            + " (" + param.getArgument().toString() + ")");
//        }
//        return ResponseEntity.unprocessableEntity().body(errorResponse);
//    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST.value(),
                "Validation error. Check 'errors' field for details.", request.getDescription(false));
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            errorResponse.addValidationError(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(ItemNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ErrorResponse> handleItemNotFoundException(RuntimeException exception, WebRequest request) {
        return buildErrorResponse(exception, HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler({DataAccessException.class, HttpMessageNotReadableException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleJpaException
            (Exception exception, WebRequest request) {
        return buildErrorResponse(exception, "BAD REQUEST", HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(TaskLimitExceededException.class)
    @ResponseStatus(HttpStatus.INSUFFICIENT_STORAGE)
    public ResponseEntity<ErrorResponse> handleTaskLimitExceededException
            (Exception exception, WebRequest request) {
        return buildErrorResponse(exception, HttpStatus.INSUFFICIENT_STORAGE, request);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorResponse> handleAllUncaughtException
            (Exception exception, WebRequest request) {
        return buildErrorResponse(exception, "Unknown error occurred", HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(
            Exception exception, HttpStatus httpStatus, WebRequest request) {
        return buildErrorResponse(exception, exception.getMessage(), httpStatus, request);
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(
            Exception exception, String message, HttpStatus httpStatus, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(httpStatus.value(), message, request.getDescription(false).substring(4));
        return ResponseEntity.status(httpStatus).body(errorResponse);
    }
}