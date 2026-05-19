package com.platform.lbchildren.controller;

import com.platform.lbchildren.dto.AddChildRequest;
import com.platform.lbchildren.dto.LoginRequest;
import com.platform.lbchildren.dto.RegisterRequest;
import com.platform.lbchildren.entity.Child;
import com.platform.lbchildren.security.UserPrincipal;
import com.platform.lbchildren.service.ParentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/parent")
public class ParentController {

    @Autowired
    private ParentService parentService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            String result = parentService.register(request);
            return ResponseEntity.ok(Map.of("message", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            String token = parentService.login(request.getUsername(), request.getPassword());
            return ResponseEntity.ok(Map.of("token", token, "role", "PARENT"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/add-child")
    public ResponseEntity<?> addChild(@AuthenticationPrincipal UserPrincipal principal,
                                      @RequestBody AddChildRequest request) {
        try {
            String result = parentService.addChild(principal, request);
            return ResponseEntity.ok(Map.of("message", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/my-children")
    public ResponseEntity<?> getMyChildren(@AuthenticationPrincipal UserPrincipal principal) {
        try {
            List<Child> children = parentService.getMyChildren(principal.getUserId());
            return ResponseEntity.ok(children);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}