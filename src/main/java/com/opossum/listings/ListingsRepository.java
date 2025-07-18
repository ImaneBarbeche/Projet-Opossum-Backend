package com.opossum.listings;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ListingsRepository extends JpaRepository<Listings, UUID> {

    List<Listings> findByUserId(UUID userId);

    List<Listings> findByIsLost(boolean isLost);

    List<Listings> findByTitleContainingIgnoreCase(String title);
}