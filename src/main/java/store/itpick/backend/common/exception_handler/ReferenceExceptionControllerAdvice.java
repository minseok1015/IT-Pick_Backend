package store.itpick.backend.common.exception_handler;

import jakarta.annotation.Priority;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import store.itpick.backend.common.exception.ReferenceException;
import store.itpick.backend.common.response.BaseErrorResponse;

import static store.itpick.backend.common.response.status.BaseExceptionResponseStatus.INVALID_REFERENCE;


@Slf4j
@Priority(0)
@RestControllerAdvice
public class ReferenceExceptionControllerAdvice {
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ReferenceException.class)
    public BaseErrorResponse handle_UserException(ReferenceException e) {
        log.error("[handle_UserException]", e);
        return new BaseErrorResponse(e.getExceptionStatus(), e.getMessage());
    }
}
