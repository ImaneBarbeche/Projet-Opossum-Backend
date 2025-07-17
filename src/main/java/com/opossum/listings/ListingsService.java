package com.opossum.listings;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ListingsService {

    private final ListingsRepository listingsRepository;

    public ListingsService(ListingsRepository listingsRepository) {
        this.listingsRepository = listingsRepository;
    }

    public Listings createListing(Listings listing) {
        listing.setCreatedAt(Instant.now());
        listing.setUpdatedAt(Instant.now());
        return listingsRepository.save(listing);
    }

    public List<Listings> getAllListings(boolean isLost) {
        return listingsRepository.findByIsLost(isLost);
    }

    public List<Listings> getListingsByUser(UUID userId) {
        return listingsRepository.findByUserId(userId);
    }

    public Optional<Listings> getListingById(UUID id) {
        return listingsRepository.findById(id);
    }

    @Transactional
    public Optional<Listings> updateListing(UUID id, Listings updated) {
        return listingsRepository.findById(id).map(existing -> {
            existing.setTitle(updated.getTitle());
            existing.setDescription(updated.getDescription());
            existing.setIsLost(updated.getIsLost());
            existing.setUpdatedAt(Instant.now());
            return listingsRepository.save(existing);
        });
    }

    public void deleteListing(UUID id) {
        listingsRepository.deleteById(id);
    }

    public List<Listings> searchListings(String title) {
        return listingsRepository.findByTitleContainingIgnoreCase(title);
    }
}
