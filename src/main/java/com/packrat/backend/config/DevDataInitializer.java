package com.packrat.backend.config;

import java.util.Optional;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.packrat.backend.entity.User;
import com.packrat.backend.repository.UserRepository;

@Component @Profile("local")
public class DevDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DevDataInitializer(final UserRepository userRepository, final PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        final Optional<User> user = userRepository.findByUsername("dev");
        if (user.isEmpty()) {
            final User devUser = new User();
            devUser.setUsername("dev");
            devUser.setPassword(passwordEncoder.encode("dev"));
            userRepository.save(devUser);
        }
    }
    
}
