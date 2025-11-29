package br.gov.md.parla_md_backend.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/api/public/test")
    public String publicTest() {
        return "Endpoint público";
    }

    @GetMapping("/api/test")
    public String securedTest() {
        return "Endpoint seguro";
    }
}


