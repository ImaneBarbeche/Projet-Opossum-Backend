package com.opossum.listings;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/listings")
public class listingsController {

    private final listingsService listingsService;

    public listingsController(listingsService listingsService) {
        this.listingsService = listingsService;
    }

    @PostMapping
    public ResponseEntity<listings> create(@RequestBody listings listings) {
        return ResponseEntity.ok(listingsService.createListing(listings));
    }


    

    @PutMapping("/{id}")
    public ResponseEntity<listings> update(@PathVariable UUID id, @RequestBody listings listings) {
        Optional<listings> updated = listingsService.updateListing(id, listings);
        return updated.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        listingsService.deleteListing(id);
        return ResponseEntity.noContent().build();
    }

     @GetMapping("/search")
    public ResponseEntity<List<listings>> search(@RequestParam String title) {
        return ResponseEntity.ok(listingsService.searchListings(title));
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<listings>> getUserlistings(@PathVariable UUID userId) {
        return ResponseEntity.ok(listingsService.getListingsByUser(userId));
    }
   
}
