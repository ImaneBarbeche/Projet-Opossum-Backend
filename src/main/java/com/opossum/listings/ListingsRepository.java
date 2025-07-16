package com.opossum.listings;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface listingsRepository extends JpaRepository<listings, UUID> {

    List<listings> findByUserId(UUID userId);

    List<listings> findByIsLost(boolean isLost);

    List<listings> findByTitleContainingIgnoreCase(String title);
}
