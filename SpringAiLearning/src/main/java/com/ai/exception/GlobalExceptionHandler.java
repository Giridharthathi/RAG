package com.ai.exception;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.rmi.ServerException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler({ServerException.class,
            HttpClientErrorException.class,
            HttpServerErrorException.class})
    public ResponseEntity<String> globalException(){
        return new ResponseEntity<>("Currently we are experience high demand, Please try after sometime",
                HttpStatus.BAD_GATEWAY);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> globalExceptin(Exception exception) {
        log.debug(exception.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_GATEWAY)
                .body("Currently we are experiencing high demand. Please try again later.");
    }


}
