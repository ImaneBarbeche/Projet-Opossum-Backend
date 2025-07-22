package com.opossum.listings;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.opossum.listings.dto.CreateListingsRequest;
import com.opossum.listings.dto.UpdateListingsRequest;
import com.opossum.common.enums.ListingStatus;

@Service
public class ListingsService {

    private final ListingsRepository listingsRepository;

    public ListingsService(ListingsRepository listingsRepository) {
        this.listingsRepository = listingsRepository;
    }

    @Transactional
    public Listings createListing(CreateListingsRequest createListingsRequest) {
        Listings listing = new Listings();
        listing.setTitle(createListingsRequest.getTitle());
        listing.setDescription(createListingsRequest.getDescription());
        listing.setType(Listings.ListingType.valueOf(createListingsRequest.getType().name()));
        listing.setCategory(createListingsRequest.getCategory());
        listing.setLatitude(createListingsRequest.getLatitude());
        listing.setLongitude(createListingsRequest.getLongitude());
        listing.setAddress(createListingsRequest.getAddress());
        listing.setCity(createListingsRequest.getCity());
        listing.setUserId(createListingsRequest.getUserId());
        listing.setStatus(Listings.ListingStatus.ACTIVE);
        listing.setUpdatedAt(Instant.now());
        return listingsRepository.save(listing);
    }

    public List<Listings> getAllListings() {
        return listingsRepository.findAll();
    }

    public List<Listings> getListingsByUser(UUID userId) {
        return listingsRepository.findByUserId(userId);
    }

    public Optional<Listings> getListingById(UUID id) {
        return listingsRepository.findById(id);
    }

    @Transactional
    public Optional<Listings> updateListing(UUID id, UpdateListingsRequest updateListingsRequest) {
        return listingsRepository.findById(id).map(existing -> {
            existing.setTitle(updateListingsRequest.getTitle());
            existing.setDescription(updateListingsRequest.getDescription());
            existing.setType(Listings.ListingType.valueOf(updateListingsRequest.getType().name()));
            existing.setCategory(updateListingsRequest.getCategory());
            existing.setLatitude(updateListingsRequest.getLatitude());
            existing.setLongitude(updateListingsRequest.getLongitude());
            existing.setAddress(updateListingsRequest.getAddress());
            existing.setCity(updateListingsRequest.getCity());

            // Correction ici
            if (updateListingsRequest.getStatus() != null && !updateListingsRequest.getStatus().isBlank()) {
                existing.setStatus(ListingStatus.valueOf(updateListingsRequest.getStatus().toUpperCase()));
            }

            existing.setUpdatedAt(Instant.now());
            return listingsRepository.save(existing);
        });
    }

    @Transactional
    public void deleteListing(UUID id) {
        listingsRepository.deleteById(id);
    }

    public List<Listings> searchListings(String title) {
        return listingsRepository.findByTitleContainingIgnoreCase(title);
    }

    public List<Listings> getListingsByStatus(ListingStatus status) {
        return listingsRepository.findByStatus(status);
    }
}
