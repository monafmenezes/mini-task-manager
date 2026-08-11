package com.pactomais.authservice.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pactomais.authservice.security.TokenService;
import com.pactomais.authservice.user.User;
import com.pactomais.authservice.user.UserRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;

    @Mock private PasswordEncoder passwordEncoder;

    @Mock private TokenService tokenService;

    @InjectMocks private AuthService authService;

    @Test
    void login_deveLancarExcecaoQuandoEmailNaoExiste() {
        LoginRequest request = new LoginRequest("fulano@teste.com", "senha12345");
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(tokenService, never()).generateToken(any());
    }

    @Test
    void login_deveLancarExcecaoQuandoSenhaEstaErrada() {
        User user = umUsuario();
        LoginRequest request = new LoginRequest(user.getEmail(), "senhaErrada");
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.senha(), user.getSenhaHash())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(tokenService, never()).generateToken(any());
    }

    @Test
    void login_deveRetornarTokenQuandoCredenciaisEstaoCorretas() {
        User user = umUsuario();
        LoginRequest request = new LoginRequest(user.getEmail(), "senhaCorreta");
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.senha(), user.getSenhaHash())).thenReturn(true);
        when(tokenService.generateToken(user)).thenReturn("token-gerado");
        when(tokenService.expirationSeconds()).thenReturn(3600L);

        LoginResponse response = authService.login(request);

        assertThat(response.token()).isEqualTo("token-gerado");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresInSeconds()).isEqualTo(3600L);
    }

    private User umUsuario() {
        return User.builder()
                .id(UUID.randomUUID())
                .nome("Fulano de Tal")
                .email("fulano@teste.com")
                .senhaHash("hash-armazenado")
                .build();
    }
}
