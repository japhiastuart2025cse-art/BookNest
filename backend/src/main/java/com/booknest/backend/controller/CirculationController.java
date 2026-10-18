package com.booknest.backend.controller;

import com.booknest.backend.dto.IssueRequest;
import com.booknest.backend.dto.ReturnRequest;
import com.booknest.backend.model.CirculationRecord;
import com.booknest.backend.service.CirculationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/circulation")
public class CirculationController {

    private final CirculationService circulationService;

    public CirculationController(CirculationService circulationService) {
        this.circulationService = circulationService;
    }

    @PostMapping("/issue")
    public ResponseEntity<CirculationRecord> issueBook(@RequestBody IssueRequest request) {
        CirculationRecord record = circulationService.issueBook(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(record);
    }

    @PostMapping("/{id}/return")
    public ResponseEntity<CirculationRecord> returnBook(@PathVariable int id,
                                                        @RequestBody(required = false) ReturnRequest request) {
        LocalDate returnDate = (request != null && request.getReturnDate() != null)
                ? request.getReturnDate()
                : LocalDate.now();
        CirculationRecord updated = circulationService.returnBook(id, returnDate);
        return ResponseEntity.ok(updated);
    }

    @GetMapping
    public ResponseEntity<List<CirculationRecord>> getAllCirculation() {
        return ResponseEntity.ok(circulationService.getAllCirculation());
    }

    @GetMapping("/active")
    public ResponseEntity<List<CirculationRecord>> getActiveLoans() {
        return ResponseEntity.ok(circulationService.getActiveLoans());
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<CirculationRecord>> getOverdueLoans() {
        return ResponseEntity.ok(circulationService.getOverdueLoans());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CirculationRecord> getCirculationById(@PathVariable int id) {
        return circulationService.getCirculationById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
}
