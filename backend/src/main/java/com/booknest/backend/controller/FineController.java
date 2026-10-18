package com.booknest.backend.controller;

import com.booknest.backend.dto.FinePaymentRequest;
import com.booknest.backend.dto.FinesSummaryDTO;
import com.booknest.backend.model.Fine;
import com.booknest.backend.service.FineService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/fines")
public class FineController {

    private final FineService fineService;

    public FineController(FineService fineService) {
        this.fineService = fineService;
    }

    @GetMapping
    public ResponseEntity<List<Fine>> getAllFines() {
        return ResponseEntity.ok(fineService.getAllFines());
    }

    @GetMapping("/summary")
    public ResponseEntity<FinesSummaryDTO> getFinesSummary() {
        return ResponseEntity.ok(fineService.getFinesSummary());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Fine> getFineById(@PathVariable int id) {
        return fineService.getFineById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<Fine> payFine(@PathVariable int id,
                                        @RequestBody(required = false) FinePaymentRequest request) {
        String note = (request != null) ? request.getPaymentNote() : null;
        Fine settled = fineService.payFine(id, note);
        return ResponseEntity.ok(settled);
    }
}
