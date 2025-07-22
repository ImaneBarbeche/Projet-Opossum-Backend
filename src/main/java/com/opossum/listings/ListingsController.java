package com.opossum.listings;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.opossum.listings.dto.CreateListingsRequest;
import com.opossum.listings.dto.UpdateListingsRequest;
import com.opossum.user.User;
import com.opossum.common.enums.ListingStatus;
import com.opossum.user.dto.UserProfileResponse;
import com.opossum.user.UserService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.opossum.listings.Listings;
import com.opossum.listings.ListingsService;


@RestController
@RequestMapping("/api/v1/listings")

public class ListingsController {

    private final ListingsService listingsService;
    private final UserService userService;

    public ListingsController(ListingsService listingsService, UserService userService) {
        this.listingsService = listingsService;
        this.userService = userService;
    }

    @PostMapping("/create")
    public ResponseEntity<Listings> create(@RequestBody CreateListingsRequest createListingsRequest) {
        Listings created = listingsService.createListing(createListingsRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}/update")
    public ResponseEntity<Listings> update(@PathVariable UUID id, @RequestBody UpdateListingsRequest updateListingsRequest) {
        Optional<Listings> updated = listingsService.updateListing(id, updateListingsRequest);
        return updated.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}/delete")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        listingsService.deleteListing(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<List<Listings>> getMyListings(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<Listings> listings = listingsService.getListingsByUser(user.getId());
        return ResponseEntity.ok(listings);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Listings>> search(@RequestParam String title) {
        return ResponseEntity.ok(listingsService.searchListings(title));
    }

    @GetMapping("/all")
    public ResponseEntity<List<Listings>> getAllListings() {
        List<Listings> listings = listingsService.getAllListings();
        return ResponseEntity.ok(listings);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Listings> getListingById(@PathVariable UUID id) {
        return listingsService.getListingById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/listing/{listingId}/user")
    public ResponseEntity<UserProfileResponse> getUserByListingId(@PathVariable UUID listingId) {
        Optional<Listings> listingOpt = listingsService.getListingById(listingId);
        if (listingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        UUID userId = listingOpt.get().getUserId();
        return userService.getUserById(userId)
                .map(user -> ResponseEntity.ok(userService.mapToDto(user)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Listings>> getListingsByStatus(@PathVariable ListingStatus status) {
        List<Listings> listings = listingsService.getListingsByStatus(status);
        return ResponseEntity.ok(listings);
    }
}