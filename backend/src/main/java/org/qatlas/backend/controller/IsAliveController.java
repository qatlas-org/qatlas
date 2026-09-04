package org.qatlas.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static org.qatlas.backend.Constants.SLASH;

@RestController
@RequestMapping(SLASH + "isAlive")
@ResponseStatus(HttpStatus.OK)
public class IsAliveController {

    @GetMapping
    public String isStarted() {
        return "Ready";
    }
}
