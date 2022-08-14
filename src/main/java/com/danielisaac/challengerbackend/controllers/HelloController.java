package com.danielisaac.challengerbackend.controllers;

import com.danielisaac.challengerbackend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @Autowired
    UserRepository userRepository;

    @RequestMapping("/hello")
    public String firstPage(){
        return userRepository.findAll().size() + " is the size of the user list";
    }
}
