package store.itpick.backend.common.exception.jwt.unauthorized;

import store.itpick.backend.common.response.status.ResponseStatus;
import lombok.Getter;

@Getter
public class JwtInvalidTokenException extends JwtUnauthorizedTokenException {

    private final ResponseStatus exceptionStatus;

    public JwtInvalidTokenException(ResponseStatus exceptionStatus) {
        super(exceptionStatus);
        this.exceptionStatus = exceptionStatus;
    }
}