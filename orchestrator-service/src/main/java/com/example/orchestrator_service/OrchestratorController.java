package com.example.orchestrator_service;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*")
@RestController
class OrchestratorController {

	private final OrchestratorFlowService orchestratorFlowService;

	OrchestratorController(OrchestratorFlowService orchestratorFlowService) {
		this.orchestratorFlowService = orchestratorFlowService;
	}

	@PostMapping("/register")
	ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
		return ResponseEntity.ok(orchestratorFlowService.register(request));
	}

	@PostMapping("/login")
	ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
		return ResponseEntity.ok(orchestratorFlowService.login(request));
	}

	@GetMapping("/tours")
	ResponseEntity<List<TourSummary>> tours() {
		return ResponseEntity.ok(orchestratorFlowService.tours());
	}

	@GetMapping("/tours/{id}")
	ResponseEntity<TourSummary> tourById(@PathVariable Long id) {
		return ResponseEntity.ok(orchestratorFlowService.tourById(id));
	}

	@PostMapping("/book-tour")
	ResponseEntity<BookTourResponse> bookTour(@RequestBody BookTourRequest request) {
		try {
			return ResponseEntity.ok(orchestratorFlowService.bookTour(request));
		} catch (IllegalArgumentException exception) {
			return ResponseEntity.badRequest().body(
			    new BookTourResponse(false, "VALIDATION_FAILED", exception.getMessage(), null, null, null, null, false, java.time.LocalDateTime.now()));
		}
	}
}