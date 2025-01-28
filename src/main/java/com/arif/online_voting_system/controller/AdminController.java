package com.arif.online_voting_system.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.arif.online_voting_system.service.AdminService;
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
	AdminService adminService;

	@PostMapping("/login")
	public String login(@RequestParam String adminname, @RequestParam String password, HttpSession session) {
		return adminService.login(adminname, password, session);
	}
}
