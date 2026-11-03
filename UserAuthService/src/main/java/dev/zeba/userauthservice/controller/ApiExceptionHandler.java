package dev.zeba.userauthservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice//tell spring whenever exception occurs in ctrler come to this class and figure out method which is to be executed for exception
public class ApiExceptionHandler {
// for validation dependency
    //sb starter validation dependency : for dtos to do some validation; ex. @not blank,@Email on email ; @size(min=8) on password
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException e) {
        //problem detail sending status and detail
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,"Some fields are invalid");
        //printing errors too (custom map)
        Map<String,String> errors = new HashMap<>();
        //getBindingResult().getFieldErrors(): exactly what has gone wrong : invalid email/password !=8
        for(FieldError fieldError: e.getBindingResult().getFieldErrors()) {
             errors.put(fieldError.getField(),fieldError.getDefaultMessage());
        }
        problemDetail.setProperty("errors",errors);
        //return problemDetail : title, status code, details, custom fail in response
        return problemDetail;
        //if request is made with invalid token error should be 401(Unauthenticated) but it was returning 403(unauthorized): coz
        //security holder was set as empty and req was sent to filter 2, otherwise public APIs wouldn't have worked
        //solution to this! by default whenever auth filter throws error, exception handling filter in spring maps it to 403,
        //because it has come from auth filter even if it unauthenticated/undenied request
        //ideally exception handler should be told everything coming from auth filter is not 403, if its authentication issue 401 should be shown
        //hence once configuration addition exception handler! in security config

        //how to protect product catalog end points?
        //via api calls, user sends token in /products requests -> call authservice to verify if token is valid or not by using rest template
    }
}
