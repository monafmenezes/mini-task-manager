package com.pactomais.authservice.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;

    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private UserService userService;

    @Test
    void register_deveLancarExcecaoQuandoEmailJaEstaEmUso() {
        RegisterRequest request = new RegisterRequest("Fulano", "fulano@teste.com", "senha12345");
        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(EmailAlreadyInUseException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void register_deveArmazenarSenhaComoHashNuncaEmTextoPuro() {
        RegisterRequest request = new RegisterRequest("Fulano", "fulano@teste.com", "senha12345");
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.senha())).thenReturn("hash-simulado");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User user = userService.register(request);

        assertThat(user.getSenhaHash()).isEqualTo("hash-simulado");
        assertThat(user.getSenhaHash()).isNotEqualTo(request.senha());
        verify(passwordEncoder).encode(request.senha());
    }
}
