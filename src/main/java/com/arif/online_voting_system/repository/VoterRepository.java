package com.arif.online_voting_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.arif.online_voting_system.dto.Voter;

public interface VoterRepository extends JpaRepository<Voter, Integer>{

	boolean existsByEmail(String email);

	boolean existsByVoterid(String voterid);

	Voter findByVoterid(String voterid);
}
