package com.arif.online_voting_system.controller;

import java.io.UnsupportedEncodingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.arif.online_voting_system.dto.Candidate;
import com.arif.online_voting_system.service.CandidateService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/candidate")
public class CandidateController {

	@Autowired
	CandidateService candidateService;

	@GetMapping("/candidate-register")
	public String loadRegisterpage(Candidate candidate, ModelMap map) {
		return candidateService.registerpage(candidate, map);
	}

	@PostMapping("/candidate-register")
	public String register(@Valid Candidate candidate, BindingResult bindingResult, HttpSession session)
			throws UnsupportedEncodingException, MessagingException {
		return candidateService.registerpage(candidate, bindingResult, session);
	}

	@GetMapping("/otp/{id}")
	public String otp(@PathVariable int id, ModelMap map) {
		map.put("id", id);
		return "candidate-otp.html";
	}

	@PostMapping("/otp")
	public String otp(@RequestParam(value = "otp", required = false) String otp, @RequestParam("id") int id,
			HttpSession session, RedirectAttributes redirectAttributes) {
		return candidateService.otp(otp, id, session, redirectAttributes);
	}

	@GetMapping("/resend-otp/{id}")
	public String resend(@PathVariable int id, HttpSession session, RedirectAttributes redirectAttributes)
			throws UnsupportedEncodingException, MessagingException {
		return candidateService.resendotp(id, session, redirectAttributes);
	}

	@PostMapping("/login")
	public String login(@RequestParam(value = "email") String email, @RequestParam(value = "password") String password,
			HttpSession session) {
		return candidateService.loginLogic(email, password, session);
	}
}
