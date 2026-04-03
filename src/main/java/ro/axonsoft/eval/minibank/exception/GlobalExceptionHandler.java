package ro.axonsoft.eval.minibank.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ro.axonsoft.eval.minibank.dto.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException e){
        logger.error(e.getMessage(),e);
        return new ResponseEntity<>(new ErrorResponse("REJECTED", e.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleAlreadyExists(ResourceAlreadyExistsException e){
        logger.error(e.getMessage(),e);
        return new ResponseEntity<>(new ErrorResponse("REJECTED", e.getMessage()), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleArgumentNotValid(MethodArgumentNotValidException e){
        logger.error(e.getMessage(),e);
        String message = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("Invalid request");
        return new ResponseEntity<>(new ErrorResponse("REJECTED", message), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException e){
        logger.error(e.getMessage(),e);
        return new ResponseEntity<>(new ErrorResponse("REJECTED", e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BusinessConflictException.class)
    public ResponseEntity<ErrorResponse> handleBusinessConflict(BusinessConflictException e) {
        logger.error(e.getMessage(), e);
        return new ResponseEntity<>(new ErrorResponse("REJECTED", e.getMessage()), HttpStatus.CONFLICT);
    }
}
