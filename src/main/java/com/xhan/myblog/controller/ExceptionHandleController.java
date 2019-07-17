package com.xhan.myblog.controller;

import com.xhan.myblog.exceptions.TooManyVisitorException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class ExceptionHandleController {

    @ExceptionHandler(value = TooManyVisitorException.class)
    public ModelAndView rejectVisit() {
        ModelAndView mav = new ModelAndView("error");
        mav.setStatus(HttpStatus.valueOf(429));
        mav.addObject("error", "访客过多，请稍后再试");
        return mav;
    }

}
