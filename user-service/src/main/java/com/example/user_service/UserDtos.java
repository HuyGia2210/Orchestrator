package com.example.user_service;

record LoginRequest(String username, String password) {
}

record RegisterRequest(String username, String password, String fullName, String email) {
}

record UserProfile(Long id, String username, String fullName, String email) {
}

record AuthResponse(boolean success, String message, UserProfile user) {
}