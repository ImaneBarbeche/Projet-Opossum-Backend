package com.opossum.listings;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class listingsService {

    private final listingsRepository listingsRepository;

    public listingsService(listingsRepository listingsRepository) {
        this.listingsRepository = listingsRepository;
    }

    public listings createListing(listings listing) {
        listing.setCreatedAt(Instant.now());
        listing.setUpdatedAt(Instant.now());
        return listingsRepository.save(listing);
    }

    public List<listings> getAllListings(boolean isLost) {
        return listingsRepository.findByIsLost(isLost);
    }

    public List<listings> getListingsByUser(UUID userId) {
        return listingsRepository.findByUserId(userId);
    }

    public Optional<listings> getListingById(UUID id) {
        return listingsRepository.findById(id);
    }

    @Transactional
    public Optional<listings> updateListing(UUID id, listings updated) {
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

    public List<listings> searchListings(String title) {
        return listingsRepository.findByTitleContainingIgnoreCase(title);
    }
}
