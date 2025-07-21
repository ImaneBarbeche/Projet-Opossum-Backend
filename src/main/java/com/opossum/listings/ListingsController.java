package com.opossum.listings;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.opossum.listings.dto.CreateListingsRequest;
import com.opossum.listings.dto.UpdateListingsRequest;
import com.opossum.user.User;

import com.opossum.user.dto.UserProfileResponse;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.opossum.user.UserService;

@RestController
@RequestMapping("/api/v1/listings")
public class ListingsController {

    private final ListingsService listingsService;
    private final UserService userService;

    public ListingsController(ListingsService listingsService, UserService userService) {
        this.listingsService = listingsService;
        this.userService = userService;
    }

    /**
     * Creates a new listing.
     *
     * @param createListingsRequest the listing details to be created
     *
     *
     */
    @PostMapping("/create")
    public ResponseEntity<Listings> create(@RequestBody CreateListingsRequest createListingsRequest) {
        return ResponseEntity.ok(listingsService.createListing(createListingsRequest));
    }

    @PutMapping("{id}/update")
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
    public ResponseEntity<List<Listings>> getAllListings(@RequestParam(required = false, defaultValue = "false") boolean isLost) {
        List<Listings> listings = listingsService.getAllListings();
        return ResponseEntity.ok(listings);

    }

    @GetMapping("/{id}")
    public ResponseEntity<Listings> getListingById(@PathVariable UUID id) {
        return listingsService.getListingById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Returns the UserProfileResponse associated with the listing with the
     * given listingId
     *
     * @param listingId the id of the listing
     * @return the ResponseEntity containing the UserProfileResponse
     */
    @GetMapping("/listing/{listingId}/user")
    public ResponseEntity<UserProfileResponse> getUserByListingId(@PathVariable UUID listingId) {
        Optional<Listings> listingOpt = listingsService.getListingById(listingId);

        if (listingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Listings listing = listingOpt.get();
        UUID userId = listing.getUserId();

        Optional<User> userOpt = userService.getUserById(userId);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            UserProfileResponse userProfileResponse = userService.mapToDto(user);
            return ResponseEntity.ok(userProfileResponse);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
