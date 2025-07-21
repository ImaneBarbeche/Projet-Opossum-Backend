package com.opossum.listings;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.opossum.user.dto.UserDto;

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
     * *********** ✨ Windsurf Command ⭐ ************
     */
    /**
     * Creates a new listing.
     *
     * @param listings the listing details to be created
     *
     *
     */
    ******* 160d849d-ee35
    -4aef
    -b365
    -a4df08240a35

    ******
     */
    @PostMapping("/create")
    public ResponseEntity<Listings> create(@RequestBody Listings listings) {
        return ResponseEntity.ok(listingsService.createListing(listings));
    }

    @PutMapping("{id}/update")
    public ResponseEntity<Listings> update(@PathVariable UUID id, @RequestBody Listings listings) {
        Optional<Listings> updated = listingsService.updateListing(id, listings);
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
        List<Listings> listings = listingsService.getAllListings(isLost);
        return ResponseEntity.ok(listings);

    }

    @GetMapping("/{id}")
    public ResponseEntity<Listings> getListingById(@PathVariable UUID id) {
        return listingsService.getListingById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * *********** ✨ Windsurf Command ⭐  ************
     */
    /**
     * Returns the UserDto associated with the listing with the given listingId
     *
     * @param listingId the id of the listing
     * @return the ResponseEntity containing the UserDto
     */
    /**
     * ***** f7d3b7d4-1604-448d-8f9f-2b0ccd139e17  ******
     */
    @GetMapping("/listing/{listingId}/user")
    public ResponseEntity<UserDto> getUserByListingId(@PathVariable UUID listingId) {
        Optional<Listings> listingOpt = listingsService.getListingById(listingId);

        if (listingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Listings listing = listingOpt.get();
        UUID userId = listing.getUserId();

        // Use the injected UserService instance
        return userService.getUserById(userId)
                .map(user -> userService.mapToDto(user))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());

    }
}
