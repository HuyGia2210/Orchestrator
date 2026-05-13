package com.example.user_service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*")
@RestController
class UserController {

	private final UserDirectoryService userDirectoryService;

	UserController(UserDirectoryService userDirectoryService) {
		this.userDirectoryService = userDirectoryService;
	}

	@PostMapping("/register")
	ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
		return ResponseEntity.ok(userDirectoryService.register(request));
	}

	@PostMapping("/login")
	ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
		return ResponseEntity.ok(userDirectoryService.login(request));
	}

	@GetMapping("/users/{id}")
	ResponseEntity<UserProfile> userById(@PathVariable Long id) {
		try {
			return ResponseEntity.ok(userDirectoryService.getUser(id));
		} catch (IllegalArgumentException exception) {
			return ResponseEntity.notFound().build();
		}
	}
}