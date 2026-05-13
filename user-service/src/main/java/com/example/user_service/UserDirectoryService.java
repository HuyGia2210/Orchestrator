package com.example.user_service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

@Service
class UserDirectoryService {

	private final Map<Long, UserProfile> users = new ConcurrentHashMap<>();
	private final Map<String, String> passwords = new ConcurrentHashMap<>();
	private final AtomicLong sequence = new AtomicLong(3);

	UserDirectoryService() {
		seed(1L, "admin", "Admin Traveler", "admin@travel.local", "123456");
		seed(2L, "guest", "Guest Traveler", "guest@travel.local", "123456");
	}

	AuthResponse register(RegisterRequest request) {
		if (passwords.containsKey(request.username())) {
			return new AuthResponse(false, "Username already exists", findByUsername(request.username()).orElse(null));
		}

		long id = sequence.getAndIncrement();
		UserProfile user = new UserProfile(id, request.username(), request.fullName(), request.email());
		users.put(id, user);
		passwords.put(request.username(), request.password());
		return new AuthResponse(true, "Register success", user);
	}

	AuthResponse login(LoginRequest request) {
		Optional<UserProfile> user = findByUsername(request.username());
		if (user.isPresent() && request.password().equals(passwords.get(request.username()))) {
			return new AuthResponse(true, "Login success", user.get());
		}
		return new AuthResponse(false, "Invalid username or password", null);
	}

	UserProfile getUser(Long id) {
		UserProfile user = users.get(id);
		if (user == null) {
			throw new IllegalArgumentException("User not found: " + id);
		}
		return user;
	}

	private Optional<UserProfile> findByUsername(String username) {
		return users.values().stream()
		    .filter(user -> user.username().equalsIgnoreCase(username))
		    .findFirst();
	}

	private void seed(Long id, String username, String fullName, String email, String password) {
		users.put(id, new UserProfile(id, username, fullName, email));
		passwords.put(username, password);
	}
}