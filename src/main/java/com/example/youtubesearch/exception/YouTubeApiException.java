package com.example.youtubesearch.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_GATEWAY)
public class YouTubeApiException extends RuntimeException {
    public YouTubeApiException(String message) {
        super(message);
    }
}