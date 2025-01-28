package com.arif.online_voting_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.arif.online_voting_system.dto.Candidate;

public interface CandidateRepository extends JpaRepository<Candidate, Integer> {

	boolean existsByEmail(String email);

	boolean existsByParty(String party);

	Candidate findByEmail(String email);

}
