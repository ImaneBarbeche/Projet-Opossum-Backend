package com.opossum.config;

import com.opossum.user.User;
import com.opossum.user.UserRepository;
import com.opossum.common.enums.Role;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

@Component
public class AdminSeeder implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.countByRole(Role.ADMIN) == 0) {
            User admin = new User();
            admin.setEmail("admin@opossum.com");
            admin.setPasswordHash(passwordEncoder.encode("adminPassword"));
            admin.setRole(Role.ADMIN);
            admin.setActive(true);
            // Ajoute les autres champs obligatoires si besoin
            userRepository.save(admin);
            System.out.println("Admin user created.");
        }
    }
}