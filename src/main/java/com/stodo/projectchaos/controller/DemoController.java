package com.stodo.projectchaos.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/demo")
public class DemoController {

    @GetMapping("/secured")
    public ResponseEntity<String> hello() {
        return ResponseEntity.ok("Hello from secure endpoint");
    }

    @GetMapping("/permitted")
    public ResponseEntity<String> helloPermitted() {
        return ResponseEntity.ok("Hello from non-secured endpoint");
    }
}