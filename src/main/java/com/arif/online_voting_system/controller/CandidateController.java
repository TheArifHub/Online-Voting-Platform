package com.arif.online_voting_system.controller;

import java.io.UnsupportedEncodingException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import com.arif.online_voting_system.dto.Candidate;
import com.arif.online_voting_system.repository.CandidateRepository;
import com.arif.online_voting_system.service.CandidateService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/candidate")
public class CandidateController {

	@Autowired
	private CandidateService candidateService;

	@Autowired
	private CandidateRepository candidateRepository;

	@GetMapping("/candidate-register")
	public String loadCandidatePage(Candidate candidate, ModelMap map) {
		return candidateService.loadRegisterPage(candidate, map);
	}

	@PostMapping("/candidate-register")
	public String saveCandidateRegister(@Valid @ModelAttribute("candidate") Candidate candidate, BindingResult result,
			@RequestParam("profilePic") MultipartFile file, Model model, HttpSession session) {

		if (result.hasErrors()) {
			return "candidate-register";
		}

		try {
			return candidateService.saveRegisterPage(candidate, file, result, session);
		} catch (Exception e) {
			model.addAttribute("error", e.getMessage());
			return "candidate-register";
		}
	}

	@GetMapping("/dashboard")
	public String showDashboard(HttpSession session) {
		if (session.getAttribute("candidate") == null) {
			session.setAttribute("error", "Please login first");
			return "redirect:/login";
		}
		Candidate candidate = (Candidate) session.getAttribute("candidate");
		Candidate refreshedCandidate = candidateRepository.findById(candidate.getId()).orElse(candidate);
		session.setAttribute("candidate", refreshedCandidate);
		return "candidate-home";
	}

	@GetMapping("/otp/{id}")
	public String otp(@PathVariable int id, ModelMap map) {
		map.put("id", id);
		return "candidate-otp.html";
	}

	@PostMapping("/otp")
	public String otp(@RequestParam(value = "otp", required = false) String otp, @RequestParam("id") int id,
			HttpSession session) {
		return candidateService.otp(otp, id, session);
	}

	@GetMapping("/resend-otp/{id}")
	public String resend(@PathVariable int id, HttpSession session)
			throws UnsupportedEncodingException, MessagingException {
		return candidateService.resendotp(id, session);
	}

	@PostMapping("/login")
	public String login(@RequestParam(value = "email") String email, @RequestParam(value = "password") String password,
			HttpSession session) {
		return candidateService.loginLogic(email, password, session);
	}

	@GetMapping("/login")
	public String loadHome(HttpSession session) {
		return candidateService.loadHome(session);
	}

	@GetMapping("/update-profile/{id}")
	public String showUpdateProfile(@PathVariable int id, Model model, HttpSession session) {
		return candidateService.showUpdateProfile(id, session, model);
	}

	@PostMapping("/update-profile/{id}")
	public String updateProfile(@PathVariable int id, Candidate updatedCandidate,
			@RequestParam("profilePic") MultipartFile file, BindingResult bindingResult, HttpSession session) {
		return candidateService.updateProfile(id, updatedCandidate, file, bindingResult, session);
	}

	@GetMapping("/profile")
	public String showProfile(HttpSession session, Model model) {
		if (session.getAttribute("candidate") != null) {
			Candidate candidate = (Candidate) session.getAttribute("candidate");
			model.addAttribute("candidate", candidate);
			return "my-profile";
		}
		session.setAttribute("error", "Invalid Session, Login Again!");
		return "redirect:/login";
	}

	@GetMapping("/election-status")
	public String showElectionStatus(Model model, HttpSession session) {
		if (session.getAttribute("candidate") != null) {
			Candidate candidate = (Candidate) session.getAttribute("candidate");

			List<Candidate> allCandidates = candidateRepository.findAllByOrderByVoteCountDesc();

			int totalVotes = allCandidates.stream().mapToInt(Candidate::getVoteCount).sum();

			int position = 1;
			for (Candidate c : allCandidates) {
				if (c.getId() == candidate.getId()) {
					break;
				}
				position++;
			}

			model.addAttribute("candidates", allCandidates);
			model.addAttribute("totalVotes", totalVotes);
			model.addAttribute("position", position);

			return "election-status";
		}
		session.setAttribute("error", "Invalid Session, Login Again!");
		return "redirect:/login";
	}

}
