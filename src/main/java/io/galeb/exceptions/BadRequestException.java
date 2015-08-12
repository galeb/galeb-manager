package io.galeb.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value=HttpStatus.BAD_REQUEST, reason="bad request")
public class BadRequestException extends RuntimeException {

    private static final long serialVersionUID = 6986223937559244217L;

    public BadRequestException() {
        super();
    }

    public BadRequestException(String message) {
        super(message);
    }
}
