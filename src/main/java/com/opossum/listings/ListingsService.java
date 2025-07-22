package com.opossum.listings;

import com.opossum.user.UserRepository;
import com.opossum.common.enums.ListingType;
import com.opossum.common.enums.ListingStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.*;

import com.opossum.listings.dto.CreateListingsRequest;
import com.opossum.listings.dto.UpdateListingsRequest;

@Service
public class ListingsService {
    // Permet de sauvegarder une entité Listings
    public Listings save(Listings l) {
        return listingsRepository.save(l);
    }

    private final ListingsRepository listingsRepository;

    public ListingsService(ListingsRepository listingsRepository, UserRepository userRepository) {
        this.listingsRepository = listingsRepository;
    }

    @Transactional
    public Listings createListing(CreateListingsRequest createListingsRequest, UUID userId) {
        Listings listing = new Listings();
        listing.setCreatedAt(Instant.now());
        listing.setTitle(createListingsRequest.getTitle());
        listing.setDescription(createListingsRequest.getDescription());
        // Conversion String -> Enum (type)
        if (createListingsRequest.getType() != null) {
            listing.setType(ListingType.valueOf(createListingsRequest.getType().toUpperCase()));
        }
        listing.setCategory(createListingsRequest.getCategory());
        // Location (null safe)
        if (createListingsRequest.getLocation() != null) {
            var loc = createListingsRequest.getLocation();
            if (loc.getLatitude() != null && loc.getLongitude() != null) {
                listing.setLatitude(java.math.BigDecimal.valueOf(loc.getLatitude()));
                listing.setLongitude(java.math.BigDecimal.valueOf(loc.getLongitude()));
            }
            listing.setAddress(loc.getAddress());
            listing.setCity(loc.getCity());
        }
        // Validation métier spec : city requis, (GPS ou adresse requis)
        if (listing.getCity() == null || listing.getCity().isBlank()) {
            throw new IllegalArgumentException("Le champ 'city' est obligatoire.");
        }
        boolean hasGps = listing.getLatitude() != null && listing.getLongitude() != null;
        boolean hasAddress = listing.getAddress() != null && !listing.getAddress().isBlank();
        if (!hasGps && !hasAddress) {
            throw new IllegalArgumentException("Soit latitude+longitude, soit address doit être renseigné.");
        }
        // userId doit venir du contexte d'authentification
        listing.setUserId(userId);
        listing.setStatus(ListingStatus.ACTIVE);
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
            if (updateListingsRequest.getTitle() != null)
                existing.setTitle(updateListingsRequest.getTitle());
            if (updateListingsRequest.getDescription() != null)
                existing.setDescription(updateListingsRequest.getDescription());
            if (updateListingsRequest.getType() != null) {
                existing.setType(ListingType.valueOf(updateListingsRequest.getType().name()));
            }
            if (updateListingsRequest.getCategory() != null)
                existing.setCategory(updateListingsRequest.getCategory());
            if (updateListingsRequest.getLatitude() != null)
                existing.setLatitude(java.math.BigDecimal.valueOf(updateListingsRequest.getLatitude()));
            if (updateListingsRequest.getLongitude() != null)
                existing.setLongitude(java.math.BigDecimal.valueOf(updateListingsRequest.getLongitude()));
            if (updateListingsRequest.getAddress() != null)
                existing.setAddress(updateListingsRequest.getAddress());
            if (updateListingsRequest.getCity() != null)
                existing.setCity(updateListingsRequest.getCity());
            if (updateListingsRequest.getStatus() != null) {
                existing.setStatus(updateListingsRequest.getStatus());
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
