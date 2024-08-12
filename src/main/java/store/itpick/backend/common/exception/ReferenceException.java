package store.itpick.backend.common.exception;

import lombok.Getter;
import store.itpick.backend.common.response.status.ResponseStatus;

@Getter
public class ReferenceException extends RuntimeException{
    private final ResponseStatus exceptionStatus;

    public ReferenceException(ResponseStatus exceptionStatus) {
        super(exceptionStatus.getMessage());
        this.exceptionStatus = exceptionStatus;
    }

}
