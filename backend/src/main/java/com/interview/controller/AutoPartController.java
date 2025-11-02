package com.interview.controller;

import com.interview.dto.*;
import com.interview.service.AutoPartService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RateLimiter(name = "myApiRateLimiter", fallbackMethod = "rateLimiterFallback")
@RestController
@RequestMapping("/api/v1/parts")
public class AutoPartController {

    private final AutoPartService service;

    public AutoPartController(AutoPartService service) {
        this.service = service;
    }


    @PostMapping
    public ResponseEntity<AutoPartResponse> createPart(@Valid @RequestBody AutoPartRequest dto) {
        AutoPartResponse created = service.create(dto);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AutoPartResponse> getPartById(@PathVariable Long id) {
        AutoPartResponse resp = service.getById(id);
        return ResponseEntity.ok(resp);
    }

    @GetMapping
    public ResponseEntity<List<AutoPartResponse>> getAllParts() {
        List<AutoPartResponse> resp = service.getAll();
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/search")
    public ResponseEntity<List<AutoPartResponse>> getPartsByName(@RequestParam("name") String name) {
        List<AutoPartResponse> resp = service.getByName(name);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/paged")
    public ResponseEntity<PagedResponse<AutoPartResponse>> getPartsPaged(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        Page<AutoPartResponse> p = service.getAllPaged(page, size);
        PagedResponse<AutoPartResponse> response = new PagedResponse<>(
                p.getContent(),
                p.getNumber(),
                p.getSize(),
                p.getTotalElements(),
                p.getTotalPages()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/search")
    public ResponseEntity<List<AutoPartResponse>> findByExample(@RequestBody AutoPartRequest dto) {
        List<AutoPartResponse> resp = service.findByExample(dto);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/count")
    public ResponseEntity<Long> countParts() {
        return ResponseEntity.ok(service.count());
    }

    @PostMapping("/count")
    public ResponseEntity<Long> countByExample(@RequestBody AutoPartRequest dto) {
        return ResponseEntity.ok(service.countByExample(dto));
    }

    @GetMapping("/total-value-per-category")
    public ResponseEntity<List<AutoPartTotalValuePerCategoryResponse>> getTotalValuePerCategory() {
        List<AutoPartTotalValuePerCategoryResponse> resp = service.getTotalValuePerCategory();
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/query")
    public ResponseEntity<Map<String, Object>> queryAutoParts(@Valid @RequestBody AutoPartQueryRequest queryRequest) {
        return ResponseEntity.ok(service.query(queryRequest));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AutoPartResponse> updatePart(@PathVariable Long id, @Valid @RequestBody AutoPartRequest dto) {
        AutoPartResponse updated = service.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePart(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    public ResponseEntity<String> rateLimiterFallback(Throwable t) {
        t.printStackTrace(); // for debugging purposes
        return ResponseEntity.status(429).body("Too Many Requests: Rate limit exceeded.");
    }
}