package com.pactomais.authservice.team;

import com.pactomais.authservice.user.UserResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TeamResponse create(@Valid @RequestBody CreateTeamRequest request) {
        Team team = teamService.create(request);
        return TeamResponse.from(team);
    }

    @GetMapping
    public List<TeamResponse> listAll() {
        return teamService.listAll().stream().map(TeamResponse::from).toList();
    }

    @PostMapping("/{id}/membros")
    public TeamResponse addMember(@PathVariable UUID id, @Valid @RequestBody AddMemberRequest request) {
        Team team = teamService.addMember(id, request.userId());
        return TeamResponse.from(team);
    }

    @GetMapping("/{id}/membros")
    public List<UserResponse> listMembers(@PathVariable UUID id) {
        return teamService.listMembers(id).stream().map(UserResponse::from).toList();
    }
}
