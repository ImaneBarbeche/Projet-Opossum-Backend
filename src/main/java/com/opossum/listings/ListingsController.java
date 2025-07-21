package com.opossum.listings;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/listings")
public class ListingsController {

    private final ListingsService ListingsService;

    public ListingsController(ListingsService ListingsService) {
        this.ListingsService = ListingsService;
    }

    @PostMapping("/create")
    public ResponseEntity<Listings> create(@RequestBody Listings listings) {
        return ResponseEntity.ok(ListingsService.createListing(listings));
    }

    @PutMapping("{id}/update")
    public ResponseEntity<Listings> update(@PathVariable UUID id, @RequestBody Listings listings) {
        Optional<Listings> updated = ListingsService.updateListing(id, listings);
        return updated.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}/delete")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        ListingsService.deleteListing(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<Listings>> search(@RequestParam String title) {
        return ResponseEntity.ok(ListingsService.searchListings(title));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Listings> getListingById(@PathVariable UUID id) {
        return ListingsService.getListingById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/me")
    public ResponseEntity<List<Listings>> getListingsByUser(@RequestParam UUID userId) {
        List<Listings> listings = ListingsService.getListingsByUser(userId);
        return ResponseEntity.ok(listings);
    }

}