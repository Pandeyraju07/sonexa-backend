package com.sonexa.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SonexaApplication {

    public static void main(String[] args) {
        SpringApplication.run(SonexaApplication.class, args);
        System.out.println("=================================================");
        System.out.println("Sonexa Spring Boot REST API Server Running on http://localhost:8080");
        System.out.println("=================================================");
    }
}
