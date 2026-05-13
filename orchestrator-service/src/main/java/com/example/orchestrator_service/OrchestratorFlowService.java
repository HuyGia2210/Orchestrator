package com.example.orchestrator_service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Service
class OrchestratorFlowService {

	private final RestClient restClient;
	private final String userServiceUrl;
	private final String tourServiceUrl;
	private final String bookingServiceUrl;
	private final String paymentServiceUrl;

	OrchestratorFlowService(
	    @Value("${app.user-service-url}") String userServiceUrl,
	    @Value("${app.tour-service-url}") String tourServiceUrl,
	    @Value("${app.booking-service-url}") String bookingServiceUrl,
	    @Value("${app.payment-service-url}") String paymentServiceUrl) {
		this.restClient = RestClient.create();
		this.userServiceUrl = userServiceUrl;
		this.tourServiceUrl = tourServiceUrl;
		this.bookingServiceUrl = bookingServiceUrl;
		this.paymentServiceUrl = paymentServiceUrl;
	}

	AuthResponse register(RegisterRequest request) {
		return restClient.post()
		    .uri(userServiceUrl + "/register")
		    .contentType(MediaType.APPLICATION_JSON)
		    .body(request)
		    .retrieve()
		    .body(AuthResponse.class);
	}

	AuthResponse login(LoginRequest request) {
		return restClient.post()
		    .uri(userServiceUrl + "/login")
		    .contentType(MediaType.APPLICATION_JSON)
		    .body(request)
		    .retrieve()
		    .body(AuthResponse.class);
	}

	List<TourSummary> tours() {
		TourListResponse response = restClient.get()
		    .uri(tourServiceUrl + "/tours")
		    .retrieve()
		    .body(TourListResponse.class);
		return response == null ? List.of() : response.tours();
	}

	TourSummary tourById(Long tourId) {
		return restClient.get()
		    .uri(tourServiceUrl + "/tours/{id}", tourId)
		    .retrieve()
		    .body(TourSummary.class);
	}

	UserProfile userById(Long userId) {
		return restClient.get()
		    .uri(userServiceUrl + "/users/{id}", userId)
		    .retrieve()
		    .body(UserProfile.class);
	}

	BookTourResponse bookTour(BookTourRequest request) {
		UserProfile user = requireUser(request.userId());
		TourSummary tour = requireTour(request.tourId());

		BookingCommand bookingCommand = new BookingCommand(
		    user.id(),
		    user.fullName(),
		    tour.id(),
		    tour.name(),
		    request.travelDate(),
		    request.guests(),
		    tour.price().multiply(BigDecimal.valueOf(Math.max(1, request.guests()))));

		BookingResponse booking = restClient.post()
		    .uri(bookingServiceUrl + "/bookings")
		    .contentType(MediaType.APPLICATION_JSON)
		    .body(bookingCommand)
		    .retrieve()
		    .body(BookingResponse.class);

		if (booking == null || !booking.success()) {
			return new BookTourResponse(
			    false,
			    "BOOKING_FAILED",
			    booking == null ? "Booking service returned no response" : booking.message(),
			    user,
			    tour,
			    booking,
			    null,
			    false,
			    java.time.LocalDateTime.now());
		}

		PaymentCommand paymentCommand = new PaymentCommand(
		    booking.bookingId(),
		    booking.bookingCode(),
		    user.id(),
		    tour.id(),
		    booking.amount(),
		    request.paymentMethod(),
		    user.fullName());

		PaymentResponse payment = restClient.post()
		    .uri(paymentServiceUrl + "/payments")
		    .contentType(MediaType.APPLICATION_JSON)
		    .body(paymentCommand)
		    .retrieve()
		    .body(PaymentResponse.class);

		if (payment == null || !payment.success()) {
			boolean rollbackPerformed = rollbackBooking(booking.bookingId());
			return new BookTourResponse(
			    false,
			    "PAYMENT_FAILED",
			    payment == null ? "Payment service returned no response" : payment.message(),
			    user,
			    tour,
			    booking,
			    payment,
			    rollbackPerformed,
			    java.time.LocalDateTime.now());
		}

		return new BookTourResponse(
		    true,
		    "BOOKING_CONFIRMED",
		    "Booking confirmed and payment completed",
		    user,
		    tour,
		    booking,
		    payment,
		    false,
		    java.time.LocalDateTime.now());
	}

	private UserProfile requireUser(Long userId) {
		try {
			return userById(userId);
		} catch (RestClientResponseException exception) {
			throw new IllegalArgumentException("User not found: " + userId, exception);
		}
	}

	private TourSummary requireTour(Long tourId) {
		try {
			return tourById(tourId);
		} catch (RestClientResponseException exception) {
			throw new IllegalArgumentException("Tour not found: " + tourId, exception);
		}
	}

	private boolean rollbackBooking(Long bookingId) {
		try {
			CancelBookingResponse response = restClient.post()
			    .uri(bookingServiceUrl + "/bookings/cancel")
			    .contentType(MediaType.APPLICATION_JSON)
			    .body(new CancelBookingRequest(bookingId, "Payment failed"))
			    .retrieve()
			    .body(CancelBookingResponse.class);
			return response != null && response.success();
		} catch (RestClientResponseException exception) {
			return false;
		}
	}
}