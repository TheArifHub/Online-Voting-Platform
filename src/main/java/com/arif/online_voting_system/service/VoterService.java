package com.arif.online_voting_system.service;

import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Random;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.arif.online_voting_system.dto.Voter;
import com.arif.online_voting_system.helper.AES;
import com.arif.online_voting_system.helper.MyMailSender;
import com.arif.online_voting_system.repository.VoterRepository;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Service
public class VoterService {

	@Autowired
	VoterRepository repository;

	@Autowired
	MyMailSender mailSender;

	public String register(Voter voter, ModelMap map) {
		map.put("voter", voter);
		return "register.html";
	}

	public String register(@Valid Voter voter, BindingResult result, RedirectAttributes redirectAttributes)
			throws UnsupportedEncodingException, MessagingException {

		if (!voter.getPassword().equals(voter.getConfirmpassword()))
			result.rejectValue("confirmpassword", "error.confirmpassword",
					"* Password and Confirm Password Should Be Matching");

		if (repository.existsByEmail(voter.getEmail()))
			result.rejectValue("email", "error.email", "* Email Already Exists");

		if (repository.existsByVoterid(voter.getVoterid()))
			result.rejectValue("voterid", "error.voterid", "* Voter-Id Already Exists");

		if (result.hasErrors())
			return "register.html";

		else {
			voter.setOtp(generateSecureOtp());
			voter.setVerified(false);
			voter.setPassword(AES.encrypt(voter.getPassword()));
			repository.save(voter);
			System.err.println(voter.getOtp());

			mailSender.sendOtp(voter);

			redirectAttributes.addFlashAttribute("success", "OTP Sent Successfully!");
			return "redirect:/voter/otp/" + voter.getId();
		}
	}

	private int generateSecureOtp() {
		SecureRandom secureRandom = new SecureRandom();
		return secureRandom.nextInt(100000, 1000000);
	}

	public String otp(String otpInput, int id, HttpSession session, RedirectAttributes redirectAttributes) {
		Voter voter = repository.findById(id).orElseThrow();

		if (otpInput == null || otpInput.trim().isEmpty()) {
			redirectAttributes.addFlashAttribute("error", "OTP is required. Please enter the OTP.");
			return "redirect:/voter/otp/" + id;
		}

		try {

			int enteredOtp = Integer.parseInt(otpInput);

			if (voter.getOtp() == enteredOtp) {
				voter.setVerified(true);
				repository.save(voter);
				redirectAttributes.addFlashAttribute("success", "Account Created Successfully");
				return "redirect:/";
			} else {
				redirectAttributes.addFlashAttribute("error", "OTP Mismatch. Try Again.");
				return "redirect:/voter/otp/" + voter.getId();
			}
		} catch (NumberFormatException e) {

			redirectAttributes.addFlashAttribute("error", "Invalid OTP format. Please enter a numeric OTP.");
			return "redirect:/voter/otp/" + id;
		}
	}

	public String resendotp(int id, HttpSession session, RedirectAttributes redirectAttributes)
			throws UnsupportedEncodingException, MessagingException {
		Voter voter = repository.findById(id).orElseThrow();

		if (voter.isVerified()) {
			redirectAttributes.addFlashAttribute("error", "Your account is already verified. No need to resend OTP.");
			return "redirect:/";
		}

		int newOtp = generateSecureOtp();
		System.err.println(newOtp);

		voter.setOtp(newOtp);

		repository.save(voter);
		mailSender.sendOtp(voter);
		redirectAttributes.addFlashAttribute("success", "OTP resent successfully. Please check your email.");
		return "redirect:/voter/otp/" + voter.getId();
	}

	public String login(String voterid, String password, HttpSession session,RedirectAttributes redirectAttributes) {
	    Voter voter = repository.findByVoterid(voterid);

	    if (voter == null) {
	    	redirectAttributes.addFlashAttribute("error", "Invalid credentials!");
	        return "redirect:/login";
	    }

	    if (!voter.isVerified()) {
	        redirectAttributes.addFlashAttribute("error", "Your account is not verified. Please verify your account before logging in.");
	        return "redirect:/login";
	    }

	    try {
	        if (AES.decrypt(voter.getPassword()).equals(password)) {
	            session.setAttribute("success", "Login Successful as a voter");
	            session.setAttribute("voter", voter); 
	            return "voter-home.html";
	        }
	    } catch (Exception e) {
	        redirectAttributes.addFlashAttribute("error", "An error occurred during password decryption. Please try again.");
	        return "redirect:/login";
	    }

	    redirectAttributes.addFlashAttribute("error", "Invalid credentials!");
	    return "redirect:/login";
	}
}
