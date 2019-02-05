package site.security;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Created by Repnox on 4/5/2018.
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class AccessException extends RuntimeException {
}
