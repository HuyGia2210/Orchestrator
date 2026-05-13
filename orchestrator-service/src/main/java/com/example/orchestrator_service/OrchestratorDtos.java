package com.example.orchestrator_service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

record LoginRequest(String username, String password) {
}

record RegisterRequest(String username, String password, String fullName, String email) {
}

record UserProfile(Long id, String username, String fullName, String email) {
}

record AuthResponse(boolean success, String message, UserProfile user) {
}

record TourSummary(Long id, String name, String destination, String description, BigDecimal price, int slots) {
}

record TourListResponse(List<TourSummary> tours) {
}

record BookingCommand(
    Long userId,
    String userName,
    Long tourId,
    String tourName,
    LocalDate travelDate,
    int guests,
    BigDecimal amount) {
}

record BookingResponse(
    boolean success,
    Long bookingId,
    String bookingCode,
    String status,
    String message,
    Long userId,
    Long tourId,
    String tourName,
    BigDecimal amount) {
}

record PaymentCommand(
    Long bookingId,
    String bookingCode,
    Long userId,
    Long tourId,
    BigDecimal amount,
    String paymentMethod,
    String payerName) {
}

record PaymentResponse(
    boolean success,
    Long paymentId,
    String paymentCode,
    String status,
    String message,
    Long bookingId,
    BigDecimal amount) {
}

record CancelBookingRequest(Long bookingId, String reason) {
}

record CancelBookingResponse(boolean success, Long bookingId, String status, String message) {
}

record BookTourRequest(Long userId, Long tourId, LocalDate travelDate, int guests, String paymentMethod) {
}

record BookTourResponse(
    boolean success,
    String status,
    String message,
    UserProfile user,
    TourSummary tour,
    BookingResponse booking,
    PaymentResponse payment,
    boolean rollbackPerformed,
    LocalDateTime timestamp) {
}