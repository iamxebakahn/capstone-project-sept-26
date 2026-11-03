package dev.zeba.userauthservice.controller;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice//tell spring whenever exception occurs in ctrler come to this class and figure out method which is to be executed for exception
public class ApiExceptionHandler {
// for validation dependency
    //sb starter validation dependency : for dtos to do some validation; ex. @not blank,@Email on email ; @size(min=8) on password
    @ExceptionHandler(value = MethodArgumentNotValidException.class)


    //return problemDetail : title, status code, details, custom fail in response
}
