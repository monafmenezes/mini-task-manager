package com.pactomais.authservice.team;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.pactomais.authservice.user.User;
import com.pactomais.authservice.user.UserNotFoundException;
import com.pactomais.authservice.user.UserRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

    @Mock private TeamRepository teamRepository;

    @Mock private UserRepository userRepository;

    @InjectMocks private TeamService teamService;

    @Test
    void addMember_deveLancarExcecaoQuandoTimeNaoExiste() {
        UUID teamId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(teamRepository.findById(teamId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teamService.addMember(teamId, userId))
                .isInstanceOf(TeamNotFoundException.class);
    }

    @Test
    void addMember_deveLancarExcecaoQuandoUsuarioNaoExiste() {
        UUID teamId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Team team = Team.builder().id(teamId).nome("Time de Produto").build();
        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teamService.addMember(teamId, userId))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void addMember_deveAdicionarUsuarioAoTime() {
        UUID teamId = UUID.randomUUID();
        Team team = Team.builder().id(teamId).nome("Time de Produto").build();
        User user =
                User.builder()
                        .id(UUID.randomUUID())
                        .nome("Fulano de Tal")
                        .email("fulano@teste.com")
                        .senhaHash("hash")
                        .build();
        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(teamRepository.save(any(Team.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Team resultado = teamService.addMember(teamId, user.getId());

        assertThat(resultado.getMembros()).containsExactly(user);
    }
}
