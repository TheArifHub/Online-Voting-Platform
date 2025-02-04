package com.arif.online_voting_system.controller;

import java.util.List;
import org.springframework.ui.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import com.arif.online_voting_system.dto.Candidate;
import com.arif.online_voting_system.repository.CandidateRepository;
import com.arif.online_voting_system.service.VoterService;
import jakarta.servlet.http.HttpSession;

@Controller
public class GeneralController {

	@Autowired
	VoterService service;

	@Autowired
	CandidateRepository candidateRepository;

	@GetMapping("/register")
	public String showRegisterForm() {
		return "register.html";
	}

	@GetMapping({ "/", "/home" })
	public String loadHome() {
		return "home.html";
	}

	@GetMapping("/about")
	public String loadAbout() {
		return "about.html";
	}

	@GetMapping("/contact")
	public String loadContact() {
		return "contact.html";
	}

	@GetMapping("/login")
	public String loadLoginPage() {
		return "login.html";
	}

	@GetMapping("/logout")
	public String logout(HttpSession session) {
		session.removeAttribute("candidate");
		session.removeAttribute("voter");
		session.removeAttribute("admin");
		session.setAttribute("success", "Logout Success!");
		return "redirect:/";
	}

	@GetMapping("/vote-now")
	public String voteNow(HttpSession session) {
		if (session.getAttribute("voter") != null) {
			return "redirect:/voter/dashboard";
		}
		session.setAttribute("error", "Please login or register as a voter first!");
		return "redirect:/login";
	}

	@GetMapping("/results")
	public String showResults(Model model) {
		List<Candidate> candidates = candidateRepository.findAllByOrderByVoteCountDesc();
		int totalVotes = candidates.stream().mapToInt(Candidate::getVoteCount).sum();
		model.addAttribute("candidates", candidates);
		model.addAttribute("totalVotes", totalVotes);
		return "public-results";
	}
}
