package com.arif.online_voting_system.service;

import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;
import com.arif.online_voting_system.dto.Candidate;
import com.arif.online_voting_system.helper.AES;
import com.arif.online_voting_system.helper.CloudinaryHelper;
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

	@Autowired
	CloudinaryHelper cloudinaryHelper;

	public String loadRegisterPage(Candidate candidate, ModelMap map) {
		map.addAttribute("candidate", candidate);
		return "candidate-register.html";
	}

	public String saveRegisterPage(@Valid Candidate candidate, MultipartFile file, BindingResult result,
			HttpSession session) throws UnsupportedEncodingException, MessagingException {

		if (candidate.getPassword() == null || candidate.getConfirmPassword() == null) {
			result.rejectValue("confirmPassword", "error.confirmPassword",
					"* Password and Confirm Password cannot be empty");
		} else if (!candidate.getPassword().equals(candidate.getConfirmPassword())) {
			result.rejectValue("confirmPassword", "error.confirmPassword",
					"* Password and Confirm Password should match");
		}

		if (candidate.getEmail() != null && candidateRepository.existsByEmail(candidate.getEmail())) {
			result.rejectValue("email", "error.email", "* Email already exists");
		}

		if (candidateRepository.existsByParty(candidate.getParty())) {
		    result.rejectValue("party", "error.party", "* This party is already registered with another candidate");
		    return "candidate-register";
		}

		if (file == null || file.isEmpty()) {
			result.rejectValue("profilePicUrl", "error.profilePic", "* Profile picture is required.");
			return "candidate-register";
		}
		try {
			candidate.setPassword(AES.encrypt(candidate.getPassword()));

			String imageUrl = cloudinaryHelper.saveImage(file);
			candidate.setProfilePicUrl(imageUrl);

			candidate.setOtp(generateSecureOtp());
			candidate.setVerified(false);
			candidate.setStatus("PENDING");
			candidateRepository.save(candidate);

			mailSender.sendOtp(candidate);

			session.setAttribute("success", "Registration successful. Please verify your email.");
			return "redirect:/candidate/otp/" + candidate.getId();
		} catch (Exception e) {
			result.rejectValue("global", "error.global", "Registration failed: " + e.getMessage());
			return "candidate-register";
		}
	}

	private int generateSecureOtp() {
		return new SecureRandom().nextInt(100000, 1000000);
	}

	public String otp(String otpInput, int id, HttpSession session) {
		Candidate candidate = candidateRepository.findById(id).orElseThrow();

		if (otpInput == null || otpInput.trim().isEmpty()) {
			session.setAttribute("error", "OTP is required. Please enter the OTP.");
			return "redirect:/candidate/otp/" + id;
		}

		try {

			int enteredOtp = Integer.parseInt(otpInput);

			if (candidate.getOtp() == enteredOtp) {
				candidate.setVerified(true);
				candidateRepository.save(candidate);
				session.setAttribute("success", "Account Created Successfully");
				return "redirect:/";
			} else {
				session.setAttribute("error", "OTP Mismatch. Try Again.");
				return "redirect:/candidate/otp/" + candidate.getId();
			}
		} catch (NumberFormatException e) {

			session.setAttribute("error", "Invalid OTP format. Please enter a numeric OTP.");
			return "redirect:/candidate/otp/" + id;
		}
	}

	public String resendotp(int id, HttpSession session) throws UnsupportedEncodingException, MessagingException {
		Candidate candidate = candidateRepository.findById(id).orElseThrow();

		if (candidate.isVerified()) {
			session.setAttribute("error", "Your account is already verified. No need to resend OTP.");
			return "redirect:/";
		}

		int newOtp = generateSecureOtp();
		System.err.println(newOtp);

		candidate.setOtp(newOtp);

		candidateRepository.save(candidate);
		mailSender.sendOtp(candidate);
		session.setAttribute("success", "OTP resent successfully. Please check your email.");
		return "redirect:/candidate/otp/" + candidate.getId();
	}

	public String loginLogic(String email, String password, HttpSession session) {
		List<Candidate> candidates = candidateRepository.findAllByEmail(email);

		if (candidates.isEmpty()) {
			session.setAttribute("error", "Invalid Credentials");
			return "redirect:/login";
		}

		for (Candidate candidate : candidates) {
			if (AES.decrypt(candidate.getPassword()).equals(password)) {
				if (!candidate.isVerified()) {
					session.setAttribute("error", "Please verify your account first");
					return "redirect:/login";
				}

				List<Candidate> allCandidates = candidateRepository.findAllByOrderByVoteCountDesc();
				int position = 1;
				for (Candidate c : allCandidates) {
					if (c.getId() == candidate.getId()) {
						break;
					}
					position++;
				}

				Candidate refreshedCandidate = candidateRepository.findById(candidate.getId()).orElse(candidate);
				session.setAttribute("candidate", refreshedCandidate);
				session.setAttribute("voteCount", refreshedCandidate.getVoteCount());
				session.setAttribute("position", position);
				session.setAttribute("electionStatus", refreshedCandidate.getStatus());

				return "candidate-home";
			}
		}

		session.setAttribute("error", "Invalid Credentials");
		return "redirect:/login";
	}

	public List<Candidate> getAllCandidates() {
		return candidateRepository.findAll();
	}

	public void updateCandidateStatus(int id, String status) {
		Candidate candidate = candidateRepository.findById(id).orElseThrow();
		candidate.setStatus(status);
		candidateRepository.save(candidate);
	}

	public List<Candidate> getPendingCandidates() {
		return candidateRepository.findByStatus("PENDING");
	}

	public String loadHome(HttpSession session) {
		if (session.getAttribute("candidate") != null) {
			return "candidate-home.html";
		} else {
			session.setAttribute("error", "Invlaid Session, Login Agian");
			return "redirect:/login";
		}
	}

	public String showUpdateProfile(int id, HttpSession session, Model model) {
		if (session.getAttribute("candidate") != null) {
			Candidate candidate = candidateRepository.findById(id).orElseThrow();
			model.addAttribute("candidate", candidate);
			return "update-profile";
		} else {
			session.setAttribute("error", "Invalid Session, Login Again!");
			return "redirect:/login";
		}
	}

	public String updateProfile(int id, Candidate updatedCandidate, MultipartFile file, BindingResult bindingResult,
			HttpSession session) {
		Candidate existingCandidate = candidateRepository.findById(id).orElseThrow();

		if (file != null && !file.isEmpty()) {
			String imageUrl = cloudinaryHelper.saveImage(file);
			existingCandidate.setProfilePicUrl(imageUrl);
		}

		existingCandidate.setFullname(updatedCandidate.getFullname());
		existingCandidate.setManifesto(updatedCandidate.getManifesto());

		candidateRepository.save(existingCandidate);
		session.setAttribute("success", "Profile Updated Successfully!");

		return "candidate-home";
	}

	public String showProfile(HttpSession session, Model model) {
		Candidate candidate = (Candidate) session.getAttribute("candidate");
		if (candidate != null) {
			model.addAttribute("candidate", candidate);
			return "my-profile";
		}
		session.setAttribute("error", "Invalid Session, Login Again!");
		return "redirect:/login";
	}
}
