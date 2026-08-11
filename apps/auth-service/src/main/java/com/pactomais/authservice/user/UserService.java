package com.pactomais.authservice.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyInUseException(request.email());
        }

        User user =
                User.builder()
                        .nome(request.nome())
                        .email(request.email())
                        .senhaHash(passwordEncoder.encode(request.senha()))
                        .build();

        return userRepository.save(user);
    }
}
