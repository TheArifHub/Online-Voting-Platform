package com.arif.online_voting_system.service;

import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;

import com.arif.online_voting_system.dto.Candidate;
import com.arif.online_voting_system.dto.Voter;
import com.arif.online_voting_system.helper.AES;
import com.arif.online_voting_system.helper.MyMailSender;
import com.arif.online_voting_system.repository.CandidateRepository;
import com.arif.online_voting_system.repository.VoterRepository;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class VoterService {

	@Autowired
	CandidateRepository candidateRepository;

	@Autowired
	private VoterRepository repository;

	@Autowired
	private MyMailSender mailSender;

	public String loadRegisterPage(Voter voter, ModelMap map) {
		map.put("voter", voter);
		return "voter-register.html";
	}

	public String saveRegister(@Valid Voter voter, BindingResult result, HttpSession session)
			throws UnsupportedEncodingException, MessagingException {

		if (repository.existsByEmail(voter.getEmail()))
			result.rejectValue("email", "error.email", "* Email Already Exists");

		if (!voter.getPassword().equals(voter.getConfirmpassword()))
			result.rejectValue("confirmpassword", "error.confirmpassword",
					"* Password and Confirm Password Should be Matching");

		if (repository.existsByVoterid(voter.getVoterid()))
			result.rejectValue("voterid", "error.voterid", "* Voter-Id Already  exists");

		if (result.hasErrors())
			return "voter-register.html";

		else {
			voter.setOtp(generateSecureOtp());
			voter.setVerified(false);
			voter.setPassword(AES.encrypt(voter.getPassword()));
			repository.save(voter);
			mailSender.sendOtp(voter);
			session.setAttribute("success", "OTP Send Successfully!!!");
			return "redirect:/voter/otp/" + voter.getId();
		}
	}

	private int generateSecureOtp() {
		return new SecureRandom().nextInt(100000, 1000000);
	}

	public String otp(String otpInput, int id, HttpSession session) {
		Voter voter = repository.findById(id).orElseThrow();

		if (otpInput == null || otpInput.trim().isEmpty()) {
			session.setAttribute("error", "OTP is required. Please enter the OTP.");
			return "redirect:/voter/otp/" + id;
		}

		try {

			int enteredOtp = Integer.parseInt(otpInput);

			if (voter.getOtp() == enteredOtp) {
				voter.setVerified(true);
				repository.save(voter);
				session.setAttribute("success", "Account Created Successfully");
				return "redirect:/";
			} else {
				session.setAttribute("error", "OTP Mismatch. Try Again.");
				return "redirect:/voter/otp/" + voter.getId();
			}
		} catch (NumberFormatException e) {

			session.setAttribute("error", "Invalid OTP format. Please enter a numeric OTP.");
			return "redirect:/voter/otp/" + id;
		}
	}

	public String resendotp(int id, HttpSession session) throws UnsupportedEncodingException, MessagingException {
		Voter voter = repository.findById(id).orElseThrow();

		if (voter.isVerified()) {
			session.setAttribute("error", "Your account is already verified. No need to resend OTP.");
			return "redirect:/";
		}

		int newOtp = generateSecureOtp();
		System.err.println(newOtp);

		voter.setOtp(newOtp);

		repository.save(voter);
		mailSender.sendOtp(voter);
		session.setAttribute("success", "OTP resent successfully. Please check your email.");
		return "redirect:/voter/otp/" + voter.getId();
	}

	public String login(String voterid, String password, HttpSession session) {
		Voter voter = repository.findByVoterid(voterid).orElse(null);

		if (voter == null) {
			session.setAttribute("error", "Invalid credentials!");
			return "redirect:/login";
		}

		if (!voter.isVerified()) {
			session.setAttribute("error",
					"Your account is not verified. Please verify your account before logging in.");
			return "redirect:/login";
		}

		try {
			if (AES.decrypt(voter.getPassword()).equals(password)) {
				// Get fresh voter data with current hasVoted status
				Voter currentVoter = repository.findById(voter.getId()).orElse(voter);
				session.setAttribute("success", "Login Successful as a voter");
				session.setAttribute("voter", currentVoter);
				return "voter-home.html";
			}
		} catch (Exception e) {
			session.setAttribute("error", "An error occurred during password decryption. Please try again.");
			return "redirect:/login";
		}

		session.setAttribute("error", "Invalid credentials!");
		return "redirect:/login";
	}

	public String castVote(int candidateId, HttpSession session) {
		Voter voter = (Voter) session.getAttribute("voter");

		if (voter == null) {
			session.setAttribute("error", "Please login to vote");
			return "redirect:/login";
		}

		// Check if voter has already voted
		Voter currentVoter = repository.findById(voter.getId()).orElseThrow();
		if (currentVoter.isHasVoted()) {
			session.setAttribute("error", "You have already voted!");
			return "redirect:/voter/dashboard";
		}

		Candidate candidate = candidateRepository.findById(candidateId).orElseThrow();
		candidate.setVoteCount(candidate.getVoteCount() + 1);
		candidateRepository.save(candidate);

		currentVoter.setHasVoted(true);
		repository.save(currentVoter);

		session.setAttribute("voter", currentVoter);
		session.setAttribute("success", "Vote cast successfully!");
		return "redirect:/voter/dashboard";
	}

}
