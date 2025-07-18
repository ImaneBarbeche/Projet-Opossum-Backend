package com.opossum.auth;

import com.opossum.user.User;
import com.opossum.user.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class VerificationViewController {

    private final UserRepository userRepository;

    public VerificationViewController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/api/v1/auth/verify")
    public String verifyEmail(@RequestParam("token") String token, Model model) {
        Optional<User> optionalUser = userRepository.findByEmailVerificationToken(token);

        if (optionalUser.isEmpty()) {
            return "verification-error";
        }

        User user = optionalUser.get();
        user.setIsEmailVerified(true);
        user.setEmailVerificationToken(null);
        userRepository.save(user);

        return "verification-success";
    }
}
