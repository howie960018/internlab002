package com.ctbc.assignment2.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateCourseNameException extends RuntimeException {
    public DuplicateCourseNameException(String message) {
        super(message);
    }
}
