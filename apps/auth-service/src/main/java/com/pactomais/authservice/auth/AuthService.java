package com.pactomais.authservice.auth;

import com.pactomais.authservice.security.TokenService;
import com.pactomais.authservice.user.User;
import com.pactomais.authservice.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public LoginResponse login(LoginRequest request) {
        User user =
                userRepository
                        .findByEmail(request.email())
                        .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.senha(), user.getSenhaHash())) {
            throw new InvalidCredentialsException();
        }

        String token = tokenService.generateToken(user);
        return new LoginResponse(token, "Bearer", tokenService.expirationSeconds());
    }
}
