package com.pactomais.authservice.team;

import com.pactomais.authservice.user.User;
import com.pactomais.authservice.user.UserNotFoundException;
import com.pactomais.authservice.user.UserRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;

    public Team create(CreateTeamRequest request) {
        Team team = Team.builder().nome(request.nome()).build();
        return teamRepository.save(team);
    }

    public List<Team> listAll() {
        return teamRepository.findAll();
    }

    @Transactional
    public Team addMember(UUID teamId, UUID userId) {
        Team team = teamRepository.findById(teamId).orElseThrow(() -> new TeamNotFoundException(teamId));
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        team.getMembros().add(user);
        return teamRepository.save(team);
    }

    public List<User> listMembers(UUID teamId) {
        Team team = teamRepository.findById(teamId).orElseThrow(() -> new TeamNotFoundException(teamId));
        return List.copyOf(team.getMembros());
    }
}
