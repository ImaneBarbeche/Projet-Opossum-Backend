package com.opossum.admin;
import org.springframework.scheduling.annotation.Scheduled;
import java.time.ZonedDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import com.opossum.common.enums.UserStatus;
import com.opossum.listings.ListingsRepository;
import java.util.Map;

@Service
public class AdminService {

    private final com.opossum.user.UserRepository userRepository;
    private final ListingsRepository listingsRepository;

    /**
     * Purge users and listings that have been soft deleted for over 1 year.
     * Runs daily at 2:00 AM UTC.
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void purgeOldDeletedEntities() {
        ZonedDateTime threshold = ZonedDateTime.now(ZoneOffset.UTC).minusYears(1);

        // Purge users
        List<com.opossum.user.User> usersToDelete = userRepository.findByStatusAndUpdatedAtBefore(UserStatus.DELETED,
                threshold.toInstant());
        for (com.opossum.user.User user : usersToDelete) {
            // Delete related listings
            List<com.opossum.listings.Listings> userListings = listingsRepository.findByUserId(user.getId());
            for (com.opossum.listings.Listings listing : userListings) {
                listingsRepository.delete(listing);
            }
            userRepository.delete(user);
            // Optionally log the deletion
            // logger.info("Hard deleted user: {} at {}", user.getId(),
            // ZonedDateTime.now(ZoneOffset.UTC));
        }

        // Purge listings
        List<com.opossum.listings.Listings> listingsToDelete = listingsRepository
                .findByStatusAndUpdatedAtBefore(com.opossum.common.enums.ListingStatus.DELETED, threshold.toInstant());
        for (com.opossum.listings.Listings listing : listingsToDelete) {
            listingsRepository.delete(listing);
            // Optionally log the deletion
            // logger.info("Hard deleted listing: {} at {}", listing.getId(),
            // ZonedDateTime.now(ZoneOffset.UTC));
        }
    }

    /**
     * AdminService constructor.
     * 
     * @param userRepository     User repository for accessing user data.
     * @param listingsRepository Listings repository for accessing listing data.
     */
    public AdminService(com.opossum.user.UserRepository userRepository, ListingsRepository listingsRepository) {
        this.userRepository = userRepository;
        this.listingsRepository = listingsRepository;
    }

    @Autowired
    private com.opossum.auth.EmailService emailService;

    public ResponseEntity<?> getGlobalStats() {
        // Statistiques mockées pour l'exemple
        Map<String, Object> users = Map.of(
                "total", 1250,
                "active", 1100,
                "newThisMonth", 85);
        Map<String, Object> announcements = Map.of(
                "total", 2340,
                "active", 456,
                "lost", 234,
                "found", 222,
                "resolvedThisMonth", 89);
        Map<String, Object> conversations = Map.of(
                "total", 890,
                "activeToday", 45);
        Map<String, Object> files = Map.of(
                "totalSize", "2.5GB",
                "totalCount", 3456);
        Map<String, Object> data = Map.of(
                "users", users,
                "announcements", announcements,
                "conversations", conversations,
                "files", files);
        Map<String, Object> response = Map.of(
                "success", true,
                "data", data,
                "timestamp", java.time.ZonedDateTime.now(java.time.ZoneOffset.UTC).toString());
        return ResponseEntity.ok(response);
    }

    
    
}