package com.arif.online_voting_system.controller;

import java.io.UnsupportedEncodingException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.arif.online_voting_system.dto.Candidate;
import com.arif.online_voting_system.dto.Voter;
import com.arif.online_voting_system.repository.CandidateRepository;
import com.arif.online_voting_system.service.CandidateService;
import com.arif.online_voting_system.service.VoterService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/voter")
public class VoterController {

	@Autowired
	CandidateService candidateService;

	@Autowired
	CandidateRepository candidateRepository;

	@Autowired
	private VoterService service;

	@GetMapping("/voter-register")
	public String loadRegisterPage(Voter voter, ModelMap map) {
		return service.loadRegisterPage(voter, map);
	}

	@PostMapping("/voter-register")
	public String saveRegister(@Valid Voter voter, BindingResult result, HttpSession session)
			throws UnsupportedEncodingException, MessagingException {
		return service.saveRegister(voter, result, session);
	}

	@GetMapping("/dashboard")
	public String voterHome(Model model, HttpSession session) {
		if (session.getAttribute("voter") == null) {
			session.setAttribute("error", "Please login first");
			return "redirect:/login";
		}
		List<Candidate> approvedCandidates = candidateRepository.findByStatus("APPROVED");
		model.addAttribute("candidates", approvedCandidates);
		return "voter-home";
	}

	@GetMapping("/otp/{id}")
	public String otp(@PathVariable int id, ModelMap map) {
		map.put("id", id);
		return "otp-verification.html";
	}

	@PostMapping("/otp")
	public String otp(@RequestParam(value = "otp", required = false) String otp, @RequestParam("id") int id,
			HttpSession session) {
		return service.otp(otp, id, session);
	}

	@GetMapping("/resend-otp/{id}")
	public String resend(@PathVariable int id, HttpSession session)
			throws UnsupportedEncodingException, MessagingException {
		return service.resendotp(id, session);
	}

	@PostMapping("/login")
	public String login(@RequestParam(value = "voterid") String voterid,
			@RequestParam(value = "password") String password, HttpSession session) {
		return service.login(voterid, password, session);
	}

	@PostMapping("/cast-vote")
	public String castVote(@RequestParam("candidateId") int candidateId, HttpSession session) {
		return service.castVote(candidateId, session);
	}

	@GetMapping("/profile")
	public String showProfile(HttpSession session) {
		if (session.getAttribute("voter") != null) {
			return "voter-profile";
		}
		session.setAttribute("error", "Invalid Session, Login Again!");
		return "redirect:/login";
	}

}
