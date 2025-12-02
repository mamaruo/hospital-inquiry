package com.mamaruo.hospitalinquiry;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;



@RestController
public class HelloController {
    @GetMapping("/")
    public String hello(HttpServletRequest request) {
        return "Hello, Hospital Inquiry! Your session ID is " +
                request.getSession().getId();
    }

    @PostMapping("/p")
    public String pString(@RequestBody String entity) {
        return entity;
    }

    @GetMapping("/csrf")
    public CsrfToken getCsrf(HttpServletRequest request) {
        return (CsrfToken) request.getAttribute(CsrfToken.class.getName());
    }
}