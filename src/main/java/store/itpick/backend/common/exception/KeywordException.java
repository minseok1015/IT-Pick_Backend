package store.itpick.backend.common.exception;


import lombok.Getter;
import store.itpick.backend.common.response.status.ResponseStatus;

@Getter
public class KeywordException extends RuntimeException{
    private final ResponseStatus exceptionStatus;

    public KeywordException(ResponseStatus exceptionStatus) {
        super(exceptionStatus.getMessage());
        this.exceptionStatus = exceptionStatus;
    }

}