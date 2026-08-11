package com.pactomais.authservice.team;

import com.pactomais.authservice.user.UserResponse;
import java.util.List;
import java.util.UUID;

public record TeamResponse(UUID id, String nome, List<UserResponse> membros) {

    public static TeamResponse from(Team team) {
        List<UserResponse> membros =
                team.getMembros().stream().map(UserResponse::from).toList();
        return new TeamResponse(team.getId(), team.getNome(), membros);
    }
}
