package com.arif.online_voting_system.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.arif.online_voting_system.dto.Voter;

public interface VoterRepository extends JpaRepository<Voter, Integer> {

	boolean existsByVoterid(String voterid);

	Optional<Voter> findByVoterid(String voterid);

	boolean existsByEmail(String email);
}
