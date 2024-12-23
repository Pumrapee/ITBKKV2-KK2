package sit.int221.kanbanapi.controllers;

import io.jsonwebtoken.security.SignatureException;
import jakarta.mail.AuthenticationFailedException;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import sit.int221.kanbanapi.exceptions.*;

import java.util.List;

@RestControllerAdvice
@CrossOrigin(origins = {"http://ip23kk2.sit.kmutt.ac.th", "http://localhost:5173", "http://intproj23.sit.kmutt.ac.th", "https://intproj23.sit.kmutt.ac.th"})
public class GlobalExceptionHandler {

    @ExceptionHandler({
            MethodArgumentTypeMismatchException.class,
            MethodArgumentNotValidException.class,
            HandlerMethodValidationException.class,
            MissingServletRequestParameterException.class
    })
    public ResponseEntity<ErrorResponse> handleValidationExceptions(Exception exception, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Validation error. Check 'errors' field for details.",
                request.getDescription(false)
        );

        if (exception instanceof MethodArgumentTypeMismatchException) {
            MethodArgumentTypeMismatchException ex = (MethodArgumentTypeMismatchException) exception;
            errorResponse.addValidationError(ex.getName(), ex.getValue() + " is not a valid " + ex.getParameter().getParameterType() +" value");
        } else if (exception instanceof MethodArgumentNotValidException) {
            MethodArgumentNotValidException ex = (MethodArgumentNotValidException) exception;
            for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
                errorResponse.addValidationError(fieldError.getField(), fieldError.getDefaultMessage());
            }
        } else if (exception instanceof HandlerMethodValidationException) {
            HandlerMethodValidationException ex = (HandlerMethodValidationException) exception;
            List<ParameterValidationResult> paramNames = ex.getAllValidationResults();
            for (ParameterValidationResult param : paramNames) {
                errorResponse.addValidationError(
                        param.getMethodParameter().getParameterName(),
                        param.getResolvableErrors().get(0).getDefaultMessage()
                                + " (" + param.getArgument().toString() + ")"
                );
            }
        } else if (exception instanceof MissingServletRequestParameterException) {
            MissingServletRequestParameterException ex = (MissingServletRequestParameterException) exception;
            errorResponse.addValidationError(ex.getParameterName(), "Missing required parameter of type " + ex.getParameterType());
        }

        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(ItemNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ErrorResponse> handleItemNotFoundException(RuntimeException exception, WebRequest request) {
        return buildErrorResponse(exception, HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler({DataAccessException.class, BadRequestException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleJpaException
            (Exception exception, WebRequest request) {
        return buildErrorResponse(exception, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleRequiredRequestBody
            (Exception exception, WebRequest request) {
        return buildErrorResponse(exception, "Required request body is missing", HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(TaskLimitExceededException.class)
    @ResponseStatus(HttpStatus.INSUFFICIENT_STORAGE)
    public ResponseEntity<ErrorResponse> handleTaskLimitExceededException
            (Exception exception, WebRequest request) {
        return buildErrorResponse(exception, HttpStatus.INSUFFICIENT_STORAGE, request);
    }

    @ExceptionHandler({BadCredentialsException.class, UsernameNotFoundException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<ErrorResponse> handleAuthenticaionException
            (Exception exception, WebRequest request) {
        return buildErrorResponse(exception, "Username or Password is incorrect.", HttpStatus.UNAUTHORIZED, request);
    }

    @ExceptionHandler({AuthenticationFailed.class, SignatureException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<ErrorResponse> handleTokenException
            (Exception exception, WebRequest request) {
        return buildErrorResponse(exception, HttpStatus.UNAUTHORIZED, request);
    }

    @ExceptionHandler(NoPermission.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<ErrorResponse> handleNoPermissionException
            (Exception exception, WebRequest request) {
        return buildErrorResponse(exception, HttpStatus.FORBIDDEN, request);
    }

    @ExceptionHandler({NoResourceFoundException.class, HttpRequestMethodNotSupportedException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ErrorResponse> handleNoResourceFoundExceptionException
            (Exception exception, WebRequest request) {
        return buildErrorResponse(exception, HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(CollaboratorConflict.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ErrorResponse> handleCollaboratorConflict
            (Exception exception, WebRequest request) {
        return buildErrorResponse(exception, HttpStatus.CONFLICT, request);
    }

    @ExceptionHandler({EmailSendFailed.class, AuthenticationFailedException.class, MailAuthenticationException.class})
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ResponseEntity<ErrorResponse> handleEmailException
            (Exception exception, WebRequest request) {
        return buildErrorResponse(exception, "Email sending service unavailable", HttpStatus.SERVICE_UNAVAILABLE, request);
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