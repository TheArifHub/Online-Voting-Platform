package com.arif.online_voting_system.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import com.arif.online_voting_system.service.VoterService;

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
}
