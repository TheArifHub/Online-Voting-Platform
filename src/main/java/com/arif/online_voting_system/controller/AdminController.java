package com.arif.online_voting_system.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.arif.online_voting_system.dto.Candidate;
import com.arif.online_voting_system.dto.Voter;
import com.arif.online_voting_system.repository.CandidateRepository;
import com.arif.online_voting_system.repository.VoterRepository;
import com.arif.online_voting_system.service.AdminService;
import com.arif.online_voting_system.service.CandidateService;
import jakarta.servlet.http.HttpSession;
import lombok.Data;

@Controller
@RequestMapping("/admin")
@Data
public class AdminController {
	@Value("${portal.admin.name}")
	String name;

	@Value("${portal.admin.password}")
	String pwd;

	@Autowired
	CandidateService candidateService;

	@Autowired
	AdminService adminService;

	@Autowired
	VoterRepository voterRepository;

	@Autowired
	CandidateRepository candidateRepository;

	@PostMapping("/login")
	public String login(@RequestParam String adminname, @RequestParam String password, HttpSession session) {
		return adminService.login(adminname, password, session);
	}

	@GetMapping("/dashboard")
	public String dashboard(HttpSession session) {
		if (session.getAttribute("admin") != null) {
			return "admin-home";
		}
		session.setAttribute("error", "Invalid Session, Login Again!");
		return "redirect:/login";
	}

	@GetMapping("/manage-candidates")
	public String manageCandidates(Model model, HttpSession session) {
		if (session.getAttribute("admin") != null) {
			List<Candidate> candidates = candidateService.getAllCandidates();
			model.addAttribute("candidates", candidates);
			return "manage-candidates";
		}
		session.setAttribute("error", "Invalid Session, Login Again!");
		return "redirect:/login";
	}

	@GetMapping("/manage-voters")
	public String manageVoters(Model model, HttpSession session) {
		if (session.getAttribute("admin") != null) {
			List<Voter> voters = voterRepository.findAll();
			model.addAttribute("voters", voters);
			return "manage-voters";
		}
		session.setAttribute("error", "Invalid Session, Login Again!");
		return "redirect:/login";
	}

	@GetMapping("/elections")
	public String viewElections(Model model, HttpSession session) {
		if (session.getAttribute("admin") != null) {
			List<Candidate> candidates = candidateRepository.findAllByOrderByVoteCountDesc();
			int totalVotes = candidates.stream().mapToInt(Candidate::getVoteCount).sum();
			model.addAttribute("candidates", candidates);
			model.addAttribute("totalVotes", totalVotes);
			return "admin-elections";
		}
		session.setAttribute("error", "Invalid Session, Login Again!");
		return "redirect:/login";
	}

	@PostMapping("/approve-candidate/{id}")
	public String approveCandidate(@PathVariable int id, HttpSession session) {
		if (session.getAttribute("admin") != null) {
			candidateService.updateCandidateStatus(id, "APPROVED");
			session.setAttribute("success", "Candidate approved successfully");
			return "redirect:/admin/manage-candidates";
		}
		session.setAttribute("error", "Invalid Session, Login Again!");
		return "redirect:/login";
	}

	@PostMapping("/reject-candidate/{id}")
	public String rejectCandidate(@PathVariable int id, HttpSession session) {
		if (session.getAttribute("admin") != null) {
			candidateService.updateCandidateStatus(id, "REJECTED");
			session.setAttribute("success", "Candidate rejected successfully");
			return "redirect:/admin/manage-candidates";
		}
		session.setAttribute("error", "Invalid Session, Login Again!");
		return "redirect:/login";
	}
}
