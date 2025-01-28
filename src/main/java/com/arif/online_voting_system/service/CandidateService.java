package com.arif.online_voting_system.service;

import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.arif.online_voting_system.dto.Candidate;
import com.arif.online_voting_system.helper.AES;
import com.arif.online_voting_system.helper.MyMailSender;
import com.arif.online_voting_system.repository.CandidateRepository;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Service
public class CandidateService {

	@Autowired
	CandidateRepository candidateRepository;

	@Autowired
	MyMailSender mailSender;

	public String registerpage(Candidate candidate, ModelMap map) {
		map.put("candidate", candidate);
		return "candidate-register.html";
	}

	public String registerpage(@Valid Candidate candidate, BindingResult bindingResult, HttpSession session)
			throws UnsupportedEncodingException, MessagingException {
		if (candidate.getPassword() == null || candidate.getConfirmPassword() == null) {
			bindingResult.rejectValue("confirmPassword", "error.confirmPassword",
					"* Password and Confirm Password cannot be empty");
		} else if (!candidate.getPassword().equals(candidate.getConfirmPassword())) {
			bindingResult.rejectValue("confirmPassword", "error.confirmPassword",
					"* Password and Confirm Password should match");
		}

		if (candidate.getEmail() != null && candidateRepository.existsByEmail(candidate.getEmail())) {
			bindingResult.rejectValue("email", "error.email", "* Email already exists");
		}

		if (candidate.getParty() != null && candidateRepository.existsByParty(candidate.getParty())) {
			bindingResult.rejectValue("party", "error.party", "* This party is already taken by another candidate");
		}

		if (bindingResult.hasErrors()) {
			return "candidate-register";
		} else {
			candidate.setOtp(generateSecureOtp());
			candidate.setVerified(false);
			candidate.setPassword(AES.encrypt(candidate.getPassword()));
			candidateRepository.save(candidate);
			System.err.println(candidate.getOtp());

			mailSender.sendOtp(candidate);
			session.setAttribute("success", "OTP Sent Successfully!");
			return "redirect:/candidate/otp/" + candidate.getId();
		}
	}

	private int generateSecureOtp() {
		SecureRandom secureRandom = new SecureRandom();
		return secureRandom.nextInt(100000, 1000000);
	}

	public String otp(String otpInput, int id, HttpSession session, RedirectAttributes redirectAttributes) {
		Candidate candidate = candidateRepository.findById(id).orElseThrow();

		if (otpInput == null || otpInput.trim().isEmpty()) {
			redirectAttributes.addFlashAttribute("error", "OTP is required. Please enter the OTP.");
			return "redirect:/candidate/otp/" + id;
		}

		try {

			int enteredOtp = Integer.parseInt(otpInput);

			if (candidate.getOtp() == enteredOtp) {
				candidate.setVerified(true);
				candidateRepository.save(candidate);
				redirectAttributes.addFlashAttribute("success", "Account Created Successfully");
				return "redirect:/";
			} else {
				redirectAttributes.addFlashAttribute("error", "OTP Mismatch. Try Again.");
				return "redirect:/candidate/otp/" + candidate.getId();
			}
		} catch (NumberFormatException e) {

			redirectAttributes.addFlashAttribute("error", "Invalid OTP format. Please enter a numeric OTP.");
			return "redirect:/candidate/otp/" + id;
		}
	}

	public String resendotp(int id, HttpSession session, RedirectAttributes redirectAttributes)
			throws UnsupportedEncodingException, MessagingException {
		Candidate candidate = candidateRepository.findById(id).orElseThrow();

		if (candidate.isVerified()) {
			redirectAttributes.addFlashAttribute("error", "Your account is already verified. No need to resend OTP.");
			return "redirect:/";
		}

		int newOtp = generateSecureOtp();
		System.err.println(newOtp);

		candidate.setOtp(newOtp);

		candidateRepository.save(candidate);
		mailSender.sendOtp(candidate);
		redirectAttributes.addFlashAttribute("success", "OTP resent successfully. Please check your email.");
		return "redirect:/candidate/otp/" + candidate.getId();
	}

	public String loginLogic(String email, String password, HttpSession session) {
		Candidate candidate = candidateRepository.findByEmail(email);

		if (candidate == null) {
			session.setAttribute("error", "Invalid credentials!");
			return "redirect:/candidate/login";
		}

		if (!candidate.isVerified()) {
			session.setAttribute("error",
					"Your account is not verified. Please verify your account before logging in.");
			return "redirect:/login";
		}

		try {
			if (AES.decrypt(candidate.getPassword()).equals(password)) {
				session.setAttribute("success", "Login Successful as a Candidate");
				session.setAttribute("candidate", candidate);
				return "candidate-home.html";
			}
		} catch (Exception e) {
			session.setAttribute("error", "An error occurred during password decryption. Please try again.");
			return "redirect:/candidate/login";
		}

		session.setAttribute("error", "Invalid credentials!");
		return "redirect:/candidate/login";
	}
}
