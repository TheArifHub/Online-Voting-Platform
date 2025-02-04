package com.arif.online_voting_system.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.arif.online_voting_system.dto.Candidate;

public interface CandidateRepository extends JpaRepository<Candidate, Integer> {

	boolean existsByEmail(String email);

	boolean existsByParty(String party);

	List<Candidate> findAllByEmail(String email);

	List<Candidate> findByStatus(String string);

	List<Candidate> findAllByOrderByVoteCountDesc();

	Optional<Candidate> findByEmail(String email);

	Optional<Candidate> findByParty(String party);

}
