package com.pactomais.authservice.team;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, UUID> {

    List<Team> findByMembros_Id(UUID userId);
}
