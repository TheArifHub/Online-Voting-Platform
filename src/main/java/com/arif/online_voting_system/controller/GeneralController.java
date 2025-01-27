package com.arif.online_voting_system.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.arif.online_voting_system.service.VoterService;

import jakarta.servlet.http.HttpSession;

@Controller
public class GeneralController {
	
	@Autowired
	VoterService service;

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
	
	@PostMapping("/login")
	public String login(@RequestParam(value = "voterid") String voterid,@RequestParam(value = "password") String password,HttpSession session,RedirectAttributes redirectAttributes)
	{
		return service.login(voterid,password,session,redirectAttributes);
	}
}
