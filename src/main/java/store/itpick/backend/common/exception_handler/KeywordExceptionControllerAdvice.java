package store.itpick.backend.common.exception_handler;


import jakarta.annotation.Priority;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import store.itpick.backend.common.exception.KeywordException;
import store.itpick.backend.common.exception.ReferenceException;
import store.itpick.backend.common.response.BaseErrorResponse;

@Slf4j
@Priority(0)
@RestControllerAdvice
public class KeywordExceptionControllerAdvice {
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(KeywordException.class)
    public BaseErrorResponse handle_UserException(KeywordException e) {
        log.error("[handle_KeywordException]", e);
        return new BaseErrorResponse(e.getExceptionStatus(), e.getMessage());
    }
}
