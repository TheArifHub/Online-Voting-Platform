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

import com.arif.online_voting_system.dto.Voter;
import com.arif.online_voting_system.service.VoterService;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/voter")
public class VoterController {
	
	@Autowired
	VoterService service;
	
	@GetMapping("/register")
	public String loadRegisterPage(Voter voter, ModelMap map) {
		return service.register(voter, map);
	}

	@PostMapping("/register")
	public String loadRegisterPage(@Valid Voter voter, BindingResult result,RedirectAttributes redirectAttributes)
			throws UnsupportedEncodingException, MessagingException {
		return service.register(voter, result,redirectAttributes);
	}

	@GetMapping("/otp/{id}")
	public String otp(@PathVariable int id, ModelMap map) {
		map.put("id", id);
		return "otp-verification.html";
	}

	@PostMapping("/otp")
	public String otp(@RequestParam(value="otp",required = false) String otp, @RequestParam("id") int id, HttpSession session,RedirectAttributes redirectAttributes) {
		return service.otp(otp, id, session,redirectAttributes);
	}

	@GetMapping("/resend-otp/{id}")
	public String resend(@PathVariable int id, HttpSession session,RedirectAttributes redirectAttributes)
			throws UnsupportedEncodingException, MessagingException {
		return service.resendotp(id, session,redirectAttributes);
	}
}
