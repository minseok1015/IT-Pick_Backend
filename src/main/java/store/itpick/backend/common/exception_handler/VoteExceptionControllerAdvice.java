package store.itpick.backend.common.exception_handler;

import jakarta.annotation.Priority;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import store.itpick.backend.common.exception.VoteException;
import store.itpick.backend.common.response.BaseErrorResponse;

@Slf4j
@Priority(0)
@RestControllerAdvice
public class VoteExceptionControllerAdvice {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(VoteException.class)
    public BaseErrorResponse handleDebateException(VoteException e) {
        log.error("[VoteException]", e);
        return new BaseErrorResponse(e.getExceptionStatus());
    }
}