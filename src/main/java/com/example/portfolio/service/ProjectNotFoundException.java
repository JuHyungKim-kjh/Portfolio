package com.example.portfolio.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ProjectNotFoundException extends RuntimeException {

    public ProjectNotFoundException(String slug) {
        super("프로젝트를 찾을 수 없습니다: " + slug);
    }
}
